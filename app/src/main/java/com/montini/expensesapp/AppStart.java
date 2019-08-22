package com.montini.expensesapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AppStart extends Application {
    public static final String CHANNEL_AppInfo = "App Info";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel app_infoCHANNEL = new NotificationChannel(
                    CHANNEL_AppInfo,
                    "App Info",
                    NotificationManager.IMPORTANCE_HIGH
            );
            app_infoCHANNEL.setDescription("App Information Notifications");//=========

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(app_infoCHANNEL);
        }
    }
}