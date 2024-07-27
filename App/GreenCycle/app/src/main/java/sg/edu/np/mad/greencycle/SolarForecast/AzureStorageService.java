package sg.edu.np.mad.greencycle.SolarForecast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface AzureStorageService {
    @GET("datadump/final_data/estate_soe_combined_api_latest.xlsx")
    Call<ResponseBody> downloadBlob();
}
