package de.michaelsoftware.android.Vision.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.gui.views.TouchImageView;
import de.michaelsoftware.android.Vision.tools.network.DownloadImageTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MediaActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private TouchImageView mContentViewImage;
    private ImageView mContentViewImageLast;
    private ImageView mContentViewImageNext;
    private VideoView mContentViewVideo;
    private MediaController mediaController = null;
    private ArrayList<String> images = null;
    private int index = 0;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentViewImage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            mContentViewVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final Runnable mAutoHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private HashMap<String, String> headers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_player);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentViewImage = (TouchImageView) findViewById(R.id.fullscreen_content_image);
        mContentViewImageLast = (ImageView) findViewById(R.id.fullscreen_content_image_last);
        mContentViewImageNext = (ImageView) findViewById(R.id.fullscreen_content_image_next);
        mContentViewVideo = (VideoView) findViewById(R.id.fullscreen_content_video);

        mContentViewVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        mContentViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mContentViewImage.setOnFlingListener(new TouchImageView.OnFlingListener() {
            @Override
            public void onFling(int flingDirection) {

                if (flingDirection == TouchImageView.OnFlingListener.FLING_LEFT && !mContentViewImage.isZoomed() ) {
                    nextImage(null);
                } else if(flingDirection == TouchImageView.OnFlingListener.FLING_RIGHT && !mContentViewImage.isZoomed() ) {
                    lastImage(null);
                }
            }
        });


        mContentViewVideo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toggle();
                return false;
            }
        });

        mContentViewVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        if(getIntent().getExtras() != null) {
            final Bundle extras = getIntent().getExtras();

            if(getIntent().hasExtra("Authorization")) {
                headers.put("Authorization", extras.getString("Authorization"));
            }

            if(getIntent().hasExtra("image")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                DownloadImageTask downloadImageTask = new DownloadImageTask(mContentViewImage, this);
                downloadImageTask.setCustomHttpHeaders(this.headers);
                downloadImageTask.execute(extras.getString("image"));

                mContentViewImage.setVisibility(View.VISIBLE);
                mContentViewImageNext.setVisibility(View.GONE);
                mContentViewImageLast.setVisibility(View.GONE);
                mContentViewVideo.setVisibility(View.GONE);
            } else if(getIntent().hasExtra("video")) {

                ConnectivityManager cm =
                        (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

                if (isMobile) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.warning_mobile_title)
                            .setMessage(R.string.warning_mobile_text)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    playVideo(extras.getString("video"));
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(R.drawable.warning)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                    } else {
                        playVideo(extras.getString("video"));
                    }

            } else if(getIntent().hasExtra("images") && getIntent().hasExtra("index")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                this.images = extras.getStringArrayList("images");
                this.index = extras.getInt("index")-1;

                mContentViewImage.setVisibility(View.VISIBLE);
                mContentViewImageNext.setVisibility(View.VISIBLE);
                mContentViewImageLast.setVisibility(View.VISIBLE);
                mContentViewVideo.setVisibility(View.GONE);

                this.nextImage(null);
            } else {
                finish();
            }
        }
    }

    private void playVideo(String video) {
        if(mediaController == null) {
            mediaController = new MediaController(this);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        mediaController.setAnchorView(mContentViewVideo);
        mContentViewVideo.setMediaController(mediaController);

        mContentViewVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Uri vidUri = Uri.parse(video);
        mContentViewVideo.setVideoURI(vidUri);

        mContentViewImage.setVisibility(View.GONE);
        mContentViewImageLast.setVisibility(View.GONE);
        mContentViewImageNext.setVisibility(View.GONE);
        mContentViewVideo.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

        mHideHandler.removeCallbacks(mAutoHideRunnable);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentViewImage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mContentViewVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);

        mHideHandler.removeCallbacks(mAutoHideRunnable);
        mHideHandler.postDelayed(mAutoHideRunnable, UI_ANIMATION_DELAY*10);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onBackPressed() {
        Bundle conData = new Bundle();
        conData.putString("results", "Thanks Thanks");
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void close(View view) {
        if(view.getId() == R.id.dummy_button) {
            finish();
        }
    }

    public void nextImage(View view) {
        if(this.images.size() > this.index+1) {
            index++;

            DownloadImageTask downloadImageTask = new DownloadImageTask(mContentViewImage, this);
            downloadImageTask.setCustomHttpHeaders(this.headers);
            downloadImageTask.execute(this.images.get(index));
        }

        proofDirections();
    }

    public void lastImage(View view) {
        if(this.index-1 >= 0) {
            index--;

            DownloadImageTask downloadImageTask = new DownloadImageTask(mContentViewImage, this);
            downloadImageTask.setCustomHttpHeaders(this.headers);
            downloadImageTask.execute(this.images.get(index));
        }

        proofDirections();
    }

    public void proofDirections() {
        if(this.index-1 < 0) {
            mContentViewImageLast.setVisibility(View.GONE);
            mContentViewImageNext.setVisibility(View.VISIBLE);
        } else if(this.images.size() <= this.index+1) {
            mContentViewImageLast.setVisibility(View.VISIBLE);
            mContentViewImageNext.setVisibility(View.GONE);
        } else {
            mContentViewImageLast.setVisibility(View.VISIBLE);
            mContentViewImageNext.setVisibility(View.VISIBLE);
        }
    }
}
