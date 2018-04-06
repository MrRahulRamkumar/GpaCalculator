package rahulramkumar.com.gpacalculator;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Rahul Ramkumar on 26-03-2018.
 */

public class GpaCalculator extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
