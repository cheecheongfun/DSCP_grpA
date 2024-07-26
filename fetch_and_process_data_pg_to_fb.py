import requests
import psycopg2
import os
import pandas as pd
import json
import logging

# Configure logging to log information, warnings, and errors
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Firebase configuration
FIREBASE_DATABASE_URL = os.getenv('FIREBASE_DATABASE_URL')
FIREBASE_DATABASE_SECRET = os.getenv('FIREBASE_DATABASE_SECRET')

# Function to delete a directory in Firebase
def delete_firebase_directory(directory_path):
    try:
        url = f'{FIREBASE_DATABASE_URL}/{directory_path}.json?auth={FIREBASE_DATABASE_SECRET}'
        response = requests.delete(url)
        response.raise_for_status()  # Raise an exception for HTTP errors
        if response.status_code == 200:
            logging.info(f"Successfully deleted directory: {directory_path}")
        else:
            logging.warning(f"Failed to delete directory: {directory_path}. Response: {response.content}")
    except requests.RequestException as e:
        logging.error(f"Error deleting directory in Firebase: {e}")

# Example usage of deleting a directory before pushing new data
#directory_to_delete = "Tanks"  # Adjust this to the path you need to delete
#delete_firebase_directory(directory_to_delete)

def get_latest_timestamp():
    try:
        # Get all device names from Firebase
        devices_url = f'{FIREBASE_DATABASE_URL}/Tanks.json?auth={FIREBASE_DATABASE_SECRET}'
        devices_response = requests.get(devices_url)
        devices_response.raise_for_status()  # Raise an exception for HTTP errors
        devices_data = devices_response.json()

        if not devices_data:
            logging.warning("No devices found in Firebase")
            return None

        all_timestamps = []
        for device_name in devices_data.keys():
            hourly_data_url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/HourlyData.json?auth={FIREBASE_DATABASE_SECRET}'
            hourly_data_response = requests.get(hourly_data_url)
            hourly_data_response.raise_for_status()  # Raise an exception for HTTP errors
            hourly_data = hourly_data_response.json()

            if hourly_data:
                timestamps = hourly_data.keys()
                for ts in timestamps:
                    try:
                        start_time_str = ts.split(" - ")[0]
                        start_time = pd.to_datetime(start_time_str, format='%Y-%m-%dT%H:%M:%S')
                        all_timestamps.append(start_time)
                    except Exception as e:
                        logging.warning(f"Invalid timestamp format: {ts} - Error: {e}")

        if all_timestamps:
            latest_timestamp = max(all_timestamps)
            return latest_timestamp
        return None
    except requests.RequestException as e:
        logging.error(f"Error fetching latest timestamp from Firebase: {e}")
        return None


