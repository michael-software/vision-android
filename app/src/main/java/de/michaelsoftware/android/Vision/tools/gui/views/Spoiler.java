package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.tools.gui.GUIHelper;

/**
 * Created by Michael on 31.05.2016.
 */
public class Spoiler extends LinearLayout {
    private LinearLayout innerLinearLayout;
    private Button spoilerButton;
    private boolean isVisible = false;
    private String label = "Spoiler";

    public Spoiler(GUIHelper gui, Context context, String pLabel, HashMap<Object, Object> value) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        this.label = pLabel;

        this.spoilerButton = new Button(context);
        this.spoilerButton.setText(label);
        this.spoilerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVisible) {
                    innerLinearLayout.setVisibility(GONE);

                    String text = label + " anzeigen";
                    spoilerButton.setText(text);

                    isVisible = false;
                } else {
                    innerLinearLayout.setVisibility(VISIBLE);

                    String text = label + " verbergen";
                    spoilerButton.setText(text);

                    isVisible = true;
                }
            }
        });
        this.addView(this.spoilerButton);

        this.innerLinearLayout = new LinearLayout(context);
        this.innerLinearLayout.setOrientation(LinearLayout.VERTICAL);
        this.setVisible(isVisible);
        this.addView(this.innerLinearLayout);


        for (int j = 0; j < value.size(); j++) {
            if (value.containsKey(j)) {
                Object elementValue = value.get(j);

                if (elementValue instanceof HashMap) {
                    HashMap<Object, Object> element = ((HashMap<Object, Object>) elementValue);

                    View view = gui.parseElement(element, true);
                    this.innerLinearLayout.addView(view);
                }
            }
        }
    }

    public void setVisible(boolean bool) {
        this.isVisible = bool;

        if(bool) {
            this.innerLinearLayout.setVisibility(VISIBLE);

            String text = label + " verbergen";
            spoilerButton.setText(text);
        } else {
            this.innerLinearLayout.setVisibility(GONE);

            String text = label + " anzeigen";
            spoilerButton.setText(text);
        }
    }
}
