package net.michaelsoftware.android.jui.network;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import net.michaelsoftware.android.jui.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Michael on 31.08.2016.
 */
public class HttpImageAsync extends AsyncTask<String,Void,Bitmap> {
    private ImageView imageView;
    ProgressDialog loading;
    private int width;
    private int height;
    private Object object;
    private String method;

    File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();
    private ArrayList<String> specialData;


    public HttpImageAsync(ImageView imageView) {
        this.imageView = imageView;
    }

    public HttpImageAsync(Object object, String method) {
        this.object = object;
        this.method = method;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        URL url = null;
        Bitmap image = null;


        String urlToImage = params[0];
        try {
            url = new URL(urlToImage);

            File folder = this.getFolder(url);


            File file = null;
            if(folder != null) {
                file = this.getFile(url);
            }


            if(file != null && file.exists() && file.isFile()) {


                FileInputStream fileInputStream = new FileInputStream(file);

                final BitmapFactory.Options options = new BitmapFactory.Options();

                if (this.height > 0 && this.width > 0) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(fileInputStream, null, options);

                    options.inSampleSize = Tools.calculateInSampleSize(options, this.width, this.height);
                }

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                image = BitmapFactory.decodeStream(new FileInputStream(file), null, options);


            } else {

                URLConnection urlConnection = url.openConnection();
                InputStream inStream = url.openConnection().getInputStream();

                final BitmapFactory.Options options = new BitmapFactory.Options();

                if (this.height > 0 && this.width > 0) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inStream, null, options);

                    options.inSampleSize = Tools.calculateInSampleSize(options, this.width, this.height);
                }

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                image = BitmapFactory.decodeStream(urlConnection.getInputStream(), null, options);


                if (file != null) {
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    InputStream inputStream = urlConnection.getInputStream();
                    int totalSize = urlConnection.getContentLength();
                    int downloadedSize = 0;
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        Log.i("Progress:", "downloadedSize:" + downloadedSize + "totalSize:" + totalSize);
                    }
                    fileOutput.close();
                }

            }

            //image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private File getFolder(URL url) {
        String folder = url.getHost() + "/cache/" + Tools.getPath( url.getPath() );
        File fileFolder = new File(SDCardRoot, folder);
        if(fileFolder.mkdirs()) {
            return fileFolder;
        }

        return null;
    }


    private File getFile(URL url) {
        String file = url.getHost() + "/cache/" + Tools.getPath( url.getPath() ) + Tools.getFilename( url.getFile(), url.getQuery());
        //String filename="downloadedFile.png";
        Log.i("Local filename:",""+file);


        File fileFile = new File(SDCardRoot, file);
        try {
            if(fileFile.exists()) {
                return fileFile;
            } else if(fileFile.createNewFile() && fileFile.exists()) {
                return fileFile;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //loading = ProgressDialog.show(MainActivity.this,"Downloading Image...","Please wait...",true,true);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        //loading.dismiss();

        if(object != null && method != null) {
            try {
                if(this.specialData == null || this.specialData.size() == 0) {
                    Method method = this.object.getClass().getMethod(this.method, Bitmap.class);
                    method.invoke(this.object, bitmap);
                } else {
                    Method method = this.object.getClass().getMethod(this.method, Bitmap.class, ArrayList.class);
                    method.invoke(this.object, bitmap, this.specialData);
                }

                return;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void setSpecialData(ArrayList<String> specialData) {
        this.specialData = specialData;
    }
}