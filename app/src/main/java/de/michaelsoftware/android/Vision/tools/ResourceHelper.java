package de.michaelsoftware.android.Vision.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * Created by Michael on 19.03.2016.
 * helper class for ressources (more will be added)
 */
public class ResourceHelper {

    @SuppressWarnings("deprecation")
    public static int getColor(Context main, int ressourceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return main.getResources().getColor(ressourceId, null);
        } else {
            return main.getResources().getColor(ressourceId);
        }
    }

    public static float getDimen(Context main, int ressourceId) {
        return main.getResources().getDimension(ressourceId);
    }

    public static Bitmap getBitmap(Context main, int resourceId) {
        return BitmapFactory.decodeResource(main.getResources(), resourceId);
    }

    public static String getString(Context context, int resourceId) {
        return context.getResources().getString(resourceId);
    }
}
