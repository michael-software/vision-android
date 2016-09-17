package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 29.08.2016.
 */
public class InputColorView extends Button implements View.OnClickListener {
    private int value;

    public InputColorView(Context context) {
        super(context);

        this.setText("Farbe wählen");

        this.setValue(Color.BLACK);

        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        this.setOnClickListener(this);
    }

    public InputColorView(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        this.setText("Farbe wählen");

        this.setValue(Color.BLACK);

        if(Tools.isString(hashMap.get("value"))) {
            String valueString = (String) hashMap.get("value");

            this.setValue( Tools.parseColor(valueString) );
        }

        this.setOnClickListener(this);
    }

    public String getValue() {
        return Tools.getHex(value);
    }

    public void setValue(int color) {
        this.value = color;

        this.setBackgroundColor(value);
        super.setTextColor(Tools.getContrastColor(value));
    }

    public void setValue(String color) {
        this.value = Tools.parseColor(color);

        this.setBackgroundColor(value);
        super.setTextColor(Tools.getContrastColor(value));
    }

    @Override
    public void onClick(View v) {
        InputColorChooserDialog chooser = new InputColorChooserDialog(getContext());
        chooser.setColor(value);
        chooser.setOnColorChangeListener(new InputColorChooserDialog.OnColorChangeListener() {
            @Override
            public void onColorChange(int color) {
                setValue(color);
            }
        });
        chooser.show();
    }



    @Override
    public void setTextColor(int color) {
        //super.setTextColor(color);
    }
}
