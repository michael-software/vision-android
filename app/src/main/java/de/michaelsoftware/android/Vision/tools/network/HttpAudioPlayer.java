package de.michaelsoftware.android.Vision.tools.network;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.michaelsoftware.android.Vision.service.AudioService;
import de.michaelsoftware.android.Vision.listener.OnEndedListener;
import de.michaelsoftware.android.Vision.listener.OnTimeChangedListener;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.Logs;

public class HttpAudioPlayer {
    private MediaPlayer mp;
    private WifiManager.WifiLock wifiLock;
    private Context o;
    private String lastUrl;
    private android.os.Handler seeker;
    private int currentPosition = 0;
    private boolean startLast = false;

    private final Runnable seekerRunnable = new Runnable() {
        @Override
        public void run() {
            if(currentPosition != mp.getCurrentPosition()) {
                currentPosition = mp.getCurrentPosition();
                timeChanged(currentPosition);
            }

            seeker.postDelayed(this, 1000);
        }
    };

    private List<OnTimeChangedListener> timeChangedListeners = new ArrayList<>();
    private boolean isPrepared = false;
    private String fileName = "";
    private List<OnEndedListener> endedListener = new ArrayList<>();
    private HashMap<String, String> headers = new HashMap<>();

    public HttpAudioPlayer(Context pO) {
        this.mp = new MediaPlayer();
        this.o = pO;

        this.seeker = new android.os.Handler();
    }

    public void setUri(String uri) {
        if(this.mp == null) {
            this.mp = new MediaPlayer();
        }

        this.fileName = this.parseFileName(uri);

        this.isPrepared = false;
        this.lastUrl = uri;

        this.setSeeker();

        try {
            this.mp.reset();
            this.mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mp.setDataSource(o, Uri.parse(uri), this.headers);
            this.mp.prepareAsync();

            this.mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if(extra == MediaPlayer.MEDIA_ERROR_MALFORMED)
                        Logs.v(this, "Media Malformed");
                    if(extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED)
                        Logs.v(this, "Media unsupported");
                    if(extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT)
                        Logs.v(this, "Media unsupported");

                    release();
                    return false;
                }
            });

            this.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    for (OnEndedListener hl : endedListener)
                        hl.onEnded();

                    release();
                }
            });

            this.mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isPrepared = true;
                    start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseFileName(String uri) {
        uri = FormatHelper.getFileName(uri);
        uri = FormatHelper.getBaseName(uri);

        return uri;
    }

    private void start() {
        this.mp.start();

        if(startLast && this.currentPosition < this.mp.getDuration()) {
            this.startLast = false;
            this.mp.seekTo(this.currentPosition);
        }

        this.setSeeker();
    }

    private void setSeeker() {
        this.unsetSeeker();
        this.seeker.postDelayed(seekerRunnable, 1000);
    }

    private void unsetSeeker() {
        this.seeker.removeCallbacks(seekerRunnable);
    }

    @SuppressWarnings("unused") /* It will be useful in later developement ( maybe :-) ) */
    public void play() {
        if(this.mp != null) {
            wifiLock = ((WifiManager) o.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

            wifiLock.acquire();
            start();
            this.mp.setWakeMode(this.o.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        }
    }

    public void pause() {
        if(this.mp != null) {
            this.mp.pause();
            this.release();
        }
    }

    private void release() {
        if(this.wifiLock != null) {
            this.wifiLock.release();
        }

        this.unsetSeeker();
    }

    public void setVolume(float v, float v1) {
        if(this.mp != null) {
            this.mp.setVolume(v, v1);
        }
    }

    public boolean isPlaying() {
        if(this.mp != null) {
            return this.mp.isPlaying();
        }

        return false;
    }

    public void stop() {
        if(this.mp != null) {
            this.mp.stop();
            this.mp.reset();
            this.mp.release();
            this.mp = null;
            this.release();

            if(this.o instanceof AudioService) {
                ((AudioService) this.o).abandonAudioFocus();
            }
        }

        isPrepared = false;
    }

    public void playLast() {
        this.startLast = true;

        if(isPrepared) {
            this.mp.start();
            this.setSeeker();
        } else {
            this.setUri(this.lastUrl);
        }
    }

    private void timeChanged(int time) {
        // Notify everybody that may be interested.
        for (OnTimeChangedListener hl : timeChangedListeners)
            hl.timeChanged(time);
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.timeChangedListeners.add(onTimeChangedListener);
    }

    public String getName() {
        if(this.fileName != null && !this.fileName.equals("")) {
            return this.fileName;
        }

        return this.lastUrl;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getDuration() {
        if(this.mp != null && isPrepared) {
            return this.mp.getDuration();
        }

        return 100;
    }

    public void setOnEndedListener(OnEndedListener onEndedListener) {
        this.endedListener.add(onEndedListener);
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
