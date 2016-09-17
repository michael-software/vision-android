package net.michaelsoftware.android.jui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;
import net.michaelsoftware.android.jui.listeners.CustomOnLongClickListener;

/**
 * Created by Michael on 07.04.2016.
 */
public class ListView extends LinearLayout {
    private Activity activity;
    private int count = 0;

    public ListView(Context activity) {
        super(activity);
        this.setOrientation(LinearLayout.VERTICAL);
        this.activity = null;
    }

    public ListView(Activity activity) {
        super(activity);
        this.setOrientation(LinearLayout.VERTICAL);
        this.activity = activity;
    }

    public void addItem(JuiParser parser, String value) {
        this.addItem(parser, value, null, null);
    }

    public void addItem(JuiParser parser, String value, String action) {
        this.addItem(parser, value, action, null);
    }

    public void addItem(JuiParser parser, String value, String action, String actionLong) {
        LinearLayout vi = new LinearLayout(this.activity);
        vi.setOrientation(HORIZONTAL);
        vi.setPadding(10, 10, 10, 10);

        if (parser != null && action != null) {
            vi.setOnClickListener(new CustomOnClickListener(parser, action));
            vi.setFocusable(true);
            vi.setClickable(true);
        }

        if (parser != null && actionLong != null) {
            vi.setOnLongClickListener(new CustomOnLongClickListener(parser, actionLong));
            vi.setFocusable(true);
            vi.setClickable(true);
        }

        vi.setOnFocusChangeListener(new OnFocusChangeListener() {
            private Drawable oldBackground;

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    oldBackground = v.getBackground();
                    v.setBackgroundColor(Color.RED);
                } else {

                    v.setBackground(oldBackground);
                }

            }
        });

        if(value instanceof String) {
            TextView tv = new TextView(this.activity);
            tv.setText((String) value);
            //tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, parser.getConfig().activityTextSize);
            tv.setPadding(10, 10, 10, 10);
            vi.addView(tv);
        }



        GradientDrawable gd = new GradientDrawable();

        int color = Tools.adjustAlpha(Color.BLACK, (float) 0.1);
        if (Tools.isEven(count)) {
            color = Tools.adjustAlpha(Color.BLACK, (float) 0.03);
        }
        gd.setColor(color);

        vi.setBackground(gd);

        if(this.activity != null && (action != null || actionLong != null))
            vi.setOnTouchListener(new LinearOnTouchListener(color, parser));

        this.addView(vi);
        this.count++;
    }

    private class LinearOnTouchListener implements OnTouchListener {
        private JuiParser parser;
        private int color;

        public LinearOnTouchListener(int color, JuiParser parser) {
            this.color = color;
            this.parser = parser;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                this.showTouch(v);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                this.hideTouch(v);
            }

            return false;
        }

        public void showTouch(View v) {
            ColorDrawable[] color = {new ColorDrawable(this.color), new ColorDrawable(parser.getConfig().listItemBackgroundColor)};
            TransitionDrawable trans = new TransitionDrawable(color);
            v.setBackground(trans);
            trans.startTransition(400);
        }

        public void hideTouch(View v) {
            if(v.getBackground() instanceof TransitionDrawable) {
                TransitionDrawable trans = (TransitionDrawable) v.getBackground();
                trans.resetTransition();
                trans.reverseTransition(450);
            } else {
                v.setBackgroundColor(this.color);
            }
        }
    }
}
