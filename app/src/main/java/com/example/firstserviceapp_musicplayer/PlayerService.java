package com.example.firstserviceapp_musicplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class PlayerService extends Service {
    private MediaPlayer player;
    private final IBinder binder = new LocalBinder();
    private boolean isPlaying=false, serviceStarted = false;
    private NotificationCompat.Builder notification;
    private NotificationManager mNotificationManager;
    private Intent pauseIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        pauseIntent = new Intent(this, PlayerService.class);
        pauseIntent.putExtra("Call", 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getExtras().getInt("Call")){
            case 1:
                AudioListAdapter.getBottomPlayerIndicator().getNext().callOnClick();
                break;
            case -1:
                AudioListAdapter.getBottomPlayerIndicator().getPrev().callOnClick();
                break;
            default:
                AudioListAdapter.getBottomPlayerIndicator().getPlay().callOnClick();
                if(isPlaying)
                    changeActionIcon(R.drawable.baseline_pause_24);
                else
                    changeActionIcon(R.drawable.baseline_play_arrow_24);

                break;

        }
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

    public void createAndStartPlayer(AudioData audio) {
        player = MediaPlayer.create(this, audio.getUri());
        player.setLooping(true);
        startPlayer();

        if(!serviceStarted)
            startNotificationService(audio);
        else
            changeCurrentTrackInNotification(audio);
    }

    public void startPlayer(){
        isPlaying = true;
        player.start();
        if(serviceStarted) changeActionIcon(R.drawable.baseline_pause_24);
    }

    public void pausePlayer(){
        if(isPlaying) {
            player.pause();
            isPlaying = false;
            changeActionIcon(R.drawable.baseline_play_arrow_24);
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

    public void startNotificationService(AudioData audio){
        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.putExtra("Call", 1);
        Intent prevIntent = new Intent(this, PlayerService.class);
        prevIntent.putExtra("Call", -1);
        Intent pauseIntent = new Intent(this, PlayerService.class);
        pauseIntent.putExtra("Call", 0);
        Intent onClickIntent = new Intent(this, MainActivity.class);
        onClickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        onClickIntent.putExtra("ClickedNotifi", 1);

        PendingIntent pendingIntentNext =
                PendingIntent.getService(this, 1, nextIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        PendingIntent pendingIntentPrev =
                PendingIntent.getService(this, -1, prevIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        PendingIntent pendingIntentPause =
                PendingIntent.getService(this, 0, pauseIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        PendingIntent pendingIntentOnClick =
                PendingIntent.getActivity(this, 1, onClickIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        notification = new NotificationCompat.Builder(this, App.channelID)
                .setContentTitle(audio.getName())
                .setSmallIcon(R.drawable.baseline_favorite_border_24)
                .setContentText(audio.getArtist())
                .setLargeIcon(audio.getImageUrl())
                .setContentIntent(pendingIntentOnClick)
                .addAction(new NotificationCompat.Action(R.drawable.baseline_skip_previous_24, "Previous", pendingIntentPrev))
                .addAction(new NotificationCompat.Action(R.drawable.baseline_pause_24, "Play/Pause", pendingIntentPause))
                .addAction(new NotificationCompat.Action(R.drawable.baseline_skip_next_24, "Next", pendingIntentNext))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle());

        startForeground(1, notification.build());
        serviceStarted = true;
    }

    private void changeActionIcon(int drawable) {
        notification.mActions.set(1, new NotificationCompat.Action(drawable, "Play/Pause",
                PendingIntent.getService(this, 0, pauseIntent,
                        PendingIntent.FLAG_IMMUTABLE)
        ));
        mNotificationManager.notify(1, notification.build());
    }

    private void changeCurrentTrackInNotification(AudioData audio){
        notification
                .setContentTitle(audio.getName())
                .setContentText(audio.getArtist())
                .setLargeIcon(audio.getImageUrl());
        mNotificationManager.notify(1, notification.build());
    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }
}
