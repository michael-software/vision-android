package net.michaelsoftware.android.jui.views;

/**
 * Created by Michael on 29.08.2016.
 */

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 29.08.2016.
 */
public class File extends JuiView {

    private HashMap<Object, Object> properties;
    private FileView file;
    private String name;

    public File(Context context) {
        super(context);
    }

    public File(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if (Tools.isString(hashMap.get("name"))) {
            this.setName((String) hashMap.get("name"));
        }

        this.properties = hashMap;

        file = new FileView(context);
    }

    @Override
    public View getView(JuiParser parser) {
        if(!Tools.empty(name)) {
            parser.registerSubmitElement(name, file);
            return JuiParser.addProperties(file, properties);
        }

        return null;
    }

    public void setName(String name) {
        this.name = name;
    }
}

