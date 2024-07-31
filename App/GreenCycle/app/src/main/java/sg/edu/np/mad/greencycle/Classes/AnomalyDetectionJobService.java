package sg.edu.np.mad.greencycle.Classes;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnomalyDetectionJobService extends JobService {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public boolean onStartJob(JobParameters params) {
        executorService.submit(() -> {
            // Perform the background work
            Log.d("AnomalyDetectionJobService", "Job started at: " + System.currentTimeMillis());
            checkForAnomalies(isAllAnomalies -> {
                if (isAllAnomalies) {
                    // Notify user about the anomaly
                    Log.e("anomaly noti", "Anomaly detected");
                    sendNotification();
                } else {
                    Log.d("AnomalyDetectionJobService", "No anomaly detected.");
                }

                // Notify JobScheduler that the job is finished
                jobFinished(params, false);
            });
        });
        return true; // Return true to indicate that the job is ongoing
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Job needs to be rescheduled if it stops before completion
        return true;
    }
    public interface AnomalyCheckCallback {
        void onResult(boolean isAllAnomalies);
    }


    private void checkForAnomalies(AnomalyCheckCallback callback) {
        PostAnomalyData apiClient = new PostAnomalyData();
        Set<String> uniqueStrings = new HashSet<>();

        apiClient.postAnomalyData(new double[]{0, 0, 0, 0, 0}, new int[]{7, 7, 7, 7, 7}, new int[]{16, 17, 18, 19, 20}, new double[]{0, 0, 0, 0, 0}, new double[]{0, 0, 0, 0, 0}, new double[]{90, 100, 80, 40, 50}, new PostAnomalyData.ModelCallback() {
            @Override
            public void onSuccess(List<String> modelOutput) {
                uniqueStrings.addAll(modelOutput);
                boolean allAnomaly = uniqueStrings.size() == 1 && uniqueStrings.contains("anomaly");
                Log.e("postAnomalyData", "All anomalies: " + allAnomaly);

                // Notify the result through the callback
                callback.onResult(allAnomaly);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("postAnomalyData", "API call failed", e);
                // Handle failure and notify through callback if necessary
                callback.onResult(false); // Or whatever is appropriate
            }
        });
    }


    private void sendNotification() {
        // Implement your notification logic here
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("notificationType", "lowEnergy");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();

        // Set the time to 12 AM
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
