package net.michaelsoftware.android.jui.models;

/**
 * Created by Michael on 08.09.2016.
 */
public class ViewModel {
    public Class<?> view;
    public String name;
    public String id;

    public ViewModel(String name, String id, Class<?> view) {
        this.name   = name;
        this.id     = id;
        this.view   = view;
    }
}