package net.michaelsoftware.android.jui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.icu.util.Calendar;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Display;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Michael on 28.08.2016.
 */
public class Tools {
    public static boolean empty(Object object) {
        if(object == null) {
            return true;
        }

        if(object instanceof String && object.equals("")) {
            return true;
        }

        if(object instanceof HashMap && ((HashMap) object).size() <= 0) {
            return true;
        }

        return false;
    }

    public static boolean isHashmap(Object o) {
        return o instanceof HashMap;
    }

    public static boolean isString(Object type) {
        if(type != null && type instanceof String) {
            return true;
        }

        return false;
    }

    public static boolean isEqual(String string1, String string2) {
        if(string1 == null && string2 == null) {
            return true;
        }

        if(string1 == null) {
            return false;
        }

        if(string2 == null) {
            return false;
        }

        if(string1.toUpperCase().equals(string2.toUpperCase())) {
            return true;
        }

        return false;
    }

    public static boolean contains(String string1, String string2) {
        return string1.toUpperCase().contains(string2.toUpperCase());

    }

    public static boolean isTrue(Object focus) {
        if(!Tools.empty(focus) && focus instanceof Boolean && (Boolean) focus) {
            return true;
        }

        return false;
    }

    public static boolean isInt(Object in) {
        if(in instanceof Integer)
            return true;

        if(in instanceof String) {
            try {
                int returnInt = Integer.parseInt((String) in);
                return true;
            } catch(NumberFormatException nfe) {
                return false;
            }
        }

        return false;
    }

    public static int getInt (Object in, Integer pDefault) {
        if(in instanceof Integer) {
            return (Integer) in;
        } else if(in instanceof String) {
            try {
                return Integer.parseInt((String) in);
            } catch(NumberFormatException nfe) {

            }
        }

        return pDefault;
    }

    /* Color */
    public static int parseColor(String stringColor) {
        if((stringColor.length() == 4 || stringColor.length() == 7 || stringColor.length() == 9) && stringColor.startsWith("#")) {
            return Color.parseColor(stringColor);
        } else if ( (stringColor.length() == 3 || stringColor.length() == 6 || stringColor.length() == 8) && !stringColor.startsWith("#") ) {
            return Color.parseColor("#" + stringColor);
        }

        return Color.BLACK;
    }

    public static boolean isBool(Object object) {
        if(object instanceof Boolean) {
            return true;
        }

        if(object instanceof String && ( ((String) object).toUpperCase().equals("TRUE") || ((String) object).toUpperCase().equals("FALSE") )) {
            return true;
        }

        return false;
    }

    public static String getHex(int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    public static String getHex(int red, int blue, int green) {
        return Tools.getHex(Tools.getColor(red, blue, green));
    }

    public static int getContrastColor(int intColor) {
        int red = Color.red(intColor);
        int green = Color.green(intColor);
        int blue = Color.blue(intColor);

        return Tools.getContrastColor(red, blue, green);
    }

    public static int getContrastColor(int red, int blue, int green) {
        double y = (299 * red + 587 * green + 114 * blue) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    public static int getColor(int red, int blue, int green) {
        red = (red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        green = (green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        blue = blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | red | green | blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    public static long getCurrentTimestamp() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            return calendar.getTimeInMillis()/1000;
        } else {
            Date d = new Date();
            return d.getTime()/1000;
        }
    }

    public static int getDate(long timestamp) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp*1000);
            return calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            Date d = new Date(timestamp*1000);
            return d.getDate();
        }
    }

    public static int getMonth(long timestamp) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp*1000);
            return calendar.get(Calendar.MONTH);
        } else {
            Date d = new Date(timestamp*1000);
            return d.getMonth();
        }
    }

    public static int getYear(long timestamp) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp*1000);
            return calendar.get(Calendar.YEAR);
        } else {
            Date d = new Date(timestamp*1000);
            return 1900+d.getYear();
        }
    }

    public static long getTimestamp(int year, int month, int date) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, date);
            return calendar.getTimeInMillis()/1000;
        } else {
            Date d = new Date(year, month, date);
            return d.getTime()/1000;
        }
    }

    public static String escape(String s) {
        return s.replaceAll("/\"/g", "\\\"").replaceAll("/\'/g", "\\\"");
    }

    public static Bitmap base64ToBitmap(String image, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        return Tools.base64ToBitmap(image, width, height);
    }

    public static Bitmap base64ToBitmap(String image, int width, int height) {
        image = image.substring(image.indexOf(",") + 1);

        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        //return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        // Calculate inSampleSize
        options.inSampleSize = Tools.calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static boolean isBase64(String value) {
        if(Tools.isString(value)) {
            int length = value.indexOf(",");

            if(length > 0) {
                value = value.substring(0, length);

                if (value.toLowerCase().contains("base64")) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    public static boolean isHashMap(Object value) {
        if(value != null && value instanceof HashMap && ((HashMap) value).size() > 0) {
            return true;
        }

        return false;
    }

    public static boolean isEven(int number) {
        if(number%2 == 0) {
            return true;
        }

        return false;
    }

    public static boolean isOdd(int number) {
        return !Tools.isEven(number);
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static String removeJavascript(String value) { /* TODO: long time running */
        value = value.replaceAll("<(.*)script(.*)>(.*)<(.*)/(.*)script(.*)>", "&lt;$1script$2&gt;$3&lt;$4/$5script$6&gt;");

        return value;
    }

    public static int getPxFromDp(Context context, int marginInt) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInt, context.getResources().getDisplayMetrics());
    }

    public static Bitmap baseToBitmap(String image, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        return Tools.baseToBitmap(image, width, height);
    }

    public static Bitmap baseToBitmap(String image, int width, int height) {
        try {
            image = image.substring(image.indexOf(",") + 1);

            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

            //return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, width, height);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static int getMaxElements(int sideLength, int imageWidth) {
        return Tools.getMaxElements(sideLength, imageWidth, 0);
    }

    public static int getMaxElements(int sideLength, int imageWidth, int padding) {
        return sideLength / (imageWidth + (padding * 2));
    }

    public static int getNewHeight(int newSide1, int oldSide1, int oldSide2) {
        if(oldSide1 != 0) {
            return (newSide1 / oldSide1) * oldSide2;
        }

        return newSide1;
    }

    public static String getAbsoluteUrl(String url, String domain) {
        if(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
            return url;
        }

        return domain + "/" + url;
    }


    public static String getPath(String path) {
        String[] array = path.split("/");

        String string = "";

        for(int i = 0, x = array.length-1; i < x; i++) {
            if(!array[i].equals(""))
                string += array[i] + "/";
        }

        return string;
    }

    public static String getFilename(String url, String query) {
        String[] array = url.split("/");


        if(query != null) {
            return array[array.length-1] + query;
        }

        return array[array.length-1];
    }
}
