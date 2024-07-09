package sg.edu.np.mad.greencycle.StartUp;


import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.FirebaseApp;


public class MyApp extends Application {
    public static final String CHANNEL_ID = "FEEDING_NOTIFICATION_CHANNEL";
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d("Firebase Init", "Firebase initialized successfully");
        } catch (Exception e) {
            Log.d("Firebase Init", "Failed to initialize Firebase", e);
        }
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Feeding Notifications";
            String description = "Channel for feeding schedule notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
