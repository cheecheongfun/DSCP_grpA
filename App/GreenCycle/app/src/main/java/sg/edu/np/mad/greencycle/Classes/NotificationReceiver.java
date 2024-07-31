package sg.edu.np.mad.greencycle.Classes;

import static android.os.Build.*;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import sg.edu.np.mad.greencycle.FeedingLog.Feeding;
import sg.edu.np.mad.greencycle.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "feeding_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String scheduleName = intent.getStringExtra("scheduleName");
        String notificationType = intent.getStringExtra("notificationType");

        if (notificationType != null) {
            if (notificationType.equals("feedingReminder")) {
                sendFeedingReminderNotification(context, scheduleName);
            } else if (notificationType.equals("lowEnergy")) {
                lowEnergy(context);
            }
        }
    }

    private void sendFeedingReminderNotification(Context context, String scheduleName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "feeding_reminder_channel";
        String title = "Feeding Reminder";
        String message = "Time to feed your worms for " + scheduleName + "!";

        NotificationChannel channel = new NotificationChannel(channelId, "Feeding Reminder", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Reminders to feed your worms");
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(scheduleName.hashCode(), builder.build());
    }

    private void lowEnergy(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "anomaly_detection";
        String title = "Low Energy";
        String message = "Low energy detected in block idk.";

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Low Energy", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Low Energy");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(0, builder.build());
    }
}
