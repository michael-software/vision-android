package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.MyService;
import de.michaelsoftware.android.Vision.activity.MainActivity;
import de.michaelsoftware.android.Vision.tools.FormatHelper;

/**
 * Created by Michael on 06.06.2016.
 */
public class Range extends SeekBar {
    private int min = 0;

    public Range(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if(hashMap.containsKey("min") && FormatHelper.isInt(hashMap.get("min")) ) {
            this.min = FormatHelper.getInt(hashMap.get("min") ,0);
        }

        if(hashMap.containsKey("value") && FormatHelper.isInt(hashMap.get("value")) ) {
            this.setProgress(FormatHelper.getInt(hashMap.get("value") ,0) - this.min);
        }

        if(hashMap.containsKey("max") && FormatHelper.isInt(hashMap.get("max")) ) {
            this.setMax(FormatHelper.getInt(hashMap.get("max") ,0) - this.min);
        }

        if(context instanceof MainActivity && hashMap.containsKey("change") && hashMap.get("change") instanceof String) {
            final String change = (String) hashMap.get("change");

            this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    MainActivity main = ((MainActivity) seekBar.getContext());

                    Bundle bundle = new Bundle();
                    bundle.putString("value", progress+"");
                    bundle.putString("authtoken", main.getLoginHelper().getAuthtoken());
                    bundle.putString("action", change);
                    bundle.putString("plugin", main.getCurrentName());

                    main.sendToService(bundle, MyService.MSG_ACTION_SEND);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            this.setOnTouchListener(new ListView.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    int action = event.getAction();
                    switch (action)
                    {
                        case MotionEvent.ACTION_DOWN:
                            // Disallow Drawer to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;

                        case MotionEvent.ACTION_UP:
                            // Allow Drawer to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }

                    // Handle seekbar touch events.
                    v.onTouchEvent(event);
                    return true;
                }
            });
        }
    }

    @Override
    public int getProgress() {
        int progress = super.getProgress();
        return progress + this.min;
    }
}
