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

delete_firebase_directory('Tanks')

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
        if since_timestamp:
            query += f" AND dd.devicetimestamp > '{since_timestamp}'"

        cursor.execute(query)
        rows = cursor.fetchall()
        columns = [desc[0] for desc in cursor.description]

        combined_df = pd.DataFrame(rows, columns=columns)
        print("Fetched DataFrame:", combined_df.head())

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
        print("Pivot DataFrame:", pivot_df.head())

        def replace_out_of_range(series, min_val, max_val):
            valid_series = series.copy()
            mask = (series < min_val) | (series > max_val)
            valid_series[mask] = pd.NA
            valid_series = valid_series.ffill().fillna(0)
            return valid_series

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

        latest_live_df = pivot_df.loc[pivot_df.groupby(['deviceid'])['devicetimestamp'].idxmax()]
        latest_live_df = latest_live_df.drop(columns=['hourly_interval'])
        print("Latest Live DataFrame:", latest_live_df.head())

        grouped_df = pivot_df.groupby(['devicename', 'deviceid', 'hourly_interval']).mean().reset_index()
        grouped_df = grouped_df.drop(columns=['devicetimestamp'])
        print("Grouped DataFrame:", grouped_df.head())

        data_dict1 = {}
        for _, row in grouped_df.iterrows():
            devicename = row['devicename']
            start_time = row['hourly_interval']
            end_time = start_time + pd.Timedelta(hours=1)
            timestamp = f"{start_time.strftime('%Y-%m-%dT%H:%M:%S')} - {end_time.strftime('%H:%M:%S')}"
            if devicename not in data_dict1:
                data_dict1[devicename] = {}
            data_dict1[devicename][timestamp] = row.drop(['devicename', 'deviceid', 'hourly_interval']).to_dict()
        print("Hourly Data Dict:", data_dict1)

        data_dict2 = {}
        for _, row in latest_live_df.iterrows():
            devicename = row['devicename']
            data_dict2[devicename] = row.drop(['devicename', 'deviceid']).to_dict()
        print("Live Data Dict:", data_dict2)

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
            url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/LiveData.json?auth={FIREBASE_DATABASE_SECRET}'
            response = requests.put(url, json=serialized_values)
            response.raise_for_status()
            logging.info(f"Pushed live data to Firebase: {device_name} - {serialized_values}")

    except requests.RequestException as e:
        logging.error(f"Error pushing data to Firebase: {e}")

def main():
    latest_timestamp = get_latest_timestamp()
    logging.info(f"Latest timestamp from Firebase: {latest_timestamp}")

    data_dict1, data_dict2 = fetch_new_data(since_timestamp=latest_timestamp)
    logging.info(f"Data fetched for hourly data: {data_dict1}")
    logging.info(f"Data fetched for live data: {data_dict2}")

    if data_dict1 or data_dict2:
        push_data_to_firebase(data_dict1, data_dict2)

if __name__ == "__main__":
    main()
