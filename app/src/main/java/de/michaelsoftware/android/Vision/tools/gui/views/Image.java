package de.michaelsoftware.android.Vision.tools.gui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnClickListener;
import de.michaelsoftware.android.Vision.tools.gui.listener.CustomOnLongClickListener;

/**
 * Created by Michael on 12.05.2016.
 */
public class Image extends ImageView implements View.OnFocusChangeListener {
    public Image(Context context) {
        super(context);
    }

    public void parseHashMap(HashMap<Object, Object> hm) {
        if(hm.containsKey("value") && hm.get("value") instanceof String && getContext() instanceof Activity) {
            String value = (String) hm.get("value");
            value = value.substring(value.indexOf(",") + 1);

            Bitmap bitmap = FormatHelper.baseToBitmap(value, (Activity) getContext());
            this.setImageBitmap(bitmap);

            Object width = hm.get("width");
            if (width != null && width instanceof String && FormatHelper.isInt(width)) {
                /* TODO */
                /*int heightOld = decodedByte.getHeight();
                int widthOld = decodedByte.getWidth();

                int widthNew = FormatHelper.stringToInt((String) width, 100);
                int heightNew = (widthNew / widthOld) * heightOld;

                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(widthNew, heightNew);
                this.setLayoutParams(parms);*/
            } else {
                /*
                Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screenWidth  = size.y;

                int screenheight = size.y;
                */

                Point point = FormatHelper.baseToPoint(value);
                int newHeight = FormatHelper.calculateProportionalResizeHeight(point.x, point.y, 600);

                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(600, newHeight);
                this.setLayoutParams(parms);
            }

            Object click = hm.get("click");
            if (click != null && click instanceof String) {
                this.setOnClickListener( new CustomOnClickListener(getContext(), (String) click) );
                this.setFocusable(true);
                this.setClickable(true);
            }

            Object longclick = hm.get("longclick");
            if (longclick != null && longclick instanceof String) {
                this.setOnLongClickListener( new CustomOnLongClickListener(getContext(), (String) longclick) );
                this.setFocusable(true);
                this.setClickable(true);
            }

            this.setOnFocusChangeListener(this);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            this.setBackgroundColor(0x66ff0000);
        } else {
            this.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
