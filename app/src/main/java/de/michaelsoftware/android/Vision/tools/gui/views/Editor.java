package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.views.JuiView;

import java.util.HashMap;

/**
 * Created by Michael on 07.09.2016.
 */
public class Editor extends JuiView {
    private HashMap<Object, Object> properties;
    private String value;
    private String name;

    public Editor(Context activity) {
        super(activity);
    }

    public Editor(Context activity, HashMap<Object, Object> hashMap) {
        super(activity);

        if (Tools.isString(hashMap.get("name"))) {
            this.setName((String) hashMap.get("name"));

            if (Tools.isString(hashMap.get("value"))) {
                this.setValue((String) hashMap.get("value"));
            }

            this.properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {

        if(!Tools.empty(this.name)) {
            View editorView = new EditorView(parser.getActivity());

            if(!Tools.empty(this.value)) {
                ((EditorView) editorView).setValue(this.value);
            }

            parser.registerSubmitElement(name, editorView);

            return JuiParser.addProperties(editorView, this.properties);
        }



        return null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }
}