# Function to fetch new data from PostgreSQL
def fetch_new_data(since_timestamp=None):
    try:
        # Establish connection to PostgreSQL
        pg_conn = psycopg2.connect(
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD"),
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT")
        )
        cursor = pg_conn.cursor()

        since_timestamp = pd.to_datetime(since_timestamp) - pd.Timedelta(hours=8)

        # Query to fetch data from PostgreSQL
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
        if since_timestamp:
            query += f" AND dd.devicetimestamp > '{since_timestamp}'"

        cursor.execute(query)
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]

        combined_df = pd.DataFrame(rows, columns=columns)

        # Convert timestamps to local time
        combined_df['devicetimestamp'] = pd.to_datetime(combined_df['devicetimestamp']) + pd.Timedelta(hours=8)
        combined_df['hourly_interval'] = combined_df['devicetimestamp'].dt.floor('H')

        # Pivot data for further processing
        pivot_df = combined_df.pivot_table(
            index=['devicename', 'deviceid', 'devicetimestamp', 'hourly_interval'],
            columns='sensordescription',
            values='value',
            aggfunc='first'
        ).reset_index()

        pivot_df = pivot_df.drop_duplicates()
        pivot_df.fillna(method='bfill', inplace=True)
        pivot_df.columns.name = None
        pivot_df.columns = [str(col) for col in pivot_df.columns]

        # Function to replace out-of-range values with forward fill or zero
        def replace_out_of_range(series, min_val, max_val):
            valid_series = series.copy()
            mask = (series < min_val) | (series > max_val)
            valid_series[mask] = pd.NA
            valid_series = valid_series.ffill().fillna(0)
            return valid_series

        # Replace out-of-range values for specific columns
        if 'Soil - Temperature' in pivot_df.columns:
            pivot_df['Soil - Temperature'] = replace_out_of_range(pivot_df['Soil - Temperature'], -10, 60)

        if 'Soil - PH' in pivot_df.columns:
            pivot_df['Soil - PH'] = replace_out_of_range(pivot_df['Soil - PH'], 0, 14)

        if 'Soil - Moisture' in pivot_df.columns:
            pivot_df['Soil - Moisture'] = replace_out_of_range(pivot_df['Soil - Moisture'], 0, 155)

        if 'Soil - EC' in pivot_df.columns:
            pivot_df['Soil - EC'] = replace_out_of_range(pivot_df['Soil - EC'], 0, 1023)

        if 'Soil - Nitrogen' in pivot_df.columns:
            pivot_df['Soil - Nitrogen'] = replace_out_of_range(pivot_df['Soil - Nitrogen'], 0, 300)

        if 'Soil - Potassium' in pivot_df.columns:
            pivot_df['Soil - Potassium'] = replace_out_of_range(pivot_df['Soil - Potassium'], 0, 300)

        if 'Soil - Phosphorus' in pivot_df.columns:
            pivot_df['Soil - Phosphorus'] = replace_out_of_range(pivot_df['Soil - Phosphorus'], 0, 300)

        # Get the latest live data
        latest_live_df = pivot_df.loc[pivot_df.groupby(['deviceid'])['devicetimestamp'].idxmax()]
        latest_live_df = latest_live_df.drop(columns=['hourly_interval'])

        # Group data by hourly intervals
        grouped_df = pivot_df.groupby(['devicename', 'deviceid', 'hourly_interval']).mean().reset_index()
        grouped_df = grouped_df.drop(columns=['devicetimestamp'])

        # Convert grouped data to dictionary format
        data_dict1 = {}
        for _, row in grouped_df.iterrows():
            devicename = row['devicename']
            start_time = row['hourly_interval']
            end_time = start_time + pd.Timedelta(hours=1)
            timestamp = f"{start_time.strftime('%Y-%m-%dT%H:%M:%S')} - {end_time.strftime('%H:%M:%S')}"
            if devicename not in data_dict1:
                data_dict1[devicename] = {}
            data_dict1[devicename][timestamp] = row.drop(['devicename', 'deviceid', 'hourly_interval']).to_dict()

        # Convert latest live data to dictionary format
        data_dict2 = {}
        for _, row in latest_live_df.iterrows():
            devicename = row['devicename']
            if devicename not in data_dict2:
                data_dict2[devicename] = {}
            data_dict2[devicename] = row.drop(['devicename', 'deviceid']).to_dict()

        return data_dict1, data_dict2
    except psycopg2.Error as e:
        logging.error(f"Database error: {e}")
        return {}, {}
    except Exception as e:
        logging.error(f"Unexpected error while fetching data: {e}")
        return {}, {}

def push_data_to_firebase(data_dict1, data_dict2):
    # Function to serialize data for Firebase
    def serialize_data(data):
        serialized_data = {}
        for k, v in data.items():
            if isinstance(v, pd.Timestamp):
                serialized_data[k] = v.isoformat()
            elif isinstance(v, dict):
                serialized_data[k] = serialize_data(v)
            elif isinstance(v, (int, float)):
                serialized_data[k] = round(v, 1)
            else:
                serialized_data[k] = v
        return serialized_data

    try:
        # Push hourly data to Firebase
        for device_name, timestamps in data_dict1.items():
            for timestamp, values in timestamps.items():
                # Serialize data before pushing
                serialized_values = serialize_data(values)
                url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/HourlyData/{timestamp}.json?auth={FIREBASE_DATABASE_SECRET}'
                response = requests.put(url, json=serialized_values)
                response.raise_for_status()  # Raise an exception for HTTP errors
                logging.info(f"Pushed hourly data to Firebase: {device_name} - {timestamp} - {serialized_values}")

        # Push latest live data to Firebase
        for device_name, values in data_dict2.items():
            # Serialize data before pushing
            serialized_values = serialize_data(values)
            url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/LiveData.json?auth={FIREBASE_DATABASE_SECRET}'
            response = requests.put(url, json=serialized_values)
            response.raise_for_status()  # Raise an exception for HTTP errors
            logging.info(f"Pushed live data to Firebase: {device_name} - {serialized_values}")

    except requests.RequestException as e:
        logging.error(f"Error pushing data to Firebase: {e}")

def main():
    logging.info("Script started")

    # Get the latest timestamp from Firebase
    latest_timestamp = get_latest_timestamp()
    if latest_timestamp:
        logging.info(f"Latest timestamp: {latest_timestamp}")
    else:
        logging.warning("No latest timestamp found.")
        latest_timestamp = None

    # Fetch new data from PostgreSQL since the latest timestamp
    data_dict1, data_dict2 = fetch_new_data(latest_timestamp)
    
    # Check if any new data was fetched
    if data_dict1 or data_dict2:
        logging.info("Data fetched successfully")

        # Push the new data to Firebase
        if data_dict1:
            push_data_to_firebase(data_dict1, data_dict2)
            logging.info("Data pushed to Firebase successfully")
        else:
            logging.warning("No new hourly data to push to Firebase.")
    else:
        logging.warning("No new data fetched. Nothing to push to Firebase.")

if __name__ == "__main__":
    main()

