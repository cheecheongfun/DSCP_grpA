import requests
import psycopg2
import os
import pandas as pd
import logging

# Configure logging to log information, warnings, and errors
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Firebase configuration
FIREBASE_DATABASE_URL = os.getenv('FIREBASE_DATABASE_URL')
FIREBASE_DATABASE_SECRET = os.getenv('FIREBASE_DATABASE_SECRET')

def delete_firebase_directory(directory_path):
    try:
        url = f'{FIREBASE_DATABASE_URL}/{directory_path}.json?auth={FIREBASE_DATABASE_SECRET}'
        response = requests.delete(url)
        response.raise_for_status()  # Raise an exception for HTTP errors
        if response.status_code == 200:
            logging.info(f"Successfully deleted directory: {directory_path}")
        else:
            logging.warning(f"Failed to delete directory: {directory_path}. Response: {response.content.decode()}")
    except requests.RequestException as e:
        logging.error(f"Error deleting directory in Firebase: {e}")

def convert_firebase_timestamp(firebase_timestamp):
    try:
        if isinstance(firebase_timestamp, str):
            # Extract the start time from the Firebase timestamp
            start_time_str = firebase_timestamp.split(" - ")[0]
            # Convert to database timestamp format
            return pd.to_datetime(start_time_str, format='%Y-%m-%dT%H:%M:%S').strftime('%Y-%m-%d %H:%M:%S')
        elif isinstance(firebase_timestamp, pd.Timestamp):
            # If it's already a Timestamp object, format it directly
            return firebase_timestamp.strftime('%Y-%m-%d %H:%M:%S')
        else:
            logging.error(f"Unsupported timestamp format: {firebase_timestamp}")
            return None
    except Exception as e:
        logging.error(f"Error converting Firebase timestamp: {e}")
        return None

def get_latest_timestamp():
    try:
        devices_url = f'{FIREBASE_DATABASE_URL}/Tanks.json?auth={FIREBASE_DATABASE_SECRET}'
        devices_response = requests.get(devices_url)
        devices_response.raise_for_status()
        devices_data = devices_response.json()

        if not devices_data:
            logging.warning("No devices found in Firebase")
            return None

        all_timestamps = []
        for device_name in devices_data.keys():
            hourly_data_url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/HourlyData.json?auth={FIREBASE_DATABASE_SECRET}'
            hourly_data_response = requests.get(hourly_data_url)
            hourly_data_response.raise_for_status()
            hourly_data = hourly_data_response.json()

            if hourly_data:
                timestamps = hourly_data.keys()
                for ts in timestamps:
                    try:
                        if isinstance(ts, str):
                            # Convert Firebase timestamp string to the database format
                            start_time_str = ts.split(" - ")[0]
                            start_time = pd.to_datetime(start_time_str, format='%Y-%m-%dT%H:%M:%S')
                            all_timestamps.append(start_time)
                        elif isinstance(ts, pd.Timestamp):
                            # If timestamp is already a Timestamp object, add it directly
                            all_timestamps.append(ts)
                    except Exception as e:
                        logging.warning(f"Invalid timestamp format: {ts} - Error: {e}")

        if all_timestamps:
            latest_timestamp = max(all_timestamps)
            return latest_timestamp
        return None
    except requests.RequestException as e:
        logging.error(f"Error fetching latest timestamp from Firebase: {e}")
        return None

