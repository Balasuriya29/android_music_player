package com.example.firstserviceapp_musicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String channelID = "MusicPlayerService";
    @Override
    public void onCreate() {
        super.onCreate();

        createNotifyChannel();
    }

    private void createNotifyChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("To Play Music");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
