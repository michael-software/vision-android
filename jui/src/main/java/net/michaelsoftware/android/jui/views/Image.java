package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;
import net.michaelsoftware.android.jui.listeners.CustomOnLongClickListener;
import net.michaelsoftware.android.jui.network.HttpImageAsync;

import java.util.HashMap;

/**
 * Created by Michael on 31.08.2016.
 */
public class Image extends JuiView {
    ImageView imageView = null;
    private String value;
    private String longClick;
    private String click;
    private HashMap<Object, Object> properties;

    public Image(Context context) {
        super(context);
    }

    public Image(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if (Tools.isString(hashMap.get("value"))) {
            this.setValue((String) hashMap.get("value"));

            if (Tools.isString(hashMap.get("click"))) {
                this.setClick((String) hashMap.get("click"));
            }

            if (Tools.isString(hashMap.get("longclick"))) {
                this.setLongClick((String) hashMap.get("longclick"));
            }

            properties = hashMap;
        }
    }


    @Override
    public View getView(JuiParser parser) {

        if(imageView != null && !Tools.empty(value)) {

            imageView.setBackgroundColor(Color.RED);

            if(Tools.isBase64(value)) {
                imageView.setImageBitmap( Tools.base64ToBitmap(value, parser.getActivity()) );
            } else {
                Display display = parser.getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;

                HttpImageAsync httpImageAsync = new HttpImageAsync(imageView);
                httpImageAsync.execute(value);
                httpImageAsync.setWidth(width);
                httpImageAsync.setHeight(height);
                imageView.setImageURI(Uri.parse(value));
            }

            if(Tools.isString(click)) {
                imageView.setOnClickListener(new CustomOnClickListener(parser, click));
            }

            if(Tools.isString(longClick)) {
                imageView.setOnLongClickListener(new CustomOnLongClickListener(parser, longClick));
            }

            imageView.setAdjustViewBounds(true);

            return JuiParser.addProperties(imageView, properties);
        }

        return null;
    }

    public void setValue(String value) {
        if(Tools.isString(value)) {
            if(imageView == null) {
                imageView = new ImageView(context);
            }

            this.value = value;
        }
    }

    public void setLongClick(String longClick) {
        this.longClick = longClick;
    }

    public void setClick(String click) {
        this.click = click;
    }
}
