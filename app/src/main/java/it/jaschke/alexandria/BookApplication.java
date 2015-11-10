package it.jaschke.alexandria;


import android.app.Application;

import com.facebook.stetho.Stetho;

public class BookApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //todo: remove when done
        Stetho.initializeWithDefaults(this);
    }
}
