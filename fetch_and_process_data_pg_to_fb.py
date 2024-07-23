import os
import psycopg2
import pandas as pd
import json
import requests

# Firebase configuration
FIREBASE_DATABASE_URL = os.getenv('FIREBASE_DATABASE_URL')
FIREBASE_DATABASE_SECRET = os.getenv('FIREBASE_DATABASE_SECRET')

def get_latest_timestamp():
    """
    Fetches the latest timestamp from the Firebase database for the 'Tanks/data' path.
    """
    response = requests.get(f'{FIREBASE_DATABASE_URL}/Tanks/data.json?auth={FIREBASE_DATABASE_SECRET}')
    data = response.json()
    if data:
        # Flatten the nested dictionary to get all timestamps
        timestamps = []
        for device_data in data.values():
            timestamps.extend(device_data.keys())
        if timestamps:
            # Determine the latest timestamp by converting to datetime and finding the max
            latest_timestamp = max(timestamps, key=lambda k: pd.to_datetime(k))
            return latest_timestamp
    return None

def fetch_new_data(since_timestamp):
    """
    Fetches new data from PostgreSQL since the given timestamp and processes it.
    """
    # Adjust the timestamp by subtracting 8 hours to align with PostgreSQL time
    since_timestamp = pd.to_datetime(since_timestamp) - pd.Timedelta(hours=8)
    
    # PostgreSQL connection details from environment variables
    pg_conn = psycopg2.connect(
        dbname=os.getenv("DB_NAME"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        host=os.getenv("DB_HOST"),
        port=os.getenv("DB_PORT")
    )
    cursor = pg_conn.cursor()

    # SQL query to get new data since the latest timestamp
    query = f"""
    SELECT
        d.devicename,
        dd.deviceid,
        s.sensordescription,
        sd.value,
        dd.devicetimestamp
    FROM
        sensordata sd
    JOIN
        sensors s ON s.sensorid = sd.sensorid
    JOIN
        devicedata dd ON sd.dataid = dd.dataid
    JOIN
        devices d ON d.deviceid = dd.deviceid
    WHERE
        dd.deviceid IN (10, 9, 8, 7) AND dd.devicetimestamp > '{since_timestamp}'
    """
    cursor.execute(query)
    rows = cursor.fetchall()
    columns = [desc[0] for desc in cursor.description]

    # Convert data to a pandas DataFrame
    combined_df = pd.DataFrame(rows, columns=columns)

    # Adjust timestamp by adding 8 hours to align with Firebase time
    combined_df['devicetimestamp'] = pd.to_datetime(combined_df['devicetimestamp']) + pd.Timedelta(hours=8)

    # Process and pivot the data as needed
    pivot_df = combined_df.pivot_table(
        index=['devicename', 'deviceid', 'devicetimestamp'],
        columns='sensordescription',
        values='value',
        aggfunc='first'
    ).reset_index()
    pivot_df.columns.name = None
    pivot_df.columns = [str(col) for col in pivot_df.columns]

    # Function to replace values outside the specified range with NaN and then forward-fill or replace with 0
    def replace_out_of_range(series, min_val, max_val):
        valid_series = series.copy()
        mask = (series < min_val) | (series > max_val)
        valid_series[mask] = pd.NA
        valid_series = valid_series.ffill().fillna(0)
        return valid_series

    if 'Soil - Temperature' in pivot_df.columns:
        pivot_df['Soil - Temperature'] = replace_out_of_range(pivot_df['Soil - Temperature'], 5, 40)

    if 'Soil - PH' in pivot_df.columns:
        pivot_df['Soil - PH'] = replace_out_of_range(pivot_df['Soil - PH'], 0, 14)

    # Convert DataFrame to a dictionary
    data_dict = {}
    for _, row in pivot_df.iterrows():
        devicename = row['devicename']
        timestamp = row['devicetimestamp'].strftime('%Y-%m-%dT%H:%M:%S')
        if devicename not in data_dict:
            data_dict[devicename] = {}
        data_dict[devicename][timestamp] = row.drop(['devicename', 'deviceid', 'devicetimestamp']).to_dict()

    return data_dict

def fetch_all_data():
    """
    Fetches all data from PostgreSQL and processes it.
    """
    # PostgreSQL connection details from environment variables
    pg_conn = psycopg2.connect(
        dbname=os.getenv("DB_NAME"),
        user=os.getenv("DB_USER"),
        password=os.getenv("DB_PASSWORD"),
        host=os.getenv("DB_HOST"),
        port=os.getenv("DB_PORT")
    )
    cursor = pg_conn.cursor()

    # SQL query to get all data
    query = """
    SELECT
        d.devicename,
        dd.deviceid,
        s.sensordescription,
        sd.value,
        dd.devicetimestamp
    FROM
        sensordata sd
    JOIN
        sensors s ON s.sensorid = sd.sensorid
    JOIN
        devicedata dd ON sd.dataid = dd.dataid
    JOIN
        devices d ON d.deviceid = dd.deviceid
    WHERE
        dd.deviceid IN (10, 9, 8, 7)
    """
    cursor.execute(query)
    rows = cursor.fetchall()
    columns = [desc[0] for desc in cursor.description]

    # Convert data to a pandas DataFrame
    combined_df = pd.DataFrame(rows, columns=columns)

    # Adjust timestamp by adding 8 hours to align with Firebase time
    combined_df['devicetimestamp'] = pd.to_datetime(combined_df['devicetimestamp']) + pd.Timedelta(hours=8)

    # Process and pivot the data as needed
    pivot_df = combined_df.pivot_table(
        index=['devicename', 'deviceid', 'devicetimestamp'],
        columns='sensordescription',
        values='value',
        aggfunc='first'
    ).reset_index()
    pivot_df.columns.name = None
    pivot_df.columns = [str(col) for col in pivot_df.columns]

    # Function to replace values outside the specified range with NaN and then forward-fill or replace with 0
    def replace_out_of_range(series, min_val, max_val):
        valid_series = series.copy()
        mask = (series < min_val) | (series > max_val)
        valid_series[mask] = pd.NA
        valid_series = valid_series.ffill().fillna(0)
        return valid_series

    if 'Soil - Temperature' in pivot_df.columns:
        pivot_df['Soil - Temperature'] = replace_out_of_range(pivot_df['Soil - Temperature'], 5, 40)

    if 'Soil - PH' in pivot_df.columns:
        pivot_df['Soil - PH'] = replace_out_of_range(pivot_df['Soil - PH'], 0, 14)

    # Convert DataFrame to a dictionary
    data_dict = {}
    for _, row in pivot_df.iterrows():
        devicename = row['devicename']
        timestamp = row['devicetimestamp'].strftime('%Y-%m-%dT%H:%M:%S')
        if devicename not in data_dict:
            data_dict[devicename] = {}
        data_dict[devicename][timestamp] = row.drop(['devicename', 'deviceid', 'devicetimestamp']).to_dict()

    return data_dict

def fetch_existing_data(devicename):
    """
    Fetches existing data for a specific device from Firebase.
    """
    url = f'{FIREBASE_DATABASE_URL}/Tanks/data/{devicename}.json?auth={FIREBASE_DATABASE_SECRET}'
    response = requests.get(url)
    data = response.json()
    return data if data else {}

def push_to_firebase(data_dict):
    """
    Pushes data to Firebase, checking if data for a specific timestamp already exists.
    """
    for devicename, timestamps in data_dict.items():
        # Fetch existing data for the device from Firebase
        existing_data = fetch_existing_data(devicename)
        
        for timestamp, values in timestamps.items():
            if timestamp not in existing_data:
                # If timestamp does not exist, push new data to Firebase
                url = f'{FIREBASE_DATABASE_URL}/Tanks/data/{devicename}/{timestamp}.json?auth={FIREBASE_DATABASE_SECRET}'
                requests.put(url, json=values)
            else:
                # Print a message if the timestamp already exists
                print(f'Timestamp {timestamp} for device {devicename} already exists. Skipping.')

if __name__ == "__main__":
    # Main function to control the flow of the script
    latest_timestamp = get_latest_timestamp()
    if latest_timestamp:
        print(f'Latest timestamp in Firebase: {latest_timestamp}')
        new_data = fetch_new_data(latest_timestamp)
    else:
        print('No existing data in Firebase. Fetching all data.')
        new_data = fetch_all_data()
    
    # Push the fetched data to Firebase
    push_to_firebase(new_data)
