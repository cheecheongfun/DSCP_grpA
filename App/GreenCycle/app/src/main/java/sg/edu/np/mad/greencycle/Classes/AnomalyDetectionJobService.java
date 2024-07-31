package sg.edu.np.mad.greencycle.Classes;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnomalyDetectionJobService extends JobService {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("AnomalyDetectionJobService", "Job started.");
        executorService.submit(() -> {
            // Perform the background work
            boolean anomalyDetected = checkForAnomalies();
            if (anomalyDetected) {
                // Notify user about the anomaly
                sendNotification();
            }
            // Notify JobScheduler that the job is finished
            jobFinished(params, false);
        });
        return true; // Return true to indicate that the job is ongoing
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Job needs to be rescheduled if it stops before completion
        return true;
    }

    private boolean checkForAnomalies() {
        PostAnomalyData apiClient = new PostAnomalyData();
        final boolean[] allAnomaly = new boolean[1];
        apiClient.postAnomalyData(new double[]{0, 0, 0, 0, 0}, new int[]{7,7,7,7,7}, new int[]{16, 17, 18, 19, 20}, new double[]{0, 0, 0, 0, 0}, new double[]{0, 0, 0, 0, 0}, new double[]{90, 100, 80, 40, 50}, new PostAnomalyData.ModelCallback() {
            @Override
            public void onSuccess(List<String> modelOutput) {
                Log.d("postAnomalyData", "Model returned values: " + modelOutput.toString()); // Log the model result
                allAnomaly[0] = true;
                for (String output : modelOutput){
                    if(!output.equals("anomaly")){
                        allAnomaly[0] = false;
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                allAnomaly[0] = false;
            }
        });

        return allAnomaly[0]; // Example return value; replace with actual anomaly detection result
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
