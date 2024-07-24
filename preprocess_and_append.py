import pandas as pd
from azure.storage.blob import BlobServiceClient
import os
import numpy as np
from feature_engine.outliers import Winsorizer
import requests
from concurrent.futures import ThreadPoolExecutor

account_name = os.getenv('AZURE_STORAGE_ACCOUNT_NAME')
account_key = os.getenv('AZURE_STORAGE_ACCOUNT_KEY')
container_name = os.getenv('CONTAINER_NAME')
blob_service_client = BlobServiceClient(account_url=f"https://{account_name}.blob.core.windows.net", credential=account_key)

def download_blob(blob_name):
    blob_client = blob_service_client.get_blob_client(container=container_name, blob=blob_name)
    if not blob_client.exists():
        raise FileNotFoundError(f"The specified blob '{blob_name}' does not exist in the container '{container_name}'.")
    
    download_file_path = f"./{blob_name.split('/')[-1]}"
    with open(download_file_path, "wb") as download_file:
        download_file.write(blob_client.download_blob().readall())
    return download_file_path

def upload_blob(blob_name, file_path):
    blob_client = blob_service_client.get_blob_client(container=container_name, blob=blob_name)
    with open(file_path, "rb") as data:
        blob_client.upload_blob(data, overwrite=True)

def fetch_data(url, param_key, param_date, response_key, station_id, value_key):
    params = {param_key: param_date.strftime("%Y-%m-%d")}
    response = requests.get(url, params=params)
    wx_forecast = response.json()
    
    readings = wx_forecast['items']
    daily_values = []
    for reading in readings:
        for station_reading in reading['readings']:
            if station_reading['station_id'] == station_id:
                daily_values.append(station_reading['value'])
    
    if daily_values:
        avg_value = sum(daily_values) / len(daily_values)
        return {response_key: avg_value}
    else:
        return {response_key: None}

def preprocess_weather_data(df_new):
    def fetch_humidity(date):
        return fetch_data('https://api.data.gov.sg/v1/environment/relative-humidity', 'date', date, 'humidity(%)', 'S50', 'value')

    def fetch_airtemp(date):
        return fetch_data('https://api.data.gov.sg/v1/environment/air-temperature', 'date', date, 'air temp', 'S50', 'value')

    def fetch_rainfall(date):
        return fetch_data('https://api.data.gov.sg/v1/environment/rainfall', 'date', date, 'rain fall', 'S50', 'value')

    unique_dates = df_new['Date'].dt.date.unique()
    
    with ThreadPoolExecutor(max_workers=10) as executor:
        r_hum = list(executor.map(fetch_humidity, unique_dates))
        airtemp = list(executor.map(fetch_airtemp, unique_dates))
        rainfall = list(executor.map(fetch_rainfall, unique_dates))
    
    humidity_df = pd.DataFrame(r_hum)
    airtemp_df = pd.DataFrame(airtemp)
    rainfall_df = pd.DataFrame(rainfall)

    df_new['date'] = df_new['Date'].dt.date
    df_new = pd.merge(df_new, humidity_df, on='date', how='left')
    df_new = pd.merge(df_new, airtemp_df, on='date', how='left')
    df_new = pd.merge(df_new, rainfall_df, on='date', how='left')

    for col in ['humidity(%)', 'air temp', 'rain fall']:
        daily_avg = df_new.groupby('date')[col].mean()
        for index, row in df_new.iterrows():
            if pd.isnull(row[col]):
                date = row['date']
                avg_value = daily_avg[date]
                df_new.at[index, col] = avg_value
    
    df_new.drop(columns=['date'], inplace=True)
    
    for col in ['humidity(%)', 'air temp', 'rain fall']:
        null_dates = df_new[df_new[col].isnull()]['Date'].dt.date.unique()
        min_val = df_new[col].min()
        max_val = df_new[col].max()
        random_values = np.random.uniform(min_val, max_val, size=len(null_dates))
        for date, value in zip(null_dates, random_values):
            df_new.loc[df_new['Date'].dt.date == date, col] = value

    df_new['rain fall'].fillna(0, inplace=True)
    
    return df_new

