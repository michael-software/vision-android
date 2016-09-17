package de.michaelsoftware.android.Vision.tools.gui.listener;

import android.view.View;

import de.michaelsoftware.android.Vision.tools.FormatHelper;
import de.michaelsoftware.android.Vision.tools.gui.MethodHelper;

import java.lang.reflect.Method;

/**
 * Created by Michael on 03.12.2015.
 * Customised onClickListener
 * Free for non-commercial use to modify and use
 */
public class CustomOnClickListener implements View.OnClickListener {
    Object object;
    String methodName;

    public CustomOnClickListener(Object pObject, String pMethodName) {
        object = pObject;
        methodName = pMethodName;
    }

    @Override
    public void onClick(View v) {
        if(methodName != null && !methodName.equals("")) {

            if (FormatHelper.contains(methodName, MethodHelper.list)) {
                MethodHelper form = new MethodHelper();
                form.call(methodName, object);

            } else {
                Method method;

                try {
                    method = object.getClass().getMethod(methodName, View.class);
                    method.invoke(object, v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
