package de.michaelsoftware.android.Vision.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.MyService;
import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.listener.OnEndedListener;
import de.michaelsoftware.android.Vision.listener.OnTimeChangedListener;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.network.HttpAudioPlayer;

/**
 * Created by Michael on 07.06.2016.
 */
public abstract class AudioService extends Service implements AudioManager.OnAudioFocusChangeListener  {

    protected NotificationManager notificationManager = null;

    private AudioManager audioManager;

    protected HttpAudioPlayer mp;
    protected int musicId = 0;
    protected int id = 1;

    public static final String PACKAGE_NAME = "de.michaelsoftware.android.vision";

    protected static final String ACTION_PAUSE = PACKAGE_NAME + ".ACTION_PAUSE";
    protected static final String ACTION_STOP = PACKAGE_NAME + ".ACTION_STOP";
    protected HashMap<String, String> headers = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (this.mp != null) {
                    this.mp.playLast();
                    this.mp.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (this.mp.isPlaying()) this.mp.stop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (this.mp.isPlaying()) this.mp.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (this.mp.isPlaying()) this.mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public Notification getAudioNotfication(String name, int time, boolean pause) {
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        PendingIntent playIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, new Intent(MyService.ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, new Intent(MyService.ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT);

        time = Math.round(time/1000);
        int duration = this.mp.getDuration()/1000;

        RemoteViews mContentView = new RemoteViews(getPackageName(), R.layout.music_notification);
        mContentView.setTextViewText(R.id.music_notification_title, name);
        mContentView.setTextViewText(R.id.music_notification_current_position, FormatHelper.getTimeString(time));
        mContentView.setInt(R.id.music_notification_progress, "setProgress", time);
        mContentView.setInt(R.id.music_notification_progress, "setMax", duration);

        if(pause) {
            mContentView.setImageViewResource(R.id.music_notification_play_image, R.drawable.media_play);
            mContentView.setTextViewText(R.id.music_notification_play_label, "Play");
        } else {
            mContentView.setImageViewResource(R.id.music_notification_play_image, R.drawable.media_pause);
            mContentView.setTextViewText(R.id.music_notification_play_label, "Pause");
        }

        mContentView.setOnClickPendingIntent(R.id.music_notification_play_btn, playIntent);
        mContentView.setOnClickPendingIntent(R.id.music_notification_stop_btn, stopIntent);
        builder.setContent(mContentView);

        builder.setAutoCancel(false);
        builder.setContentTitle("Audio wird abgespielt (" + FormatHelper.getTimeString(time) + ")");
        builder.setContentText("Spiele: " + name);
        builder.setSmallIcon(R.drawable.vision_icon);
        //builder.setContentIntent(resultPendingIntent);
        builder.setOngoing(true);
        Notification notification = builder.build();
        notification.bigContentView = mContentView;
        return notification;
    }

    protected void playAudio(String url) {
        if(this.mp == null) {
            this.mp = new HttpAudioPlayer(this);
            this.mp.setHeaders(this.headers);
            this.mp.setOnTimeChangedListener(new OnTimeChangedListener() {
                @Override
                public void timeChanged(int time) {
                    updateAudioNotification(time);
                }
            });

            this.mp.setOnEndedListener(new OnEndedListener() {
                @Override
                public void onEnded() {
                    mp.stop();
                    stopForeground(true);
                    notificationManager.cancel(musicId);
                }
            });
        }

        this.mp.stop();

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // could not get audio focus.
            this.mp.setUri(url);

            setForeground();
        }
    }

    protected void updateAudioNotification(int time) {
        Notification myNotication = getAudioNotfication(this.mp.getName(), time, !this.mp.isPlaying());

        if(musicId == 0) {
            musicId = this.id;
            this.id++;
        }

        notificationManager.notify(musicId, myNotication);
    }

    protected void setForeground() {
        Notification notification = getAudioNotfication(this.mp.getName(), this.mp.getCurrentPosition(), false);

        if(musicId == 0) {
            musicId = this.id;
            this.id++;
        }

        notificationManager.notify(musicId, notification);

        startForeground(musicId, notification);
    }

    public void abandonAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }
}
