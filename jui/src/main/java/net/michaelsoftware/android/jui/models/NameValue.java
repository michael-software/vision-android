package net.michaelsoftware.android.jui.models;

/**
 * Created by Michael on 31.08.2016.
 */
public class NameValue {
    private String value;
    private String name;

    public NameValue(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    //to display object as a string in spinner
    public String toString() {
        return name;
    }

}
