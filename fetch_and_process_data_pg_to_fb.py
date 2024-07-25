import os
import psycopg2
import pandas as pd
import json
import requests

# Firebase configuration
FIREBASE_DATABASE_URL = os.getenv('FIREBASE_DATABASE_URL')
FIREBASE_DATABASE_SECRET = os.getenv('FIREBASE_DATABASE_SECRET')

def get_latest_timestamp():
    try:
        response = requests.get(f'{FIREBASE_DATABASE_URL}/Tanks/data.json?auth={FIREBASE_DATABASE_SECRET}')
        response.raise_for_status()
        data = response.json()
    except requests.RequestException as e:
        print(f"Error fetching data from Firebase: {e}")
        return None

    # Print the fetched data to inspect its structure
    print("Fetched data:", json.dumps(data, indent=2))

    if data:
        timestamps = []
        for device_data in data.values():
            timestamps.extend(device_data.keys())

        # Print timestamps to inspect
        print("Timestamps:", timestamps)

        valid_timestamps = [ts for ts in timestamps if pd.to_datetime(ts, errors='coerce') is not pd.NaT]

        if valid_timestamps:
            try:
                latest_timestamp = max(valid_timestamps, key=lambda k: pd.to_datetime(k, errors='coerce'))
                return latest_timestamp
            except Exception as e:
                print("Error finding the latest timestamp:", e)
    return None

def fetch_data(query):
    try:
        pg_conn = psycopg2.connect(
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD"),
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT")
        )
        cursor = pg_conn.cursor()
        cursor.execute(query)
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]
    except psycopg2.Error as e:
        print(f"Error querying PostgreSQL: {e}")
        return pd.DataFrame()

    combined_df = pd.DataFrame(rows, columns=columns)
    combined_df['devicetimestamp'] = pd.to_datetime(combined_df['devicetimestamp']) + pd.Timedelta(hours=8)

    pivot_df = combined_df.pivot_table(
        index=['devicename', 'deviceid', 'devicetimestamp'],
        columns='sensordescription',
        values='value',
        aggfunc='first'
    ).reset_index()
    pivot_df.columns.name = None
    pivot_df.columns = [str(col) for col in pivot_df.columns]

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

    data_dict = {}
    for _, row in pivot_df.iterrows():
        devicename = row['devicename']
        timestamp = row['devicetimestamp'].strftime('%Y-%m-%dT%H:%M:%S')
        if devicename not in data_dict:
            data_dict[devicename] = {}
        data_dict[devicename][timestamp] = row.drop(['devicename', 'deviceid', 'devicetimestamp']).to_dict()

    return data_dict

def fetch_new_data(since_timestamp):
    since_timestamp = pd.to_datetime(since_timestamp) - pd.Timedelta(hours=8)

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
    return fetch_data(query)

def fetch_all_data():
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
    return fetch_data(query)

def push_to_firebase(data_dict):
    for devicename, timestamps in data_dict.items():
        for timestamp, values in timestamps.items():
            url = f'{FIREBASE_DATABASE_URL}/Tanks/data/{devicename}/{timestamp}.json?auth={FIREBASE_DATABASE_SECRET}'
            print(f'Pushing to URL: {url}')  # Debug print to check URL construction
            try:
                response = requests.put(url, json=values)
                response.raise_for_status()
                print(f'Successfully pushed data for {devicename} at {timestamp}')
            except requests.RequestException as e:
                print(f'Failed to push data for {devicename} at {timestamp}. Error: {e}')

def delete_from_firebase(path):
    url = f'{FIREBASE_DATABASE_URL}/{path}.json?auth={FIREBASE_DATABASE_SECRET}'
    try:
        response = requests.delete(url)
        response.raise_for_status()
        print(f"Successfully deleted {path}")
    except requests.RequestException as e:
        print(f"Failed to delete {path}. Error: {e}")

if __name__ == "__main__":
    latest_timestamp = get_latest_timestamp()
    if latest_timestamp:
        print(f'Latest timestamp in Firebase: {latest_timestamp}')
        new_data = fetch_new_data(latest_timestamp)
    else:
        print('No existing data in Firebase. Fetching all data.')
        new_data = fetch_all_data()

    # Optionally delete old data (adjust path as needed)
    # delete_from_firebase('Tanks/data/NDS006/Tanks')

    push_to_firebase(new_data)
