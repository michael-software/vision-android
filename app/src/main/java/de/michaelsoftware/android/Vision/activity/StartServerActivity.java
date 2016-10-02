package de.michaelsoftware.android.Vision.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import net.michaelsoftware.android.jui.network.HttpPostJsonHelper;

import java.util.HashMap;

import de.michaelsoftware.android.Vision.tools.SecurityHelper;
import de.michaelsoftware.android.Vision.tools.network.WOLHelper;

public class StartServerActivity extends Activity {
    private String mac;
    private String ip;
    private String wolserver;
    private String authtoken;
    private boolean send = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(de.michaelsoftware.android.Vision.R.layout.activity_start_server);

        Intent intent = getIntent();
        this.mac = intent.getStringExtra("MAC");
        this.ip  = intent.getStringExtra("IP");
        this.wolserver = intent.getStringExtra("WOLSERVER");
        this.authtoken = intent.getStringExtra("AUTHTOKEN");
    }

    public void startServer(View v) {
        if(!this.send && v instanceof ImageView) {
            if(this.wolserver != null && !this.wolserver.equals("")) {
                HttpPostJsonHelper httpPostJsonHelper = new HttpPostJsonHelper();

                HashMap<String, String> postList = new HashMap<>();
                postList.put("authtoken", SecurityHelper.encrypt(authtoken));

                httpPostJsonHelper.setPost(postList);
                httpPostJsonHelper.execute(wolserver);
                httpPostJsonHelper.setOutput(this, "startServerUrl");
            } else {
                WOLHelper wol = new WOLHelper(ip, mac);
                wol.execute();
            }

            this.send = true;

            ((ImageView) v).setImageResource(de.michaelsoftware.android.Vision.R.drawable.shutdown_disabled);
        }

    }

    @SuppressWarnings("unused") // used by invoke from HttpPostJson   TODO
    public void startServerUrl(HashMap hashMap) {
    }

}
