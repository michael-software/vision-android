package net.michaelsoftware.android.jui.network;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import net.michaelsoftware.android.jui.Tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Michael on 31.08.2016.
 */
public class HttpImageAsync extends AsyncTask<String,Void,Bitmap> {
    private ImageView imageView;
    ProgressDialog loading;
    private int width;
    private int height;

    public HttpImageAsync(ImageView imageView) {
        this.imageView = imageView;
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

            final BitmapFactory.Options options = new BitmapFactory.Options();

            if(this.height > 0 && this.width > 0) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);

                options.inSampleSize = Tools.calculateInSampleSize(options, this.width, this.height);
            }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            image =  BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);

            //image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
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
        imageView.setImageBitmap(bitmap);
    }
}