package com.example.firstserviceapp_musicplayer;

import android.graphics.Bitmap;
import android.net.Uri;

public class AudioData implements Runnable{
    private final Uri uri;
    private final String name;
    private final String duration;
    private int size;
    private final Bitmap imageUrl;
    private final long durationInMS;

    public AudioData(Uri uri, String name, String duration, int size, Bitmap imageUrl, long durationInMS) {
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.imageUrl = imageUrl;
        this.durationInMS = durationInMS;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public String getDuration() {
        return duration;
    }

    public long getDurationInMS() {
        return durationInMS;
    }

    public Bitmap getImageUrl() {
        return imageUrl;
    }

    @Override
    public void run() {

    }
}

