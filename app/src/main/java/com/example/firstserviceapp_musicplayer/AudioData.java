package com.example.firstserviceapp_musicplayer;

import android.graphics.Bitmap;
import android.net.Uri;

public class AudioData{
    private final Uri uri;
    private final String name;
    private final String duration;
    private int size;
    private final Bitmap imageUrl;
    private final long durationInMS;
    private final String artist;

    public AudioData(Uri uri, String name, String duration, int size, Bitmap imageUrl, long durationInMS, String artist) {
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.imageUrl = imageUrl;
        this.durationInMS = durationInMS;
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
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
}

