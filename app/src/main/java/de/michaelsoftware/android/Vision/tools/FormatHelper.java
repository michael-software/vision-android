package de.michaelsoftware.android.Vision.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Display;
import android.webkit.MimeTypeMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;

/**
 * Created by Michael on 11.12.2015.
 * Class converts Strings and Objects to others and handles spezial String conversations
 * (e.g. http://exmaple.org/test/ to example.org/test   or   http://example.org/ajax.php?plugin=plg_example_plugin&view=home&cmd=&get=view to plg_example_plugin)
 */
public class FormatHelper {
    public static int stringToInt(String in) {
        return stringToInt(in, 0);
    }

    public static int stringToInt(String in, int deflt) {
        int returnInt;

        try {
            returnInt = Integer.parseInt(in);
        } catch(NumberFormatException nfe) {
            returnInt = deflt;
        }

        return returnInt;
    }

    @SuppressWarnings("unused") // need to have a variable
    public static boolean isInt(String in) {
        try {
            int returnInt = Integer.parseInt(in);
            return true;
        } catch(NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isInt(Object in) {
        if(in instanceof Integer)
            return true;

        if(in instanceof String && FormatHelper.isInt((String) in) )
            return true;

        return false;
    }

    public static int getInt (Object in, Integer pDefault) {
        if(in instanceof Integer) {
            return (Integer) in;
        } else if(in instanceof String) {
            return FormatHelper.stringToInt((String) in, pDefault);
        }

        return pDefault;
    }

    public static boolean containsKey(ArrayList pArray, int pKey) {
        try {
            pArray.get(pKey);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        return true;
    }

    public static String getServerName(String server) {
        if(server == null) { return null; }

        server = server.replaceAll("http://(.*?)", "$1");
        server = server.replaceAll("https://(.*?)", "$1");

        String[] serverParams = server.split("/");

        String serverValue = "";

        for (String serverParam : serverParams) {
            if (!serverParam.isEmpty() && !serverParam.contains(".php")
                    && !serverParam.contains(".html") && !serverParam.contains(".css") && !serverParam.contains(".js")) {
                if (!serverValue.equals(""))
                    serverValue += "/";

                serverValue += serverParam;
            } else {
                return serverValue;
            }
        }

        return serverValue;
    }

    public static String getServerUrl(String serverName) {
        String protocol = "http://";

        if(serverName.contains("https://")) {
            protocol = "https://";
        }

        serverName = serverName.replace("http://", "");
        serverName = serverName.replace("https://", "");

        serverName = FormatHelper.encodeURI(serverName, "");

        serverName = protocol + serverName;
        if(!serverName.substring(serverName.length() - 1).equals("/")) {
            serverName = serverName + "/";
        }

        return serverName;
    }

    public static String encodeURI(String pName) {
        return FormatHelper.encodeURI(pName, "");
    }

    public static String encodeURI(String pName, String pDefault) {
        String returnString;

        try {
            returnString = URLEncoder.encode(pName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            returnString = pDefault;
        }

        return returnString;
    }

    public static String decodeUri(String value) {
        return FormatHelper.decodeUri(value, "");
    }

    private static String decodeUri(String value, String pDefault) {
        String returnString;

        try {
            returnString = URLDecoder.decode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            returnString = pDefault;
        }

        return returnString;
    }

    public static String getView(String pUrl) {
        return pUrl.replaceAll("(.*)plugin=([a-zA-Z_0-9]*)(.*)", "$2");
    }

    public static String getPage(String pUrl) {
        return pUrl.replaceAll("(.*)page=([a-zA-Z_0-9]*)(.*)", "$2");
    }

    public static String getCommand(String pUrl) {
        return pUrl.replaceAll("(.*)cmd=([a-zA-Z_0-9]*)(.*)", "$2");
    }

    public static String getFileName(String pUrl) {
        String encodedFileName = pUrl.replaceAll("(.*)getFile(.*)&file=(.*)&(.*)", "$3");

        try {
            return URLDecoder.decode(encodedFileName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getBaseName(String pUrl) {
        String[] elements = pUrl.split("/");

        if(elements.length > 0) {
            pUrl = elements[elements.length - 1];
        }


        String[] elementsFile = pUrl.split("\\.");
        if(elementsFile.length > 0) {
            elementsFile[elementsFile.length - 1] = null;

            StringBuilder builder = new StringBuilder();
            for(String s : elementsFile) {
                if(s != null)
                    builder.append(s).append(".");
            }

            builder.deleteCharAt( builder.length()-1 );

            return builder.toString();
        }

        return pUrl;
    }

    public static boolean isLast(String pString, String pChar) {
        String string = pString.substring(pString.length() - 1);
        return string.equals(pChar);
    }

    public static String removeLast(String pString) {
        if(pString.length() > 0) {
            return pString.substring(0, pString.length() - 1);
        }

        return "";
    }

    public static String getUnspecifiedMimeType(String mime) {
        mime = mime.toLowerCase();

        if( mime.startsWith("image") ) {
            return "image/*";
        } else if( mime.startsWith("video") ) {
            return "video/*";
        } else if( mime.startsWith("audio") ) {
            return "audio/*";
        } else if( mime.startsWith("application") ) {
            return "application/*";
        } else if( mime.startsWith("multipart") ) {
            return "multipart/*";
        } else if( mime.startsWith("message") ) {
            return "message/*";
        } else if( mime.startsWith("model") ) {
            return "model/*";
        }

            return "text/*";
    }

    public static Bitmap baseToBitmap(String image, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        return FormatHelper.baseToBitmap(image, width, height);
    }

    public static Bitmap baseToBitmap(String image, int width, int height) {
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
    }

    public static Point baseToPoint(String image) {
        image = image.substring(image.indexOf(",") + 1);

        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        return new Point(options.outWidth, options.outHeight);
    }

    public static int getNewHeight(int newSide1, int oldSide1, int oldSide2) {
        if(oldSide1 != 0) {
            return (newSide1 / oldSide1) * oldSide2;
        }

        return newSide1;
    }

    public static String trim(String start) {
        start = start.trim();
        return start.replaceAll("[|?*<\">+\\[\\]/']", "");
    }

    @SuppressWarnings("unused") // maybe use it later in developement
    public static int getMaxElements(int sideLength, int imageWidth) {
        return FormatHelper.getMaxElements(sideLength, imageWidth, 0);
    }

    public static int getMaxElements(int sideLength, int imageWidth, int padding) {
        return sideLength / (imageWidth + (padding * 2));
    }

    public static String getExtension(String path) {
        return path.substring(path.lastIndexOf(".")+1, path.length());
    }

    public static String getMimeTypeByExtension(String path) {
        if(!FormatHelper.getExtension(path).equals("")) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(FormatHelper.getExtension(path));
        }

        return "application/octet-stream";
    }

    // Thanks to Andre: http://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean contains(String search, String[] list) {
        for (String item : list) {
            if(search.contains(item)) {
                return true;
            }
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
        return !FormatHelper.isEven(number);
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static boolean isEqual(String string1, String string2) {
        if( string1.toUpperCase().equals(string2.toUpperCase()) ) {
            return true;
        }

        return false;
    }

    public static boolean contains(String string1, String string2) {
        if( string1.toUpperCase().contains(string2.toUpperCase()) ) {
            return true;
        }

        return false;
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

    public static int calculateProportionalResizeHeight(int width, int height, int reqWidth) {
        double returnFloat = ( ((double)height) * ((double)reqWidth) ) / ((double)width);

        return Math.round(Math.round(returnFloat));
    }

    public static int parseColor(String stringColor) {
        if((stringColor.length() == 4 || stringColor.length() == 7) && stringColor.startsWith("#")) {
            return Color.parseColor(stringColor);
        } else if ( (stringColor.length() == 3 || stringColor.length() == 6) && !stringColor.startsWith("#") ) {
            return Color.parseColor("#" + stringColor);
        }

        return Color.BLACK;
    }

    public static String getPluginFromUri(Uri uri) {
        String s = uri.toString();

        if(s.startsWith("vision://openPlugin/")) {
            String[] a = s.replace("vision://openPlugin/", "").split("/");

            if(a.length > 0 && a[0] != null) {
                return a[0];
            }
        }

        return null;
    }

    public static byte[] encodeFormData(String value) {
        try {
            byte[] b = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                b = value.getBytes(StandardCharsets.UTF_8);
            } else {
                b = value.getBytes("UTF-8");
            }

            return b;

        } catch (UnsupportedEncodingException e) {
            return value.getBytes();
        }
    }

    public static String getTimeString(int seconds) {
        int minutes = seconds/60;
        int hours = minutes/60;

        minutes = minutes - hours*60;
        seconds = seconds - hours*3600 - minutes*60;

        String returnString = "";

        if(hours != 0)
            returnString = hours + ":";

        if(minutes != 0) {
            if(hours != 0 && minutes < 10) {
                returnString += "0";
            }

            returnString += minutes + ":";
        } else if(hours != 0) {
            returnString += "00:";
        } else {
            returnString += "0:";
        }

        if(seconds < 10) {
            returnString += "0" + seconds;
        } else {
            returnString += seconds;
        }

        return returnString;
    }

    public static int getPxFromDp(Context context, int marginInt) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginInt, context.getResources().getDisplayMetrics());
    }

    public static String getHex(int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    public static String getHex(int red, int blue, int green) {
        return FormatHelper.getHex(FormatHelper.getColor(red, blue, green));
    }

    public static int getContrastColor(int intColor) {
        int red = Color.red(intColor);
        int green = Color.green(intColor);
        int blue = Color.blue(intColor);

        return FormatHelper.getContrastColor(red, blue, green);
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

    public static String removeJavascript(String value) { /* TODO: long time running */
        value = value.replaceAll("<(.*)script(.*)>(.*)<(.*)/(.*)script(.*)>", "&lt;$1script$2&gt;$3&lt;$4/$5script$6&gt;");

        return value;
    }

    public static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        int actionBarHeight = 0;

        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        }

        return actionBarHeight;
    }
}
