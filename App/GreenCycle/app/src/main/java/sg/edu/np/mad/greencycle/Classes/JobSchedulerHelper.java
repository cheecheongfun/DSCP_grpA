package sg.edu.np.mad.greencycle.Classes;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class JobSchedulerHelper {

    private static final int JOB_ID = 1;

    public static void scheduleAnomalyDetectionJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(context, AnomalyDetectionJobService.class);

        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Optional: Specify network requirements
                .setPersisted(true) // Keep job alive across reboots
                .setPeriodic(24 * 60 * 60 * 1000); // Run every 24 hours (change as needed)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            jobBuilder.setRequiresBatteryNotLow(true); // Optional: Avoid running if battery is low
        }

        JobInfo jobInfo = jobBuilder.build();

        if (jobScheduler != null) {
            int result = jobScheduler.schedule(jobInfo);
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d("JobScheduler", "Job scheduled successfully.");
            } else {
                Log.e("JobScheduler", "Job scheduling failed.");
            }
        }
    }
}
