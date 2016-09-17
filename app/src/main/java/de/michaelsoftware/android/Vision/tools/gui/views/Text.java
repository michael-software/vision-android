package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.tools.FormatHelper;

/**
 * Created by Michael on 09.05.2016.
 * Custom TextView to use it with the GUI Helper
 */
public class Text extends TextView {

    public Text(Context context) {
        super(context);
        this.setSingleLine(false);
    }

    public Text(Context context, HashMap<Object, Object> hashMap) {
        super(context);
        this.parseHashMap(hashMap);
        this.setSingleLine(false);
    }

    public void setHashMap(HashMap<Object, Object> hm) {
        this.parseHashMap(hm);
    }

    private void parseHashMap(HashMap<Object, Object> hm) {
        if(hm.get("value") instanceof String) {

            String s = (String) hm.get("value");
            s = s.replaceAll("&lt;br /&gt;", "<br />").replaceAll("&lt;br/&gt;", "<br />").replaceAll("&lt;br&gt;", "<br />").replaceAll("<br />", "\n");
            s = s.replaceAll("\n ", "\n").replaceAll(" \n", "\n");
            s = s.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
            this.setText(s);

            LinearLayout.LayoutParams view_params = new
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            this.setLayoutParams(view_params);

            if(hm.containsKey("align") && hm.get("align") != null && hm.get("align") instanceof String) {
                String valueAlign = (String) hm.get("align");

                this.setAlignment(valueAlign);
            }

            if(hm.containsKey("appearance") && hm.get("appearance") != null && hm.get("appearance") instanceof String) {
                String appearance = (String) hm.get("appearance");

                this.setAppearance(appearance);
            }
        }
    }

    public void setAlignment(String alignment) {
        if (FormatHelper.isEqual(alignment, "center")) {
            this.setGravity(Gravity.CENTER);
        } else if (FormatHelper.isEqual(alignment, "right")) {
            this.setGravity(Gravity.END);
        } else if (FormatHelper.isEqual(alignment, "LEFT")) {
            this.setGravity(Gravity.START);
        }
    }

    public void setAppearance(String appearance) {
        if (FormatHelper.contains(appearance, "bold")) {
            this.setTypeface(null, Typeface.BOLD);
        }

        if (FormatHelper.contains(appearance, "italic")) {
            this.setTypeface(null, Typeface.ITALIC);
        }

        if (FormatHelper.contains(appearance, "underline")) {
            this.setPaintFlags(this.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        if (FormatHelper.contains(appearance, "strikethru")) {
            this.setPaintFlags(this.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
}
