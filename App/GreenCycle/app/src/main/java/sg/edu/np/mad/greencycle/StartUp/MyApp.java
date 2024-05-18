package sg.edu.np.mad.greencycle.StartUp;


import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;


public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d("Firebase Init", "Firebase initialized successfully");
        } catch (Exception e) {
            Log.d("Firebase Init", "Failed to initialize Firebase", e);
        }
    }
}
