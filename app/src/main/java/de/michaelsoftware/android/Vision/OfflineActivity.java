package de.michaelsoftware.android.Vision;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import de.michaelsoftware.android.Vision.activity.MainActivity;

public class OfflineActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
    }

    public void reconnect(View v) {
        if(this.isNetworkAvailable()) {
            Intent i = new Intent(OfflineActivity.this, MainActivity.class);
            this.finish();  //Kill the activity from which you will go to next activity
            this.startActivity(i);
        } else {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
