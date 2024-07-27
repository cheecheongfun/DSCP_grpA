package sg.edu.np.mad.greencycle.SolarForecast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.Executors;

public class DataViewModel extends ViewModel {
    private final MutableLiveData<List<AzureStorageHelper.DataPoint>> dataPoints = new MutableLiveData<>();

    public LiveData<List<AzureStorageHelper.DataPoint>> getDataPoints() {
        return dataPoints;
    }

    public void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AzureStorageHelper azureStorageHelper = new AzureStorageHelper();
            List<AzureStorageHelper.DataPoint> points = azureStorageHelper.downloadAndProcessBlob();
            dataPoints.postValue(points);
        });
    }
}
