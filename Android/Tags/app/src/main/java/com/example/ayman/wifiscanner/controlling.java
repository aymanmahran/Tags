package com.example.ayman.wifiscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;
import java.util.List;


public class controlling extends AppCompatActivity {

    WebView myBrowser;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlling);

        myBrowser = (WebView)findViewById(R.id.mybrowser);
        myBrowser.setWebViewClient(new WebViewClient());
        // myBrowser.loadUrl("http://192.168.4.1");

    }

    public void rng(View view){
        Toast.makeText(controlling.this, "Ringing...", Toast.LENGTH_SHORT).show();
        myBrowser.loadUrl("http://192.168.4.1/on");
    }

    public void chng(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(controlling.this, R.style.AlertDialog);
        builder.setTitle("Change Credentials");

        final EditText one = new EditText(this);
        final EditText two = new EditText(this);
        one.setInputType(InputType.TYPE_CLASS_TEXT);
        two.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        one.setHint("SSID");
        two.setHint("Password");

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(one);
        lay.addView(two);
        lay.setPadding(50, 0, 50 , 0);
        builder.setView(lay);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String ssid = one.getText().toString();
                final String pass = two.getText().toString();

                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo info = wifiManager.getConnectionInfo();
                final String sd  = info.getSSID();

                myBrowser.loadUrl("http://192.168.4.1/"+ssid+"/"+pass);

                Toast.makeText(controlling.this, "Changing...", Toast.LENGTH_LONG).show();

                final Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable(){
                    @Override
                    public void run(){

                        Toast.makeText(controlling.this, "Connecting...", Toast.LENGTH_LONG).show();

                    }
                }, 3000);

                final Handler handler = new Handler();

                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){

                String ssd = "tag:" + ssid;
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", ssd);
                wifiConfig.preSharedKey = String.format("\"%s\"",pass);

                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        for( WifiConfiguration i : list ) {
                            if(i.SSID != null && i.SSID.equals("\"" + sd + "\"")) {
                                wifiManager.removeNetwork(i.networkId);
                                break;
                            }
                        }
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();

                        final Handler handler3 = new Handler();
                        handler3.postDelayed(new Runnable(){
                            @Override
                            public void run(){

                                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                if(mWifi.isConnected()){
                                    Toast.makeText(controlling.this, "Connected", Toast.LENGTH_LONG).show();

                                }
                                else{
                                    Toast.makeText(controlling.this, "Error connecting to tag", Toast.LENGTH_LONG).show();

                                }
                            }
                        }, 3000);


                    }
                }, 7000);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }

    public class MyJavaScriptInterface {
        Context mContext;

        MyJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast){
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void openAndroidDialog(){
            AlertDialog.Builder myDialog
                    = new AlertDialog.Builder(controlling.this);
            myDialog.setTitle("DANGER!");
            myDialog.setMessage("You can do what you want!");
            myDialog.setPositiveButton("ON", null);
            myDialog.show();
        }
    }
}