def preprocess_estate_dpm(new_file):
    # loading & preprocessing
    df_new = pd.read_excel(new_file)
    df_new['PR %'] = np.where(df_new['PR %']==0,80,df_new['PR %'])
    df_new['PR %'] = np.where(df_new['PR %']<70,70,df_new['PR %'])
    df_new.drop(['Energy Generation','Expected Value kWh'],axis=1,inplace=True)
    winsorizer = Winsorizer(capping_method='quantiles',tail='right',fold=0.05,variables='Energy kWh')
    winsorizer.fit(df_new)
    df_new = winsorizer.transform(df_new)
    winsorizer = Winsorizer(capping_method='quantiles',tail='left',fold=0.01,variables='Energy kWh')
    winsorizer.fit(df_new)
    df_new = winsorizer.transform(df_new)
    winsorizer = Winsorizer(capping_method='quantiles',tail='left',fold=0.05,variables='IRR Value W/m²')
    winsorizer.fit(df_new)
    df_new = winsorizer.transform(df_new)
    df_new.rename(columns={'Date and Time':'Date'},inplace=True)
    df_new['Date'] = pd.to_datetime(df_new['Date'])
    df_new['Month']=df_new['Date'].dt.month
    df_new['Day']=df_new['Date'].dt.day
    df_new['Block'] = df_new['Location Code'].str[7:9]
    df_new.drop(columns=['Location Code'],inplace=True)
    df_new.insert(1,'Block',df_new.pop('Block'))

    df_new = preprocess_weather_data(df_new)
    return df_new

def preprocess_estate_inv(new_file):
    df_new = pd.read_excel(new_file)
    df_new['IRR Value W/m²']=0
    df_new['PR %']=0
    df_new['Sensor ID']="NA"
    df_new['Sensor Type']="INV"
    df_new.insert(2, 'IRR Value W/m²', df_new.pop('IRR Value W/m²'))
    df_new[['IRR Value W/m²','PR %']] = df_new[['IRR Value W/m²','PR %']].astype('float64')
    winsorizer = Winsorizer(capping_method='quantiles', tail='right', fold=0.05, variables='Energy kWh')
    winsorizer.fit(df_new)
    df_new = winsorizer.transform(df_new)
    winsorizer = Winsorizer(capping_method='quantiles', tail='left', fold=0.05, variables='Energy kWh')
    winsorizer.fit(df_new)
    df_new = winsorizer.transform(df_new)
    df_new.rename(columns={'Date and Time':'Date'},inplace=True)
    df_new['Date'] = pd.to_datetime(df_new['Date'])
    df_new['Month']=df_new['Date'].dt.month
    df_new['Day']=df_new['Date'].dt.day
    df_new['Block'] = df_new['Location Code'].str[7:9]
    df_new.drop(columns=['Location Code'],inplace=True)
    df_new.insert(1,'Block',df_new.pop('Block'))

    df_new = preprocess_weather_data(df_new)
    return df_new

def preprocess_estate_irr(new_file):
    df_new = pd.read_excel(new_file)
    df_new['IRR Value W/m²']=0
    df_new['PR %']=0
    df_new['Sensor ID']="NA"
    df_new['Sensor Type']="IRR"
    df_new.insert(2, 'IRR Value W/m²', df_new.pop('IRR Value W/m²'))
    df_new[['IRR Value W/m²','PR %']] = df_new[['IRR Value W/m²','PR %']].astype('float64')
    df_new.rename(columns={'Date and Time':'Date'},inplace=True)
    df_new['Date'] = pd.to_datetime(df_new['Date'])
    df_new['Month']=df_new['Date'].dt.month
    df_new['Day']=df_new['Date'].dt.day
    df_new['Block'] = df_new['Location Code'].str[7:9]
    df_new.drop(columns=['Location Code'],inplace=True)
    df_new.insert(1,'Block',df_new.pop('Block'))

    df_new = preprocess_weather_data(df_new)
    return df_new

