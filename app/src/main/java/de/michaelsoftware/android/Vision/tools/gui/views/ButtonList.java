package de.michaelsoftware.android.Vision.tools.gui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.Logs;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnClickListener;

/**
 * Created by Michael on 13.06.2016.
 */
public class ButtonList extends LinearLayout implements View.OnFocusChangeListener {
    private Activity activity;
    private int maxElements = 1;
    private int backgroundColor;
    private int backgroundColorFocus;
    private int imageWidth = 300;
    private LinearLayout.LayoutParams layoutParams;
    private LinearLayout li;
    private int j = 0;

    public ButtonList(Activity pActivity, HashMap<Object, Object> valueValue) {
        super(pActivity);

        this.activity = pActivity;

        this.setOrientation(LinearLayout.VERTICAL);

        li = new LinearLayout(pActivity);
        li.setOrientation(LinearLayout.HORIZONTAL);
        li.setGravity(Gravity.CENTER_HORIZONTAL);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);

        int activityWidth = pActivity.getWindow().getDecorView().getWidth();
        int activityHeight = pActivity.getWindow().getDecorView().getHeight();

        if (activityWidth < activityHeight) {
            maxElements = FormatHelper.getMaxElements(activityWidth, imageWidth, 20);
        } else {
            maxElements = FormatHelper.getMaxElements(activityHeight, imageWidth, 20);
        }

        if(ThemeUtils.getCurrentTheme() == ThemeUtils.DARK) {
            backgroundColor      = ResourceHelper.getColor(pActivity, R.color.tileBackgroundDark);
            backgroundColorFocus = ResourceHelper.getColor(pActivity, R.color.tileBackgroundDarkFocus);
        } else {
            backgroundColor      = ResourceHelper.getColor(pActivity, R.color.tileBackgroundLight);
            backgroundColorFocus = ResourceHelper.getColor(pActivity, R.color.tileBackgroundLightFocus);
        }

        for (int i = 0; i < valueValue.size(); i++) {
            if(!valueValue.containsKey(i)) continue;

            Object value = valueValue.get(i);

            if(value instanceof HashMap && ((HashMap) value).containsKey("value")) {
                this.insertButton((HashMap) value);
            }

            j++;
        }

        this.addView(li);
    }

    private void insertButton(HashMap value) {
        Object elementValue = value.get("value");
        Object elementClick = value.get("click");
        Logs.d(this, elementValue);

        if(elementValue instanceof HashMap && ((HashMap) elementValue).containsKey(0) && ((HashMap) elementValue).containsKey(1)) {
            Object image = ((HashMap) elementValue).get(0);
            Object text  = ((HashMap) elementValue).get(1);

            if(image instanceof String && text instanceof String) {
                if(j >= maxElements) {
                    this.addView(li);

                    li = new LinearLayout(this.activity);
                    li.setOrientation(LinearLayout.HORIZONTAL);
                    li.setGravity(Gravity.CENTER_HORIZONTAL);

                    j = 0;
                }

                LinearLayout innerLi = new LinearLayout(this.activity);
                innerLi.setOrientation(LinearLayout.VERTICAL);
                innerLi.setPadding(10, 10, 10, 10);

                innerLi.setBackgroundColor(backgroundColor);

                if(elementClick != null && elementClick instanceof String) {
                    innerLi.setOnClickListener(new CustomOnClickListener(this.activity, (String) elementClick));
                }

                if(!image.equals("")) {
                    Bitmap bm = FormatHelper.baseToBitmap((String) image, 300, 300);

                    if(bm != null) {
                        ImageView iv = new ImageView(this.activity);
                        iv.setImageBitmap(bm);
                        int imageHeight = FormatHelper.getNewHeight(imageWidth, bm.getWidth(), bm.getHeight());
                        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(imageWidth, imageHeight);
                        iv.setLayoutParams(parms);
                        innerLi.addView(iv);
                    } else {
                        ImageView iv = new ImageView(this.activity);
                        //iv.setImageBitmap(ResourceHelper.getBitmap(mainActivity, R.drawable.no_image));
                        iv.setImageResource(R.drawable.no_image);
                        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(imageWidth, imageWidth);
                        iv.setLayoutParams(parms);
                        innerLi.addView(iv);
                    }
                } else {
                    ImageView iv = new ImageView(this.activity);
                    //iv.setImageBitmap(ResourceHelper.getBitmap(mainActivity, R.drawable.no_image));
                    iv.setImageResource(R.drawable.no_image);
                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(imageWidth, imageWidth);
                    iv.setLayoutParams(parms);
                    innerLi.addView(iv);
                }


                TextView tv = new FontFitTextView(this.activity);
                tv.setText((String) text);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setTextSize(20);
                tv.setTypeface(null, Typeface.BOLD);
                innerLi.addView(tv);

                innerLi.setFocusable(true);
                innerLi.setClickable(true);

                innerLi.setOnFocusChangeListener(this);

                li.addView(innerLi, layoutParams);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            v.setBackgroundColor(backgroundColorFocus);
        } else {
            v.setBackgroundColor(backgroundColor);
        }
    }
}
