package com.example.ayman.wifiscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class WiFiScannerActivity extends AppCompatActivity{

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_scanner);
        buttonScan = findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, R.layout.listv_style, arrayList);
        //adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        listView.setAdapter(adapter);
        scanWifi();


        final Handler handler2 = new Handler();



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String g = Integer.toString(i);
                //Toast.makeText(WiFiScannerActivity.this, g , Toast.LENGTH_LONG).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(WiFiScannerActivity.this, R.style.AlertDialog);
                builder.setTitle("Password");
                final EditText input = new EditText(WiFiScannerActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input, 50, 0, 50, 0);

                final String ss = listView.getItemAtPosition(i).toString();

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {

                        String pass = input.getText().toString();
                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        wifiConfig.SSID = String.format("\"%s\"", ss);
                        wifiConfig.preSharedKey = String.format("\"%s\"",pass);
                        int netId = wifiManager.addNetwork(wifiConfig);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();
                        Toast.makeText(WiFiScannerActivity.this, "Connecting...", Toast.LENGTH_LONG).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run(){

                                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                if(mWifi.isConnected()){
                                    Intent intent = new Intent(WiFiScannerActivity.this, controlling.class);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(WiFiScannerActivity.this, "Error connecting to tag", Toast.LENGTH_LONG).show();
                                    dialogInterface.cancel();

                                }
                            }
                        }, 3000);

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                input.setCompoundDrawablePadding(50);
                builder.show();
            }
        });



    }

    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning Tags ...", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            //Toast.makeText(WiFiScannerActivity.this, "hg", Toast.LENGTH_SHORT).show();

            for (ScanResult scanResult : results) {
                if(scanResult.SSID.startsWith("tag:")) {
                    arrayList.add(scanResult.SSID);
                    String st = scanResult.SSID;
                    //Toast.makeText(WiFiScannerActivity.this, "fgd", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            }
        };
    };
}