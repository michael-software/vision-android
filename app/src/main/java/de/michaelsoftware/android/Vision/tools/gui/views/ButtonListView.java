package de.michaelsoftware.android.Vision.tools.gui.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.ResourceHelper;

/**
 * Created by Michael on 13.06.2016.
 */
public class ButtonListView extends LinearLayout implements View.OnFocusChangeListener {
    private Activity activity;
    private int maxElements = 1;
    private int backgroundColor;
    private int backgroundColorFocus;
    private int imageWidth = 300;
    private LayoutParams layoutParams;
    private LinearLayout li;
    private int j = 0;

    public ButtonListView(JuiParser parser, HashMap<Object, Object> hashMap) {
        super(parser.getActivity());

        this.activity = parser.getActivity();

        this.setOrientation(LinearLayout.VERTICAL);

        li = new LinearLayout(this.activity);
        li.setOrientation(LinearLayout.HORIZONTAL);
        li.setGravity(Gravity.CENTER_HORIZONTAL);

        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);

        int activityWidth = this.activity.getWindow().getDecorView().getWidth();
        int activityHeight = this.activity.getWindow().getDecorView().getHeight();

        if (activityWidth < activityHeight) {
            maxElements = Tools.getMaxElements(activityWidth, imageWidth, 20);
        } else {
            maxElements = Tools.getMaxElements(activityHeight, imageWidth, 20);
        }

        backgroundColor      = 0x66000000;
        backgroundColorFocus = ResourceHelper.getColor(this.activity, R.color.tileBackgroundLightFocus);

        if(Tools.isHashmap(hashMap.get("value"))) {
            HashMap<Object, Object> valueValue = (HashMap<Object, Object>) hashMap.get("value");

            for (int i = 0; i < valueValue.size(); i++) {
                if (!valueValue.containsKey(i)) continue;

                Object value = valueValue.get(i);

                if (value instanceof HashMap && ((HashMap) value).containsKey("value")) {
                    this.insertButton((HashMap) value, parser);
                }

                j++;
            }
        }

        this.addView(li);
    }

    private void insertButton(HashMap value, JuiParser parser) {
        Object elementValue = value.get("value");
        Object elementClick = value.get("click");

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

                if(Tools.isString(elementClick)) {
                    innerLi.setOnClickListener(new CustomOnClickListener(parser, (String) elementClick));
                }

                if(!image.equals("")) {
                    Bitmap bm = Tools.baseToBitmap((String) image, 300, 300);

                    if(bm != null) {
                        ImageView iv = new ImageView(this.activity);
                        iv.setImageBitmap(bm);
                        int imageHeight = Tools.getNewHeight(imageWidth, bm.getWidth(), bm.getHeight());
                        LayoutParams parms = new LayoutParams(imageWidth, imageHeight);
                        iv.setLayoutParams(parms);
                        innerLi.addView(iv);
                    } else {
                        ImageView iv = new ImageView(this.activity);
                        //iv.setImageBitmap(ResourceHelper.getBitmap(mainActivity, R.drawable.no_image));
                        iv.setImageResource(R.drawable.no_image);
                        LayoutParams parms = new LayoutParams(imageWidth, imageWidth);
                        iv.setLayoutParams(parms);
                        innerLi.addView(iv);
                    }
                } else {
                    ImageView iv = new ImageView(this.activity);
                    //iv.setImageBitmap(ResourceHelper.getBitmap(mainActivity, R.drawable.no_image));
                    iv.setImageResource(R.drawable.no_image);
                    LayoutParams parms = new LayoutParams(imageWidth, imageWidth);
                    iv.setLayoutParams(parms);
                    innerLi.addView(iv);
                }


                TextView tv = new FontFitTextView(getContext());
                tv.setText((String) text);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setTextSize(20);
                tv.setTypeface(null, Typeface.BOLD);
                tv.setMaxLines(1);
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
