import pandas as pd
from azure.storage.blob import BlobServiceClient
import os
import numpy as np
from feature_engine.outliers import Winsorizer
import requests
from concurrent.futures import ThreadPoolExecutor

account_name = os.getenv('AZURE_STORAGE_ACCOUNT_NAME')
account_key = os.getenv('AZURE_STORAGE_ACCOUNT_KEY')
container_name = 'datadump'
final_data_folder = 'final_data'
blob_service_client = BlobServiceClient(account_url=f"https://{account_name}.blob.core.windows.net", credential=account_key)

def download_blob(blob_name):
    blob_client = blob_service_client.get_blob_client(container=container_name, blob=f"{final_data_folder}/{blob_name}")
    download_file_path = f"./{blob_name}"
    with open(download_file_path, "wb") as download_file:
        download_file.write(blob_client.download_blob().readall())
    return download_file_path

def upload_blob(blob_name, file_path):
    blob_client = blob_service_client.get_blob_client(container=container_name, blob=f"{final_data_folder}/{blob_name}")
    with open(file_path, "rb") as data:
        blob_client.upload_blob(data, overwrite=True)

def preprocess_and_append(new_file):
    collated_file = download_blob('estate_soe_combined_api_latest.xlsx')
    df_collated = pd.read_excel(collated_file)

    df_new = pd.read_excel(new_file)
    df_new['PR %'] = np.where(df_new['PR %']==0,80,df_new['PR %'])
    df_new['PR %'] = np.where(df_new['PR %']<70,70,df_new['PR %'])
    df_new.drop(['Energy Generation','Expected Value kWh'],axis=1,inplace=True)
    winsorizer = Winsorizer(capping_method='quantiles',tail='right',fold=0.05,vairables='Energy kWh')
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
    ## relative humidity
    def fetch_humidity(date):
        params = {'date':date.strftime("%Y-%m-%d")}
        response = requests.get('https://api.data.gov.sg/v1/environment/relative-humidity', params=params)
        wx_forecast = response.json()
        
        readings = wx_forecast['items']
        daily_humidity = []
        for reading in readings:
            for station_reading in reading['readings']:
                if station_reading['station_id'] == 'S50':
                    daily_humidity.append(station_reading['value'])
        
        if daily_humidity:
            avg_humidity = sum(daily_humidity) / len(daily_humidity)
            return {'date': date, 'humidity(%)': avg_humidity}
        else:
            return {'date': date, 'humidity(%)': None}
    unique_dates = df_new['Date'].dt.date.unique()
    with ThreadPoolExecutor(max_workers=10) as executor:
        r_hum=list(executor.map(fetch_humidity,unique_dates))
    humidity_df = pd.DataFrame(r_hum)
    df_new['date']=df_new['Date'].dt.date
    df_new = pd.merge(df_new,humidity_df,on='date',how='left')
    daily_avg=df_new.groupby('date')['humidity(%)'].mean()
    for index, row in df_new.iterrows():
        if pd.isnull(row['humidity(%)']):
            date = row['date']
            avg_humidity = daily_avg[date]
            df_new.at[index, 'humidity(%)'] = avg_humidity
    df_new.drop(columns=['date'],inplace=True)
    null_dates = df_new[df_new['humidity(%)'].isnull()]['Date'].dt.date.unique()
    min_h = df_new['humidity(%)'].min()
    max_h = df_new['humidity(%)'].max()
    random_h_values=np.random.uniform(min_h,max_h,size=len(null_dates))
    for date,humidity in zip(null_dates,random_h_values):
        df_new.loc[df_new['Date'].dt.date==date,'humidity(%)']=humidity
    ## air temp
    def fetch_airtemp(date):
        params = {'date': date.strftime("%Y-%m-%d")}
        response = requests.get('https://api.data.gov.sg/v1/environment/air-temperature', params=params)
        wx_forecast = response.json()
        
        readings = wx_forecast['items']
        daily_airtemp = []
        for reading in readings:
            for station_reading in reading['readings']:
                if station_reading['station_id'] == 'S50':
                    daily_airtemp.append(station_reading['value'])
        
        if daily_airtemp:
            avg_airtemp = sum(daily_airtemp) / len(daily_airtemp)
            return {'date': date, 'air temp': avg_airtemp}
        else:
            return {'date': date, 'air temp': None}    
    unique_dates=df_new['Date'].dt.date.unique()
    with ThreadPoolExecutor(max_workers=10) as executor:
        airtemp=list(executor.map(fetch_airtemp,unique_dates))
    airtemp_df = pd.DataFrame(airtemp)
    df_new['date']=df_new['Date'].dt.date
    df_new=pd.merge(df_new,airtemp_df,on='date',how='left')
    for index,row in df_new.iterrows():
        if pd.isnull(row['air temp']):
            date=row['date']
            avg_airtemp=daily_avg[date]
            df_new.at[index,'air temp']=avg_airtemp
    df_new.drop(columns=['date'],inplace=True)
    null_dates=df_new[df_new['air temp'].isnull()]['Date'].dt.date.unique()
    min_at = df_new['air temp'].min()
    max_at = df_new['air temp'].max()
    random_at_values=np.random.uniform(min_at,max_at,size=len(null_dates))
    for date,airtemp in zip(null_dates,random_at_values):
        df_new.loc[df_new['Date'].dt.date==date,'air temp']=airtemp
    
    ## rainfall
    def fetch_rainfall(date):
        params = {'date': date.strftime("%Y-%m-%d")}
        response = requests.get('https://api.data.gov.sg/v1/environment/rainfall', params=params)
        wx_forecast = response.json()
        
        readings = wx_forecast['items']
        daily_rainfall = []
        for reading in readings:
            for station_reading in reading['readings']:
                if station_reading['station_id'] == 'S50':
                    daily_rainfall.append(station_reading['value'])
        
        if daily_rainfall:
            avg_rainfall = sum(daily_rainfall) / len(daily_rainfall)
            return {'date': date, 'rain fall': avg_rainfall}
        else:
            return {'date': date, 'rain fall': None}
    unique_dates=df_new['Date'].dt.date.unique()
    with ThreadPoolExecutor(max_workers=10) as executor:
        rainfall=list(executor.map(fetch_rainfall,unique_dates))
    rainfall_df=pd.DataFrame(rainfall)
    df_new['date']=df_new['Date'].dt.date
    df_new=pd.merge(df_new,rainfall_df,on='date',how='left')
    daily_avg = df_new.groupby('date')['rain fall'].mean()
    for index,row in df_new.iterrows():
        if pd.isnull(row['rain fall']):
            date = row['date']
            avg_rainfall=daily_avg[date]
            df_new.at[index,'rain fall']=avg_rainfall
    df_new.drop(columns=['date'],inplace=True)
    df_new['rain fall'].fillna(0,inplace=True)    
    
    

    
    df_combined = pd.concat([df_collated, df_new], ignore_index=True)

    df_combined.to_excel(collated_file, index=False)
    upload_blob('estate_soe_combined_api_latest.xlsx', collated_file)

if __name__ == "__main__":
    import sys
    preprocess_and_append(sys.argv[1])
