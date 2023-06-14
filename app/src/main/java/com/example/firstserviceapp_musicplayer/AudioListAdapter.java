package com.example.firstserviceapp_musicplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;
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
    private static CountDownTimer count = null;
    private static long remainingMS = 0, start = 0, totalDuration=0;
    private static int progress=1;

    public AudioListAdapter(Context context, ArrayList<AudioData> audios, PlayerService service, RelativeLayout layout, RecyclerView recyclerView) {
        AudioListAdapter.audios = audios;
        this.context = context;
        AudioListAdapter.localService = service;
        bottomPlayerIndicator = BottomPlayerIndicator.getInstance(layout, context);
        AudioListAdapter.myRef = recyclerView;
    }

    private static void createAndStartThread(long duration){
        count = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                remainingMS = l;
                start = totalDuration - l;

                long startMin = start / 1000 / 60;
                long startSec = start / 1000 % 60;

                long remainingMin = remainingMS / 1000 / 60;
                long remainingSec = remainingMS / 1000 % 60;

                bottomPlayerIndicator.start.setText(String.format(Locale.US,"%02d:%02d", startMin, startSec));
                bottomPlayerIndicator.remaining.setText(String.format(Locale.US,"-%02d:%02d", remainingMin, remainingSec));

                bottomPlayerIndicator.progress.setProgress(progress);
                progress+=1000;
            }

            @Override
            public void onFinish() {
                System.out.println("Finished");
            }
        };

        count.start();
    }

    private static void stopThread(){
        if(count != null) count.cancel();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item_template, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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

    public class AudioViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText,durationText;
        private final RelativeLayout itemParent;
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
                    stopThread();
                    progress = 0;
                    itemParent.setBackground(AppCompatResources.getDrawable(context, R.drawable.card_gradient));
                    titleText.setTextColor(Color.WHITE);
                    durationText.setTextColor(-1);
                    totalDuration = audios.get(getAdapterPosition()).getDurationInMS();
                    bottomPlayerIndicator.progress.setMax((int) totalDuration);
                    createAndStartThread(totalDuration);
                    currentPlayingIndex = getAdapterPosition();
                    localService.createAndStartPlayer(audios.get(getAdapterPosition()).getUri());
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(this.playPauseButton);
                    playingStatus = 2;
                    currentPlaying = this;
                }
                else {
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(currentPlaying.playPauseButton);
                    if(currentPlayingIndex != getAdapterPosition()){
                        currentPlaying.itemParent.setBackgroundColor(Color.TRANSPARENT);
                        currentPlaying.titleText.setTextColor(Color.rgb(138, 162, 187));
                        currentPlaying.durationText.setTextColor(Color.rgb(138, 162, 187));

                        itemParent.setBackground(AppCompatResources.getDrawable(context, R.drawable.card_gradient));
                        titleText.setTextColor(Color.WHITE);
                        durationText.setTextColor(-1);
                        stopThread();
                        progress=0;
                        totalDuration = audios.get(getAdapterPosition()).getDurationInMS();
                        bottomPlayerIndicator.progress.setMax((int) totalDuration);
                        createAndStartThread(totalDuration);

                        localService.stopPlayer();
                        localService.createAndStartPlayer(audios.get(getAdapterPosition()).getUri());

                        currentPlayingIndex = getAdapterPosition();
                        currentPlaying = this;
                        playingStatus = 2;
                        Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(this.playPauseButton);

                    }else{
                        if(playingStatus == 2) {
                            stopThread();
                            localService.pausePlayer();
                            playingStatus = 1;
                        } else{
                            createAndStartThread(remainingMS);
                            localService.startPlayer();
                            playingStatus = 2;
                            Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(currentPlaying.playPauseButton);
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
        private final TextView title, end, start, remaining, toolTip;
        private final ImageButton play, next, prev;
        private final SeekBar progress;

        private BottomPlayerIndicator(RelativeLayout layout, Context context){
            parent = layout;
            title = layout.findViewById(R.id.currentPlayingTitle);
            start = layout.findViewById(R.id.startTime);
            end = layout.findViewById(R.id.endTime);
            play = layout.findViewById(R.id.playButton);
            next = layout.findViewById(R.id.nextButton);
            prev = layout.findViewById(R.id.previousButton);
            progress = layout.findViewById(R.id.progressBar2);
            remaining = layout.findViewById(R.id.remainingTime);
            toolTip = layout.findViewById(R.id.toolTip);

            start.setOnClickListener(view -> {
                start.setVisibility(View.INVISIBLE);
                remaining.setVisibility(View.VISIBLE);
            });

            remaining.setOnClickListener(view -> {
                remaining.setVisibility(View.INVISIBLE);
                start.setVisibility(View.VISIBLE);
            });

            play.setOnClickListener(view -> {
                if(playingStatus == 2) {
                    stopThread();
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(play);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(currentPlaying.playPauseButton);
                    localService.pausePlayer();
                    playingStatus = 1;
                } else{
                    createAndStartThread(remainingMS);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(play);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(currentPlaying.playPauseButton);
                    localService.startPlayer();
                    playingStatus = 2;
                }

            });

            next.setOnClickListener(view -> {
                if(currentPlayingIndex<audios.size()) {
                    stopThread();

                    currentPlaying.itemParent.setBackgroundColor(Color.TRANSPARENT);
                    currentPlaying.titleText.setTextColor(Color.rgb(138, 162, 187));
                    currentPlaying.durationText.setTextColor(Color.rgb(138, 162, 187));

                    totalDuration = audios.get(currentPlayingIndex+1).getDurationInMS();
                    AudioListAdapter.progress = 0;
                    progress.setMax((int) totalDuration);
                    createAndStartThread(totalDuration);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(currentPlaying.playPauseButton);
                    AudioViewHolder nextHolder = (AudioViewHolder) Objects.requireNonNull(myRef.findViewHolderForAdapterPosition(currentPlayingIndex + 1));

                    nextHolder.itemParent.setBackground(AppCompatResources.getDrawable(context, R.drawable.card_gradient));
                    nextHolder.titleText.setTextColor(Color.WHITE);
                    nextHolder.durationText.setTextColor(-1);

                    localService.stopPlayer();
                    localService.createAndStartPlayer(audios.get(currentPlayingIndex + 1).getUri());
                    currentPlayingIndex += 1;
                    currentPlaying = nextHolder;
                    playingStatus = 2;
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(nextHolder.playPauseButton);

                    setBottomBar(context, audios.get(currentPlayingIndex));
                }
            });

            prev.setOnClickListener(view -> {
                if(currentPlayingIndex>0){
                    stopThread();

                    currentPlaying.itemParent.setBackgroundColor(Color.TRANSPARENT);
                    currentPlaying.titleText.setTextColor(Color.rgb(138, 162, 187));
                    currentPlaying.durationText.setTextColor(Color.rgb(138, 162, 187));

                    totalDuration = audios.get(currentPlayingIndex-1).getDurationInMS();
                    AudioListAdapter.progress = 0;
                    progress.setMax((int) totalDuration);
                    createAndStartThread(totalDuration);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(currentPlaying.playPauseButton);
                    AudioViewHolder prevHolder = (AudioViewHolder) Objects.requireNonNull(myRef.findViewHolderForAdapterPosition(currentPlayingIndex - 1));

                    prevHolder.itemParent.setBackground(AppCompatResources.getDrawable(context, R.drawable.card_gradient));
                    prevHolder.titleText.setTextColor(Color.WHITE);
                    prevHolder.durationText.setTextColor(-1);

                    localService.stopPlayer();
                    localService.createAndStartPlayer(audios.get(currentPlayingIndex-1).getUri());
                    currentPlayingIndex -=1;
                    currentPlaying = prevHolder;
                    playingStatus = 2;
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(prevHolder.playPauseButton);

                    setBottomBar(context,audios.get(currentPlayingIndex));
                }
            });

            progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int finalChanged;
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(b && (totalDuration - i >= 1500)) {
                        toolTip.setVisibility(View.VISIBLE);
                        if(playingStatus == 2){
                            stopThread();
                            Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(play);
                            Glide.with(context).asDrawable().load(R.drawable.baseline_play_arrow_24).into(currentPlaying.playPauseButton);
                            localService.pausePlayer();
                            playingStatus = 1;
                        }
                        finalChanged = i;

                        long startMin = finalChanged / 1000 / 60;
                        long startSec = finalChanged / 1000 % 60;

                        toolTip.setText(String.format(Locale.US,"%02d:%02d", startMin, startSec));
                    }
                    int x = seekBar.getThumb().getBounds().left;
                    toolTip.setX(x);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    remainingMS = totalDuration - finalChanged;

                    AudioListAdapter.start = finalChanged;

                    long startMin = AudioListAdapter.start / 1000 / 60;
                    long startSec = AudioListAdapter.start / 1000 % 60;

                    long remainingMin = remainingMS / 1000 / 60;
                    long remainingSec = remainingMS / 1000 % 60;

                    start.setText(String.format(Locale.US, "%02d:%02d", startMin, startSec));
                    remaining.setText(String.format(Locale.US,"-%02d:%02d", remainingMin, remainingSec));
                    AudioListAdapter.progress= finalChanged;

                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(play);
                    Glide.with(context).asDrawable().load(R.drawable.baseline_pause_24).into(currentPlaying.playPauseButton);
                    playingStatus = 2;

                    localService.seekAndPlayPlayer(remainingMS);
                    createAndStartThread(remainingMS);
                    toolTip.setVisibility(View.GONE);
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
