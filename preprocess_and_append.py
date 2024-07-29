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
container_name2 = os.getenv('CONTAINER_NAME2')
blob_service_client = BlobServiceClient(account_url=f"https://{account_name}.blob.core.windows.net", credential=account_key)

def download_blob(blob_name):
    blob_client = blob_service_client.get_blob_client(container=container_name, blob=blob_name)
    if not blob_client.exists():
        raise FileNotFoundError(f"The specified blob '{blob_name}' does not exist in the container '{container_name}'.")
    
    download_file_path = f"./{blob_name.split('/')[-1]}"
    with open(download_file_path, "wb") as download_file:
        download_file.write(blob_client.download_blob().readall())
    return download_file_path

def download_blob2(blob_name):
    blob_client = blob_service_client.get_blob_client(container=container_name2, blob=blob_name)
    if not blob_client.exists():
        raise FileNotFoundError(f"The specified blob '{blob_name}' does not exist in the container '{container_name2}'.")
    
    download_file_path = f"./{blob_name.split('/')[-1]}"
    with open(download_file_path, "wb") as download_file:
        download_file.write(blob_client.download_blob().readall())
    return download_file_path

def upload_blob(blob_name, file_path):
    blob_client = blob_service_client.get_blob_client(container=container_name2, blob=blob_name)
    with open(file_path, "rb") as data:
        blob_client.upload_blob(data, overwrite=True)

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

def preprocess_weather_data(df_new):
    # r hum
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
    
    # air temp
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
    
    # rain fall
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

    return df_new

def preprocess_estate_dpm(new_file):
    df_new = pd.read_excel(new_file)
    df_new['PR %'] = np.where(df_new['PR %']==0,80,df_new['PR %'])
    df_new['PR %'] = np.where(df_new['PR %']<70,70,df_new['PR %'])
    df_new.drop(['Energy Generation','Expected Value kWh'],axis=1,inplace=True)
    # winsorizer = Winsorizer(capping_method='quantiles',tail='right',fold=0.05,variables='Energy kWh')
    # winsorizer.fit(df_new)
    # df_new = winsorizer.transform(df_new)
    # winsorizer = Winsorizer(capping_method='quantiles',tail='left',fold=0.01,variables='Energy kWh')
    # winsorizer.fit(df_new)
    # df_new = winsorizer.transform(df_new)
    # winsorizer = Winsorizer(capping_method='quantiles',tail='left',fold=0.05,variables='IRR Value W/m²')
    # winsorizer.fit(df_new)
    # df_new = winsorizer.transform(df_new)
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
    df=df.iloc[:-1]
    df.reset_index(inplace=True)
    df.rename(columns={'index':'datetime',0:'kW'},inplace=True)
    
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

