package com.example.firstserviceapp_musicplayer;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {
    private static ArrayList<AudioData> audios;
    private static PlayerService localService;
    private final Context context;
    private static int playingStatus = 0;
    private static int currentPlayingIndex = 0;
    private static AudioViewHolder currentPlaying;
    private static BottomPlayerIndicator bottomPlayerIndicator;
    private static RecyclerView myRef;
    private static long pauseOffset = 0;
    private static boolean isRunning = false;

    public AudioListAdapter(Context context, ArrayList<AudioData> audios, PlayerService service, RelativeLayout layout, RecyclerView recyclerView) {
        AudioListAdapter.audios = audios;
        this.context = context;
        AudioListAdapter.localService = service;
        bottomPlayerIndicator = BottomPlayerIndicator.getInstance(layout, context);
        AudioListAdapter.myRef = recyclerView;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_template, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.titleText.setText(audios.get(holder.getAdapterPosition()).getName());
        holder.durationText.setText(audios.get(holder.getAdapterPosition()).getDuration());

        if(audios.get(holder.getAdapterPosition()).getImageUrl() != null)
            Glide.with(context).asBitmap().load(audios.get(holder.getAdapterPosition()).getImageUrl()).into(holder.imageUrl);
        else
            Glide.with(context).asDrawable().load(R.drawable.default_thumbnail).into(holder.imageUrl);

    }

    @Override
    public int getItemCount() {
//        Toast.makeText(context, String.valueOf(audios.size()), Toast.LENGTH_SHORT).show();
        return audios.size();
    }

    private void setBottomBar(AudioData audioData) {
        bottomPlayerIndicator.parent.setVisibility(View.VISIBLE);
        bottomPlayerIndicator.title.setText(audioData.getName());
        bottomPlayerIndicator.end.setText(audioData.getDuration());

        if(playingStatus == 2)
            Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(bottomPlayerIndicator.play);
        else
            Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(bottomPlayerIndicator.play);
    }

    private static void startTimerC(){
        if(!isRunning){
            bottomPlayerIndicator.start.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            bottomPlayerIndicator.start.start();
            isRunning = true;
        }
    }

    private static void pauseTimerC(){
        if(isRunning){
            bottomPlayerIndicator.start.stop();
            pauseOffset = SystemClock.elapsedRealtime() - bottomPlayerIndicator.start.getBase();
            isRunning = false;
        }
    }

    private static void stopTimerC(){
        if(isRunning){
            bottomPlayerIndicator.start.setBase(SystemClock.elapsedRealtime());
            pauseOffset = 0;
            isRunning = false;
        }
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText,durationText;
        private final CardView itemParent;
        private final ImageView imageUrl, playPauseButton;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            titleText = itemView.findViewById(R.id.audioTitle);
            durationText = itemView.findViewById(R.id.audioDuration);
            itemParent = itemView.findViewById(R.id.item_parent);
            imageUrl = itemView.findViewById(R.id.imageUrl);
            playPauseButton = itemView.findViewById(R.id.playOrPause);
            itemParent.setOnClickListener(view -> {
                if(playingStatus == 0) {
                    Toast.makeText(context, "Starting New", Toast.LENGTH_SHORT).show();
                    startTimerC();
                    currentPlayingIndex = getAdapterPosition();
                    localService.createAndStartPlayer(audios.get(getAdapterPosition()).getUri());
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_circle_24).into(playPauseButton);
                    playingStatus = 2;
                    currentPlaying = this;
                }
                else {
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_circle_24).into(currentPlaying.playPauseButton);
                    if(currentPlayingIndex != getAdapterPosition()){
                        Toast.makeText(context, "Stoping and Starting New", Toast.LENGTH_SHORT).show();
                        stopTimerC();
                        startTimerC();
                        localService.stopPlayer();
                        localService.createAndStartPlayer(audios.get(getAdapterPosition()).getUri());
                        currentPlayingIndex = getAdapterPosition();
                        currentPlaying = this;
                        playingStatus = 2;
                        Glide.with(context).asDrawable().load(R.drawable.baseline_pause_circle_24).into(playPauseButton);
                    }else{
                        if(playingStatus == 2) {
                            Toast.makeText(context, "Pausing", Toast.LENGTH_SHORT).show();
                            pauseTimerC();
                            localService.pausePlayer();
                            playingStatus = 1;
                        } else{
                            Toast.makeText(context, "Resuming", Toast.LENGTH_SHORT).show();
                            startTimerC();
                            localService.startPlayer();
                            playingStatus = 2;
                            Glide.with(context).asDrawable().load(R.drawable.baseline_pause_circle_24).into(currentPlaying.playPauseButton);
                        }
                    }
                }
                setBottomBar(audios.get(getAdapterPosition()));
            });
        }
    }

    private static class BottomPlayerIndicator{
        private static BottomPlayerIndicator indicator = null;
        private final RelativeLayout parent;
        private final TextView title, end;
        private final Chronometer start;
        private final ImageButton play, next, prev;
        private final ProgressBar progress;

        private BottomPlayerIndicator(RelativeLayout layout, Context context){
            parent = layout;
            title = layout.findViewById(R.id.currentPlayingTitle);
            start = layout.findViewById(R.id.startTime);
            end = layout.findViewById(R.id.endTime);
            play = layout.findViewById(R.id.playButton);
            next = layout.findViewById(R.id.nextButton);
            prev = layout.findViewById(R.id.previousButton);
            progress = layout.findViewById(R.id.progressBar2);

            play.setOnClickListener(view -> {
                if(playingStatus == 2) {
                    pauseTimerC();
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(play);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_circle_24).into(currentPlaying.playPauseButton);
                    localService.pausePlayer();
                    playingStatus = 1;
                } else{
                    startTimerC();
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(play);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_circle_24).into(currentPlaying.playPauseButton);
                    localService.startPlayer();
                    playingStatus = 2;
                }

            });

            next.setOnClickListener(view -> {
                if(currentPlayingIndex<audios.size()) {
                    stopTimerC();
                    startTimerC();
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_circle_24).into(currentPlaying.playPauseButton);
                    AudioViewHolder nextHolder = (AudioViewHolder) Objects.requireNonNull(myRef.findViewHolderForAdapterPosition(currentPlayingIndex + 1));
                    localService.stopPlayer();
                    localService.createAndStartPlayer(audios.get(currentPlayingIndex + 1).getUri());
                    currentPlayingIndex += 1;
                    currentPlaying = nextHolder;
                    playingStatus = 2;
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_circle_24).into(nextHolder.playPauseButton);

                    setBottomBar(context, audios.get(currentPlayingIndex));
                }
            });

            prev.setOnClickListener(view -> {
                if(currentPlayingIndex>0){
                    stopTimerC();
                    startTimerC();
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_circle_24).into(currentPlaying.playPauseButton);
                    AudioViewHolder prevHolder = (AudioViewHolder) Objects.requireNonNull(myRef.findViewHolderForAdapterPosition(currentPlayingIndex - 1));
                    localService.stopPlayer();
                    localService.createAndStartPlayer(audios.get(currentPlayingIndex-1).getUri());
                    currentPlayingIndex -=1;
                    currentPlaying = prevHolder;
                    playingStatus = 2;
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_circle_24).into(prevHolder.playPauseButton);

                    setBottomBar(context,audios.get(currentPlayingIndex));
                }
            });
        }

        private void setBottomBar(Context context, AudioData audio) {
            parent.setVisibility(View.VISIBLE);
            title.setText(audio.getName());
            end.setText(audio.getDuration());
            Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(play);
        }

        public static synchronized BottomPlayerIndicator getInstance(RelativeLayout layout, Context context){
            if(indicator == null){
                indicator = new BottomPlayerIndicator(layout, context);
            }
            return indicator;
        }
    }
}
