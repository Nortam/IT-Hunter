package tech.ithunter.app;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(getApplicationContext());
    }

}