def transform_df(df):
    df = df.transpose().iloc[1:]
    df = df.iloc[:-1]
    df.reset_index(inplace=True)
    df.rename(columns={'index':'datetime',0:'kW'}, inplace=True)
    return df

def cap_to_2_decimals(x):
    return float(f"{x:.2f}".rstrip('0').rstrip('.'))

def preprocess_soe_dpm(new_file, sensorID):
    df_new = pd.read_excel(new_file)
    df_new = transform_df(df_new)
    df_new.replace("--",0.0,inplace=True)
    df_new['datetime'] = pd.to_datetime(df_new['datetime'])
    df_new['kWh'] = df_new['kW'] * (5/60)
    df_new.set_index('datetime', inplace=True)
    df_new = df_new['kWh'].resample('D').sum()
    df_new = pd.DataFrame({'Date':df_new.index, 'Total_kWh':df_new.values})
    df_new['Total_kWh'] = df_new['Total_kWh'].apply(cap_to_2_decimals)
    if sensorID == '01':
        df_new['Sensor ID'] = 'M3_COM1_01'
    else:
        df_new['Sensor ID'] = 'M3_COM1_02'
    df_new = df_new[(df_new != 0).all(axis=1)]
    df_new['Date'] = df_new['Date'].dt.strftime('%Y-%m-%d')
    df_new['Date'] = df_new['Date'].astype('object')
    df_new['IRR Value W/m²']=0
    df_new['Location Code']="SOE Block"
    df_new['PR %']=0
    df_new['Sensor Type']="DPM"
    df_new.insert(1, 'Location Code', df_new.pop('Location Code'))
    df_new.insert(2, 'IRR Value W/m²', df_new.pop('IRR Value W/m²'))
    df_new.insert(5, 'Sensor ID', df_new.pop('Sensor ID'))
    df_new.rename(columns = {'Date':'Date and Time', 'Total_kWh':'Energy kWh'}, inplace = True)
    df_new[['IRR Value W/m²','PR %']] = df_new[['IRR Value W/m²','PR %']].astype('float64')
    df_new.rename(columns = {'Date and Time':'Date'}, inplace = True)
    df_new['Date'] = pd.to_datetime(df_new['Date'])
    df_new['Month'] = df_new['Date'].dt.month
    df_new['Day'] = df_new['Date'].dt.day
    df_new.loc[df_new['Location Code'] == 'SOE Block', 'Location Code'] = None
    df_new['Block'] = df_new['Location Code'].str[7:9]
    df_new.drop(columns=['Location Code'], inplace=True)
    df_new['Block'].fillna('SOE Block', inplace=True)  
    df_new.insert(1, 'Block', df_new.pop('Block'))
    df_new = preprocess_weather_data(df_new)
    return df_new


def preprocess_and_append(new_file):
    try:
        collated_file=download_blob('final_data/estate_soe_combined_api_latest.xlsx')
    except FileNotFoundError as e:
        print(e)
        return
    df_collated = pd.read_excel(collated_file)
    
    if 'DPM' in new_file:
        df_new = preprocess_estate_dpm(new_file)
    elif 'INV' in new_file:
        df_new = preprocess_estate_inv(new_file)
    elif 'IRR' in new_file:
        df_new = preprocess_estate_irr(new_file)
    elif '01' in new_file:
        df_new = preprocess_soe_dpm(new_file, '01')
    elif '02' in new_file:
        df_new = preprocess_soe_dpm(new_file, '02')
    else:
        quit()
    

    # combine and upload
    df_combined = pd.concat([df_collated, df_new], ignore_index=True)
    df_combined.to_excel(collated_file, index=False)
    upload_blob('final_data/estate_soe_combined_api_latest.xlsx', collated_file)

if __name__ == "__main__":
    import sys
    if sys.argv[1] == 'estate_soe_combined_api_latest.xlsx':
        quit()
    else:
        preprocess_and_append(sys.argv[1])