def preprocess_soe_dpm_5mins(new_file,sensorID):
    df_new = pd.read_excel(new_file)
    df_new = transform_df(df_new)
    df_new.replace("--",0.0,inplace=True)
    df_new['datetime'] = pd.to_datetime(df_new['datetime'])
    df_new['kWh'] = df_new['kW'] * (5/60)
    if sensorID == '01':
        df_new['Panel'] = 'M3_COM1_01'
    else:
        df_new['Panel'] = 'M3_COM1_02'
    df_new['Block'] = 'SOE Block'

    def fetch_humidity_2(date):
        params = {'date': date.strftime("%Y-%m-%d")}
        response = requests.get('https://api.data.gov.sg/v1/environment/relative-humidity', params=params)
        wx_forecast = response.json()
        
        readings = wx_forecast['items']
        daily_humidity = []
        for reading in readings:
            timestamp = reading['timestamp']
            for station_reading in reading['readings']:
                if station_reading['station_id'] == 'S50':
                    daily_humidity.append({'datetime': timestamp, 'humidity(%)': station_reading['value']})
        return daily_humidity

    unique_dates = df_new['datetime'].dt.date.unique()
    with ThreadPoolExecutor(max_workers=10) as executor:
        results = list(executor.map(fetch_humidity_2, unique_dates))

    r_hum = [item for sublist in results for item in sublist]

    humidity_df = pd.DataFrame(r_hum)
    humidity_df['datetime'] = pd.to_datetime(humidity_df['datetime']).dt.tz_convert(None)

    df_new = pd.merge(df_new, humidity_df, on='datetime', how='left')

    daily_avg = df_new.groupby(df_new['datetime'].dt.date)['humidity(%)'].mean()

    for index, row in df_new.iterrows():
        if pd.isnull(row['humidity(%)']):
            date = row['datetime'].date()
            avg_humidity = daily_avg[date]
            df_new.at[index, 'humidity(%)'] = avg_humidity
    
    def fetch_airtemp_2(date):
        params = {'date': date.strftime("%Y-%m-%d")}
        response = requests.get('https://api.data.gov.sg/v1/environment/air-temperature', params=params)
        wx_forecast = response.json()
        
        readings = wx_forecast['items']
        daily_air_temp = []
        for reading in readings:
            timestamp = reading['timestamp']
            for station_reading in reading['readings']:
                if station_reading['station_id'] == 'S50':
                    daily_air_temp.append({'datetime': timestamp, 'air temperature': station_reading['value']})
        return daily_air_temp
    unique_dates = df_new['datetime'].dt.date.unique()

    with ThreadPoolExecutor(max_workers=10) as executor:
        results = list(executor.map(fetch_airtemp_2, unique_dates))

    at_hum = [item for sublist in results for item in sublist]

    airtemp_df = pd.DataFrame(at_hum)
    airtemp_df['datetime'] = pd.to_datetime(airtemp_df['datetime']).dt.tz_convert(None)

    df_new = pd.merge(df_new, airtemp_df, on='datetime', how='left')

    daily_avg = df_new.groupby(df_new['datetime'].dt.date)['air temperature'].mean()

    df_new['air temperature'] = df_new.apply(
        lambda row: daily_avg[row['datetime'].date()] if pd.isnull(row['air temperature']) else row['air temperature'], 
        axis=1
    )
    
    def fetch_rainfall_2(date):
        params = {'date': date.strftime("%Y-%m-%d")}
        response = requests.get('https://api.data.gov.sg/v1/environment/rainfall', params=params)
        wx_forecast = response.json()
        
        readings = wx_forecast['items']
        daily_rainfall = []
        for reading in readings:
            timestamp = reading['timestamp']
            for station_reading in reading['readings']:
                if station_reading['station_id'] == 'S50':
                    daily_rainfall.append({'datetime': timestamp, 'rainfall': station_reading['value']})
        return daily_rainfall

    unique_dates = df_new['datetime'].dt.date.unique()

    with ThreadPoolExecutor(max_workers=10) as executor:
        results = list(executor.map(fetch_rainfall_2, unique_dates))

    rf = [item for sublist in results for item in sublist]

    rf_df = pd.DataFrame(rf)
    rf_df['datetime'] = pd.to_datetime(rf_df['datetime']).dt.tz_convert(None)

    df_new = pd.merge(df_new, rf_df, on='datetime', how='left')

    daily_avg = df_new.groupby(df_new['datetime'].dt.date)['rainfall'].mean()

    df_new['rainfall'] = df_new.apply(
        lambda row: daily_avg[row['datetime'].date()] if pd.isnull(row['rainfall']) else row['rainfall'], 
        axis=1
    )

    date_g = df_new.groupby(df_new['datetime'].dt.date)['kW'].sum()
    zerokW = date_g[date_g==0].index
    df_new.drop(df_new[df_new['datetime'].dt.date.isin(zerokW)].index, inplace=True)

    return df_new

def preprocess_and_append(new_file):
    try:
        collated_file=download_blob2('estate_soe_combined_api_latest.xlsx')
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
    upload_blob('estate_soe_combined_api_latest_test.xlsx', collated_file)

    try:
        collated_file = download_blob2('soe_combined_api_latest.xlsx')
    except FileNotFoundError as e:
        print(e)
        return
    df_collated = pd.read_excel(collated_file)
    if new_file.endswith('01'):
        df_new = preprocess_soe_dpm_5mins(new_file, '01')
    elif new_file.endswith('02'):
        df_new = preprocess_soe_dpm_5mins(new_file, '02')
    else:
        quit()
    
    df_combined = pd.concat([df_collated, df_new], ignore_index=True)
    df_combined.to_excel(collated_file, index=False)
    upload_blob('soe_combined_api_latest_test.xlsx', collated_file)

if __name__ == "__main__":
    import sys
    if sys.argv[1] == 'estate_soe_combined_api_latest.xlsx':
        quit()
    elif sys.argv[1] == 'soe_combined_api_latest.xlsx':
        quit()
    else:
        preprocess_and_append(sys.argv[1])
