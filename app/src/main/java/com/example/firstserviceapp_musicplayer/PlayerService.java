package com.example.firstserviceapp_musicplayer;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;


public class PlayerService extends Service {
    private MediaPlayer player;
    private final IBinder binder = new LocalBinder();
    private boolean isPlaying=false;

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    public void createAndStartPlayer(Uri uri) {
        player = MediaPlayer.create(this, uri);
        isPlaying = true;
        player.setLooping(true);

        player.start();
    }

    public void startPlayer(){
        isPlaying = true;
        player.start();
    }

    public void pausePlayer(){
        if(isPlaying) {
            player.pause();
            isPlaying = false;
        }
    }

    public void seekAndPlayPlayer(long remainingMS) {
        player.seekTo((int) remainingMS, MediaPlayer.SEEK_CLOSEST_SYNC);
        startPlayer();
    }

    public void stopPlayer(){
        if(isPlaying) {
            player.stop();
            isPlaying = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent =
//                PendingIntent.getActivity(this, 0, notificationIntent,
//                        PendingIntent.FLAG_IMMUTABLE);
//
//        Notification notification =
//                new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
//                        .setContentTitle(getText(R.string.notification_title))
//                        .setContentText(getText(R.string.notification_message))
//                        .setSmallIcon(R.drawable.icon)
//                        .setContentIntent(pendingIntent)
//                        .setTicker(getText(R.string.ticker_text))
//                        .build();
//
//        startForeground(1, notification);

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Binding");
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
    }
}
