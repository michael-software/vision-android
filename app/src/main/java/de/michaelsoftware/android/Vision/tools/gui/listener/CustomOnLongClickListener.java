package de.michaelsoftware.android.Vision.tools.gui.listener;

import android.view.View;

import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.gui.MethodHelper;

import java.lang.reflect.Method;

/**
 * Created by Michael on 10.01.2016.
 * Used from GUIHelper for managing LongClicks on Views
 */
public class CustomOnLongClickListener implements View.OnLongClickListener {
    Object object;
    String methodName;

    public CustomOnLongClickListener(Object pObject, String pMethodName) {
        object = pObject;
        methodName = pMethodName;
    }

    @Override
    public boolean onLongClick(View v) {
        if(methodName == null || methodName.equals(""))
            return true;

        if(FormatHelper.contains(methodName, MethodHelper.list)) {
            MethodHelper form = new MethodHelper();
            form.call(methodName, object);
        } else {
            Method method;
            try {
                method = object.getClass().getMethod(methodName, View.class);
                method.invoke(object, v);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
