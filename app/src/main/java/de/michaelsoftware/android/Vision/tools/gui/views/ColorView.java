package de.michaelsoftware.android.Vision.tools.gui.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.ThemeUtils;
import de.michaelsoftware.android.Vision.tools.gui.ColorChooserDialog;

/**
 * Created by Michael on 06.08.2016.
 */
public class ColorView extends Button implements View.OnClickListener {
    private int value;

    public ColorView(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        this.setText("Farbe wählen");

        this.setValue(Color.BLACK);

        this.parseHashMap(hashMap);
        this.setOnClickListener(this);
    }

    private void parseHashMap(HashMap<Object, Object> hashMap) {
        if(hashMap.containsKey("value") && hashMap.get("value") instanceof String) {
            String valueString = (String) hashMap.get("value");

            this.setValue( FormatHelper.parseColor(valueString) );
        }
    }

    public String getValue() {
        return FormatHelper.getHex(value);
    }

    public void setValue(int color) {
        this.value = color;

        this.setBackgroundColor(value);
        this.setTextColor(FormatHelper.getContrastColor(value));
    }

    @Override
    public void onClick(View v) {
        ColorChooserDialog chooser = new ColorChooserDialog(getContext());
        chooser.setColor(value);
        chooser.setOnColorChangeListener(new ColorChooserDialog.OnColorChangeListener() {
            @Override
            public void onColorChange(int color) {
                setValue(color);
            }
        });
        chooser.show();
    }
}