def fetch_new_data(since_timestamp=None):
    try:
        pg_conn = psycopg2.connect(
            dbname=os.getenv("DB_NAME"),
            user=os.getenv("DB_USER"),
            password=os.getenv("DB_PASSWORD"),
            host=os.getenv("DB_HOST"),
            port=os.getenv("DB_PORT")
        )
        cursor = pg_conn.cursor()

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
        # Append timestamp condition if provided
        if since_timestamp:
            query += " AND dd.devicetimestamp > %s"

        cursor.execute(query, (since_timestamp,))
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]

        combined_df = pd.DataFrame(rows, columns=columns)
        logging.info("Fetched DataFrame:\n%s", combined_df.head())

        combined_df['devicetimestamp'] = pd.to_datetime(combined_df['devicetimestamp']) + pd.Timedelta(hours=8)
        combined_df['hourly_interval'] = combined_df['devicetimestamp'].dt.floor('H')

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
        logging.info("Pivot DataFrame:\n%s", pivot_df.head())

        def replace_out_of_range(series, min_val, max_val):
            valid_series = series.copy()
            mask = (series < min_val) | (series > max_val)
            valid_series[mask] = pd.NA
            valid_series = valid_series.ffill().fillna(0)
            return valid_series

        for sensor_name, (min_val, max_val) in {
            'Soil - Temperature': (-10, 60),
            'Soil - PH': (0, 14),
            'Soil - Moisture': (0, 155),
            'Soil - EC': (0, 1023),
            'Soil - Nitrogen': (0, 300),
            'Soil - Potassium': (0, 300),
            'Soil - Phosphorus': (0, 300)
        }.items():
            if sensor_name in pivot_df.columns:
                pivot_df[sensor_name] = replace_out_of_range(pivot_df[sensor_name], min_val, max_val)

        latest_live_df = pivot_df.loc[pivot_df.groupby(['deviceid'])['devicetimestamp'].idxmax()]
        latest_live_df = latest_live_df.drop(columns=['hourly_interval'])
        logging.info("Latest Live DataFrame:\n%s", latest_live_df.head())

        grouped_df = pivot_df.groupby(['devicename', 'deviceid', 'hourly_interval']).mean().reset_index()
        grouped_df = grouped_df.drop(columns=['devicetimestamp'])
        logging.info("Grouped DataFrame:\n%s", grouped_df.head())

        data_dict1 = {}
        for _, row in grouped_df.iterrows():
            devicename = row['devicename']
            start_time = row['hourly_interval']
            end_time = start_time + pd.Timedelta(hours=1)
            timestamp = f"{start_time.strftime('%Y-%m-%dT%H:%M:%S')} - {end_time.strftime('%H:%M:%S')}"
            if devicename not in data_dict1:
                data_dict1[devicename] = {}
            data_dict1[devicename][timestamp] = row.drop(['devicename', 'deviceid', 'hourly_interval']).to_dict()
        logging.info("Hourly Data Dict:\n%s", data_dict1)

        data_dict2 = {}
        for _, row in latest_live_df.iterrows():
            devicename = row['devicename']
            data_dict2[devicename] = row.drop(['devicename', 'deviceid']).to_dict()
        logging.info("Live Data Dict:\n%s", data_dict2)

        return data_dict1, data_dict2

    except psycopg2.Error as e:
        logging.error(f"PostgreSQL error: {e}")
        return {}, {}

def serialize_data(data):
    serialized_data = {}
    for key, value in data.items():
        if isinstance(value, dict):
            serialized_data[key] = serialize_data(value)
        elif isinstance(value, pd.Timestamp):
            serialized_data[key] = value.isoformat()
        else:
            serialized_data[key] = value
    return serialized_data

def push_data_to_firebase(data_dict1, data_dict2):
    try:
        for device_name, timestamps in data_dict1.items():
            for timestamp, values in timestamps.items():
                serialized_values = serialize_data(values)
                url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/HourlyData/{timestamp}.json?auth={FIREBASE_DATABASE_SECRET}'
                response = requests.put(url, json=serialized_values)
                response.raise_for_status()
                logging.info(f"Pushed hourly data to Firebase: {device_name} - {timestamp} - {serialized_values}")

        for device_name, values in data_dict2.items():
            serialized_values = serialize_data(values)
            url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/CurrentData.json?auth={FIREBASE_DATABASE_SECRET}'
            response = requests.put(url, json=serialized_values)
            response.raise_for_status()
            logging.info(f"Pushed live data to Firebase: {device_name} - {serialized_values}")

    except requests.RequestException as e:
        logging.error(f"Error pushing data to Firebase: {e}")

def main():
    latest_timestamp = get_latest_timestamp()
    new_data_dict1, new_data_dict2 = fetch_new_data(since_timestamp=latest_timestamp)
    push_data_to_firebase(new_data_dict1, new_data_dict2)
    logging.info("Data push to Firebase completed.")

if __name__ == "__main__":
    main()
