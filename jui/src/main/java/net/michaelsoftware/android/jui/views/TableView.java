package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;

import java.util.HashMap;

/**
 * Creates a TableView with the specified HashMap
 * Created by Michael on 10.05.2016.
 */
public class TableView extends TableLayout {
    private final Context context;

    public TableView(Context context) {
        super(context);

        this.context = context;
    }

    public void parseHashMap(HashMap<Object, Object> hashMap, JuiParser parser) {
        LinearLayout.LayoutParams view_params = new
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(view_params);
        this.setStretchAllColumns(true);
        this.setColumnShrinkable(0, true);
        Object rows = hashMap.get("value");
        Object click = hashMap.get("click");
        Object longclick = hashMap.get("longclick");

        if (rows instanceof HashMap)
            for (int j = 0; j < ((HashMap) rows).size(); j++) {
                Object row = ((HashMap) rows).get(j);
                TableRow newRow = new TableRow(this.context);

                if (row instanceof HashMap)
                    for (int k = 0; k < ((HashMap) row).size(); k++) {
                        Object element = ((HashMap) row).get(k);

                        if (element instanceof String) {
                            TextView tv = new TextView(this.context);
                            tv.setText((String) element);
                            newRow.addView(tv);
                        } else if (element instanceof HashMap) {
                            LinearLayout tableLin = parser.parseReturn((HashMap<Object, Object>) element, false);

                            newRow.addView(tableLin);
                        }
                    }

                if (click instanceof HashMap && ((HashMap) click).containsKey(j) && ((HashMap) click).get(j) instanceof String) {
                    String clickString = (String) ((HashMap) click).get(j);
                    newRow.setOnClickListener(new CustomOnClickListener(parser, clickString));
                }

                if (longclick instanceof HashMap && ((HashMap) longclick).containsKey(j) && ((HashMap) longclick).get(j) instanceof String) {
                    String longclickString = (String) ((HashMap) longclick).get(j);
                    newRow.setOnClickListener(new CustomOnClickListener(parser, longclickString));
                }

                this.addView(newRow);
            }
    }
}
