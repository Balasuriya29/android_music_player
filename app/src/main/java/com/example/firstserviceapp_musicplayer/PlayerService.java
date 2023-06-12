package com.example.firstserviceapp_musicplayer;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
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

    public int getDuration() {
        return player.getCurrentPosition();
    }

    public void pausePlayer(){
        if(isPlaying) {
            player.pause();
            isPlaying = false;
        }
    }

    public void stopPlayer(){
        if(isPlaying) {
            player.stop();
            isPlaying = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
