package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 29.08.2016.
 */
public class Input extends JuiView {

    private EditText edt;
    private InputColorView btnColor;
    private InputDateView btnDate;
    private String preset;
    private String name;
    private HashMap<Object, Object> properties;

    public Input(Context context) {
        super(context);
    }

    public Input(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if (Tools.isString(hashMap.get("name"))) {
            this.setName((String) hashMap.get("name"));
        }

        if (Tools.isString(hashMap.get("preset"))) {
            this.setPreset((String) hashMap.get("preset"));
        } else {
            this.setPreset("input");
        }

        /* SET VALUE */
        if (Tools.isString(hashMap.get("value"))) {
            this.setValue((String) hashMap.get("value"));
        }

        /* SET FOCUS */
        if (Tools.isTrue(hashMap.get("focus"))) {
            this.requestFocus();
        }

        properties = hashMap;

        /* SET HINT */
        Object hint = hashMap.get("hint");
        if (Tools.isString(hint)) {
            this.setHint((String) hint);
        }
    }

    private void setPreset(String preset) {

        if(Tools.isEqual(preset, "color")) {
            this.btnColor = new InputColorView(context);
            this.preset = "color";
        } else if(Tools.isEqual(preset, "date")) {
            this.btnDate = new InputDateView(context);
            this.preset = "date";
        } else {
            this.edt = new EditText(context);
            edt.setSingleLine(true);
        }

        switch (preset) {
            case "textarea":
                edt.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                edt.setSingleLine(false);
                edt.setMinLines(3);
                break;
            case "password":
                edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case "number":
                edt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                edt.setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));
                break;
            default:
                break;
        }
    }

    private void requestFocus() {
        edt.requestFocus();
    }

    @Override
    public View getView(JuiParser parser) {
        if(!Tools.empty(name)) {
            if (Tools.isEqual(preset, "color")) {
                parser.registerSubmitElement(name, btnColor);

                return JuiParser.addProperties(btnColor, properties);
            } else if (Tools.isEqual(preset, "date")) {
                parser.registerSubmitElement(name, btnDate);

                return JuiParser.addProperties(btnDate, properties);
            } else {
                parser.registerSubmitElement(name, edt);

                return parser.addInputProperties( JuiParser.addProperties(edt, properties), properties );
            }
        }

        return null;
    }

    public void setValue(String text) {
        if(Tools.empty(edt)) {
            if(Tools.isEqual(preset, "color")) {
                this.btnColor = new InputColorView(context);
            } else if(Tools.isEqual(preset, "date")) {
                this.btnDate = new InputDateView(context);
            } else {
                this.edt = new EditText(context);
            }
        }

        if(Tools.isEqual(preset, "color")) {
            this.btnColor.setValue(text);
        } else if(Tools.isEqual(preset, "date")) {
            if(Tools.isInt(text))
                this.btnDate.setValue(Tools.getInt(text, 0));
        } else {
            this.edt.setText(text);
        }
    }

    public void setHint(String hint) {
        this.edt.setHint(hint);
    }

    public void setName(String name) {
        this.name = name;
    }
}
