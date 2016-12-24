package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class Table extends JuiView {
    private HashMap<Object, Object> properties;

    public Table(Context activity) {
        super(activity);
    }

    public Table(Context activity, HashMap<Object, Object> hashMap) {
        super(activity, hashMap);

        this.properties = hashMap;
    }

    @Override
    public View getView(JuiParser parser) {
        TableView tableView = new TableView(parser.getActivity());
        tableView.parseHashMap(properties, parser);

        return JuiParser.addProperties(tableView, properties);
    }
}
