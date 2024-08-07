import pandas as pd
from azure.storage.blob import BlobServiceClient
import os

account_name = os.getenv('AZURE_STORAGE_ACCOUNT_NAME')
account_key = os.getenv('AZURE_STORAGE_ACCOUNT_KEY')
container_name2 = os.getenv('CONTAINER_NAME2')
blob_service_client = BlobServiceClient(account_url=f"https://{account_name}.blob.core.windows.net", credential=account_key)

def download_blob(blob_name):
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

def check_and_append(new_file):
    df_new = pd.read_excel(new_file)
    duplicates_exist = df_new.duplicated().any()
    if duplicates_exist:
        df_new.drop_duplicates(inplace=True)
        df_new.to_excel(new_file,index=False)
        upload_blob(os.path.basename(new_file), new_file)
        return True
    return False

if __name__ == "__main__":
    import sys
    new_file = download_blob(sys.argv[1])
    has_duplicates = check_and_append(new_file)
    if not has_duplicates:
        print("No dupliactes found.")
