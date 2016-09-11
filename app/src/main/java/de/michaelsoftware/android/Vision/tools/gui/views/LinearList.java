package de.michaelsoftware.android.Vision.tools.gui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnClickListener;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnLongClickListener;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;

/**
 * Created by Michael on 07.04.2016.
 */
public class LinearList extends LinearLayout {
    private Activity activity;
    private int count = 0;
    private int textColor = Color.BLACK;

    public LinearList(Context activity) {
        super(activity);
        this.setOrientation(LinearLayout.VERTICAL);
        this.activity = null;
    }

    public LinearList(Activity activity) {
        super(activity);
        this.setOrientation(LinearLayout.VERTICAL);
        this.activity = activity;

        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            textColor = ResourceHelper.getColor(activity, R.color.textColorDark);
        }
    }

    public void addItem(Object value) {
        this.addItem(value, null, null);
    }

    public void addItem(Object value, String action) {
        this.addItem(value, action, null);
    }

    public void addItem(Object value, String action, String actionLong) {
        LinearLayout vi = new LinearLayout(this.activity);
        vi.setOrientation(HORIZONTAL);
        vi.setPadding(10, 10, 10, 10);

        if (this.activity != null && action != null) {
            vi.setOnClickListener(new CustomOnClickListener(this.activity, action));
            vi.setFocusable(true);
            vi.setClickable(true);
        }

        if (this.activity != null && actionLong != null) {
            vi.setOnLongClickListener(new CustomOnLongClickListener(this.activity, actionLong));
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
            tv.setTextColor(this.textColor);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResourceHelper.getDimen(this.activity, R.dimen.activity_text_size));
            tv.setPadding(10, 10, 10, 10);
            vi.addView(tv);
        }

        /*
        ImageView iv = new ImageView(this.activity);
        iv.setImageResource(R.drawable.ic_launcher);
        iv.setPadding(7, 7, 7, 7);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(60, 60);
        iv.setLayoutParams(layoutParams);
        vi.addView(iv);*/

        GradientDrawable gd = new GradientDrawable();

        int color = FormatHelper.adjustAlpha(textColor, (float) 0.1);
        if (FormatHelper.isEven(count)) {
            color = FormatHelper.adjustAlpha(textColor, (float) 0.03);
        }
        gd.setColor(color);

        vi.setBackground(gd);

        if(this.activity != null && (action != null || actionLong != null))
            vi.setOnTouchListener(new LinearOnTouchListener(color));

        this.addView(vi);
        this.count++;
    }

    private class LinearOnTouchListener implements OnTouchListener {
        int color;

        public LinearOnTouchListener(int color) {
            this.color = color;
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
            ColorDrawable[] color = {new ColorDrawable(this.color), new ColorDrawable(ResourceHelper.getColor(activity, R.color.red))};
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
