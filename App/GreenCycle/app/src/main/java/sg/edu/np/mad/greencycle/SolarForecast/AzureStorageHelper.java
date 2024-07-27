package sg.edu.np.mad.greencycle.SolarForecast;

import static org.apache.poi.openxml4j.opc.OPCPackage.*;

import android.util.Log;

import okhttp3.OkHttpClient;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.SAXParserFactory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class AzureStorageHelper {
    private static final String BASE_URL = "https://dscpgroupa.blob.core.windows.net/";
    private static final String TAG = "AzureStorageHelper";

    private Retrofit retrofit;
    private AzureStorageService service;

    public AzureStorageHelper() {
        OkHttpClient client = new OkHttpClient.Builder().build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build();  // No converters needed for ResponseBody

        service = retrofit.create(AzureStorageService.class);
    }

    public List<DataPoint> downloadAndProcessBlob() {
        List<DataPoint> dataPoints = new ArrayList<>();
        try {
            Call<ResponseBody> call = service.downloadBlob();
            Response<ResponseBody> response = call.execute();  // This should work

            if (response.isSuccessful() && response.body() != null) {
                InputStream inputStream = response.body().byteStream();  // Use byteStream to get InputStream
                dataPoints = processExcelFile(inputStream);
            } else {
                Log.e(TAG, "Error downloading blob: " + response.message());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error downloading or processing blob", e);
        }
        return dataPoints;
    }

    private List<DataPoint> processExcelFile(InputStream inputStream) {
        List<DataPoint> dataPoints = new ArrayList<>();
        Map<LocalDate, List<DataPoint>> groupedDataPoints = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            OPCPackage pkg = OPCPackage.open(inputStream);
            XSSFReader xssfReader = new XSSFReader(pkg);
            XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            parser.setContentHandler(new XSSFSheetXMLHandler(
                    xssfReader.getStylesTable(),
                    null,
                    new XSSFSheetXMLHandler.SheetContentsHandler() {
                        private Map<String, String> currentRow = new HashMap<>();
                        private int rowCount = 0;

                        @Override
                        public void startRow(int rowNum) {
                            currentRow.clear();
                            rowCount++;
                        }

                        @Override
                        public void endRow(int rowNum) {
                            if ("DPM".equals(currentRow.get("G"))) {
                                DataPoint dataPoint = new DataPoint();
                                String date = currentRow.get("A");
                                LocalDate localDate = LocalDate.parse(date, formatter);
                                dataPoint.setDate(localDate);
                                dataPoint.setHumidity(Double.parseDouble(currentRow.get("J")));
                                dataPoint.setAirTemp(Double.parseDouble(currentRow.get("K")));
                                dataPoint.setRainFall(Double.parseDouble(currentRow.get("L")));

                                groupedDataPoints.computeIfAbsent(localDate, k -> new ArrayList<>()).add(dataPoint);
                            }
                        }

                        @Override
                        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                            currentRow.put(cellReference.substring(0, 1), formattedValue);
                        }

                        @Override
                        public void headerFooter(String text, boolean isHeader, String tagName) {}
                    },
                    true
            ));

            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (iter.hasNext()) {
                InputStream sheetInputStream = iter.next();
                parser.parse(new InputSource(sheetInputStream));
                sheetInputStream.close();
            }

            Log.d(TAG, "Number of rows in the Excel file: " + groupedDataPoints.size());

            // Calculate the mean values for each date group
            for (Map.Entry<LocalDate, List<DataPoint>> entry : groupedDataPoints.entrySet()) {
                List<DataPoint> dailyDataPoints = entry.getValue();
                double avgHumidity = dailyDataPoints.stream().mapToDouble(DataPoint::getHumidity).average().orElse(0);
                double avgAirTemp = dailyDataPoints.stream().mapToDouble(DataPoint::getAirTemp).average().orElse(0);
                double avgRainFall = dailyDataPoints.stream().mapToDouble(DataPoint::getRainFall).average().orElse(0);

                DataPoint avgDataPoint = new DataPoint();
                avgDataPoint.setDate(entry.getKey()); // Use the date as the key
                avgDataPoint.setHumidity(avgHumidity);
                avgDataPoint.setAirTemp(avgAirTemp);
                avgDataPoint.setRainFall(avgRainFall);

                dataPoints.add(avgDataPoint);
            }

            // Sort dataPoints by date
            dataPoints = dataPoints.stream()
                    .sorted((dp1, dp2) -> dp1.getDate().compareTo(dp2.getDate()))
                    .collect(Collectors.toList());

            Log.d(TAG, "Processed " + dataPoints.size() + " rows from the Excel file.");
        } catch (Exception e) {
            Log.e(TAG, "Error processing Excel file", e);
        }

        return dataPoints;
    }


    public static class DataPoint {
        private LocalDate date;
        private double humidity;
        private double airTemp;
        private double rainFall;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public double getHumidity() { return humidity; }
        public void setHumidity(double humidity) { this.humidity = humidity; }
        public double getAirTemp() { return airTemp; }
        public void setAirTemp(double airTemp) { this.airTemp = airTemp; }
        public double getRainFall() { return rainFall; }
        public void setRainFall(double rainFall) { this.rainFall = rainFall; }
    }

}
