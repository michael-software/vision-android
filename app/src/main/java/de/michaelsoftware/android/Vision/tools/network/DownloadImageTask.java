package de.michaelsoftware.android.Vision.tools.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.michaelsoftware.android.Vision.R;
import de.michaelsoftware.android.Vision.tools.Logs;

/**
 * Created by Michael on 07.06.2016.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private final Handler timeout;
    ImageView bmImage;
    private ProgressDialog progressDialog;
    private Context context;

    public DownloadImageTask(ImageView bmImage, Context context) {
        this.bmImage = bmImage;
        this.context = context;

        timeout = new android.os.Handler();
        timeout.postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgress();
            }
        }, 500);
    }

    public void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context, R.style.DialogDark);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(context.getResources().getString(R.string.loading));
            progressDialog.setMessage(context.getResources().getString(R.string.loading_info));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(10);
            progressDialog.setProgress(0);

            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    ProgressBar v = (ProgressBar)progressDialog.findViewById(android.R.id.progress);
                    v.getIndeterminateDrawable().setColorFilter(0xFFEF0000,
                            android.graphics.PorterDuff.Mode.MULTIPLY);

                }
            });
        }

        progressDialog.show();
    }

    protected Bitmap doInBackground(String... urls) {
        Bitmap mIcon11 = null;
        try {
            URL url = new URL(urls[0]);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream inp = conn.getInputStream();
                mIcon11 = BitmapFactory.decodeStream(inp);
            }
        } catch (Exception e) {
            Logs.e(this, e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        timeout.removeCallbacksAndMessages(null);

        if(progressDialog != null) {
            progressDialog.dismiss();
        }

        bmImage.setImageBitmap(result);
    }
}