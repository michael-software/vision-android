package net.michaelsoftware.android.jui.models;

/**
 * Created by Michael on 30.08.2016.
 */
public class ActionModel {
    public String name;
    public int parameters = 0;
    public Object object;

    public ActionModel(String name, int numParams, Object object) {
        this.name       = name;
        this.parameters = numParams;
        this.object     = object;
    }
}
