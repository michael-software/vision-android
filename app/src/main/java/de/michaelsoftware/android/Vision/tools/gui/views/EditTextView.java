package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import java.util.HashMap;

/**
 * Created by Michael on 28.06.2016.
 */
public class EditTextView extends EditText {
    public EditTextView(Context context, HashMap<Object, Object> hashMap) {
        super(context);
        
        this.parseHashMap(hashMap);
    }

    private void parseHashMap(HashMap<Object, Object> hashMap) {
        String valueName = (String) hashMap.get("name");
        String valueType = (String) hashMap.get("type");

        /* SET VALUE */
        String valueValue = "";
        if(hashMap.containsKey("value")) valueValue = (String) hashMap.get("value");
        this.setText(valueValue);

        /* SET FOCUS */
        if(hashMap.containsKey("focus") && hashMap.get("focus") instanceof Boolean && (Boolean) hashMap.get("focus")) {
            this.requestFocus();
        }

        /* SET TYPE */
        if (valueType.equalsIgnoreCase("multiline")) {
            this.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
            this.setSingleLine(false);
            this.setMinLines(3);
        } else {
            this.setSingleLine(true);

            if(valueType.equalsIgnoreCase("password")) {
                this.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }

        /* SET FILTER */
        Object accept = hashMap.get("accept");
        if (accept != null && accept instanceof String) {
            if (((String) accept).equalsIgnoreCase("numbers")) {
                this.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                this.setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));
            }
        }

        /* SET HINT */
        Object hint = hashMap.get("hint");
        if (hint != null && hint instanceof String && !hint.equals("")) {
            this.setHint((String) hint);
        }
    }
}
