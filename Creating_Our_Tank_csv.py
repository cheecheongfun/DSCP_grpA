import psycopg2
import pandas as pd

# Establish connection to PostgreSQL
pg_conn = psycopg2.connect(
  dbname=os.getenv("DB_NAME"),
  user=os.getenv("DB_USER"),
  password=os.getenv("DB_PASSWORD"),
  host=os.getenv("DB_HOST"),
  port=os.getenv("DB_PORT")
)
cursor = pg_conn.cursor()

# Query to join the tables
query = """
SELECT
    d.devicename,
    dd.deviceid,
    s.sensordescription,
    sd.value,
    dd.devicetimestamp,
    dd.dbtimestamp
FROM
    sensordata sd
JOIN
    sensors s ON s.sensorid = sd.sensorid
JOIN
    devicedata dd ON sd.dataid = dd.dataid
JOIN
    devices d ON d.deviceid = dd.deviceid
WHERE
    dd.deviceid IN (10,9,8,7)
"""
 
cursor.execute(query)
rows = cursor.fetchall()
columns = [desc[0] for desc in cursor.description]

# Convert data to a pandas DataFrame
combined_df = pd.DataFrame(rows, columns=columns)

combined_df['hourly_interval'] = combined_df['devicetimestamp'].dt.floor('10min')

# Pivot the data
pivot_df = combined_df.pivot_table(
    index=['devicename', 'deviceid', 'devicetimestamp', 'hourly_interval'],
    columns='sensordescription',
    values='value',
    aggfunc='sum'  # assuming one value per sensor per deviceid and timestamp
).reset_index()

# Flatten the columns
pivot_df.columns.name = None
pivot_df.columns = [str(col) for col in pivot_df.columns]


# Convert 'devicetimestamp' to a datetime format and add 8 hours
pivot_df['devicetimestamp'] = pd.to_datetime(pivot_df['devicetimestamp']) + pd.DateOffset(hours=8)

# Convert 'devicetimestamp' to a human-readable format
pivot_df['devicetimestamp'] = pivot_df['devicetimestamp'].dt.strftime('%Y-%m-%d %H:%M:%S')

pivot_df = pivot_df.drop_duplicates()
pivot_df.bfill(inplace=True)
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
            
# Group data by hourly intervals
grouped_df = pivot_df.drop(columns=['devicetimestamp'])
grouped_df = grouped_df.groupby(['devicename', 'deviceid', 'hourly_interval']).mean().reset_index()


# Split data by devicename and save each to a separate CSV file
for devicename, group_df in grouped_df.groupby('devicename'):
    filename = f"{devicename}_data.csv"
    group_df.to_csv(filename, index=False)
    print(f"Saved {filename}")



