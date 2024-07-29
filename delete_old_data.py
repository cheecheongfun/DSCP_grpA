import requests
import os
import logging
import datetime

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

def delete_old_data():
    try:
        # Get all device names from Firebase
        devices_url = f'{FIREBASE_DATABASE_URL}/Tanks.json?auth={FIREBASE_DATABASE_SECRET}'
        devices_response = requests.get(devices_url)
        devices_response.raise_for_status()  # Raise an exception for HTTP errors
        devices_data = devices_response.json()

        if not devices_data:
            logging.warning("No devices found in Firebase")
            return

        cutoff_date = datetime.datetime.now() - datetime.timedelta(days=30) 

        for device_name in devices_data.keys():
            hourly_data_url = f'{FIREBASE_DATABASE_URL}/Tanks/{device_name}/HourlyData.json?auth={FIREBASE_DATABASE_SECRET}'
            hourly_data_response = requests.get(hourly_data_url)
            hourly_data_response.raise_for_status()  # Raise an exception for HTTP errors
            hourly_data = hourly_data_response.json()

            if hourly_data:
                for timestamp in list(hourly_data.keys()):
                    try:
                        start_time_str = timestamp.split(" - ")[0]
                        start_time = datetime.datetime.strptime(start_time_str, '%Y-%m-%dT%H:%M:%S')
                        if start_time < cutoff_date:
                            delete_firebase_directory(f'Tanks/{device_name}/HourlyData/{timestamp}')
                    except Exception as e:
                        logging.warning(f"Invalid timestamp format: {timestamp} - Error: {e}")

    except requests.RequestException as e:
        logging.error(f"Error fetching hourly data from Firebase: {e}")

def main():
    logging.info("Script started")
    delete_old_data()
    logging.info("Old data deletion completed")

if __name__ == "__main__":
    main()
