package com.zebra.es.iot.android_wifi_scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity{

  private WifiManager wifiManager;
  private ListView listView;
  private Button buttonScan;
  private int size = 0;
  private List<ScanResult> results;
  private ArrayList<String> arrayList = new ArrayList<>();
  private ArrayAdapter adapter;
  private GpsTracker gpsTracker;
  private String strAppVersion = "AndroidApp.1.0.0";
  double latitude = 0;
  double longitude = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
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

    adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
    listView.setAdapter(adapter);
    scanWifi();
  }

  public void getLocation(){
    gpsTracker = new GpsTracker(MainActivity.this);
    if(gpsTracker.canGetLocation()){
      latitude = gpsTracker.getLatitude();
      longitude = gpsTracker.getLongitude();
    }else{
      gpsTracker.showSettingsAlert();
    }
  }

  private void scanWifi() {
    arrayList.clear();
    registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    wifiManager.startScan();
    Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
  }

  public String getCurrentUTC(){
    Date time = Calendar.getInstance().getTime();
    SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
    outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    String formattedDate = outputFmt.format(time);
    return formattedDate;
  }

  BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      results = wifiManager.getScanResults();
      unregisterReceiver(this);
      latitude = 0;
      longitude = 0;
      getLocation();
      arrayList.clear();
      String formattedDate = getCurrentUTC();
      arrayList.add("Device Type: " + android.os.Build.DEVICE + "\nLat: " + String.valueOf(latitude) + "\nLong: " + String.valueOf(longitude)+ "\nVersion: " + strAppVersion);
      for (ScanResult scanResult : results) {
        arrayList.add("BSSID: " + scanResult.BSSID + "\nRecUtc: " + formattedDate + "\nSignalStrength: " + scanResult.level + "\nSignalType: 1"); // + " - " + scanResult.capabilities);
        adapter.notifyDataSetChanged();
      }
    };
  };
}