package com.example.wifiscanjava;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private final static String[] WIFI_PERMISSIONS = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final int REQUEST_CODE = 1;
    private WifiManager wifiManager;
    private WifiRttManager wifiRttManager;
    private ArrayList<ScanResult> ftmAPs = new ArrayList<>() ;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiRttManager = (WifiRttManager) getApplicationContext().getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        TextView textView = findViewById(R.id.textViewFTMStatus);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)) {
            //Toast.makeText(this, "设备具有WIFI RTT支持", Toast.LENGTH_LONG).show();
            textView.setText("设备具有WiFi RTT支持");
        } else {
            //Toast.makeText(this, "设备不具有WIFI RTT支持", Toast.LENGTH_LONG).show();
            textView.setText("设备不具有WiFi RTT支持");
        }
        myRequestPermission();
    }

    private void myRequestPermission() {
        ArrayList<String> permissionList = new ArrayList<>();
        for (String permission : WIFI_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
                Log.d("Permission Denied:", permission);
            }
        }
        if (permissionList.size() > 0) {
            requestPermissions(WIFI_PERMISSIONS, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void onStartButtonClicked(View view) {
//        Toast.makeText(this, "点击", Toast.LENGTH_SHORT).show();
        Button startButton = findViewById(R.id.button_start);
        startButton.setEnabled(false);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {

                startButton.setEnabled(true);
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        } else {
            Toast.makeText(this, "开始扫描WIFI列表", Toast.LENGTH_LONG).show();

        }
    }

    private void scanSuccess() {
        List<ScanResult> scanResults = wifiManager.getScanResults();

        ftmAPs.clear();

        for (ScanResult result : scanResults) {
            if (result.is80211mcResponder()) {
                ftmAPs.add(result);
                Toast.makeText(this, "找到FTM接入点:" + result.SSID, Toast.LENGTH_LONG).show();
                Button rangingButton = findViewById(R.id.buttonRanging);
                rangingButton.setEnabled(true);
            }
            Log.d("WIFI SSID:", result.SSID);
            Log.d("WIFI SCAN:", result.capabilities);
            Log.d("WIFI centerFreq0:", Integer.toString(result.centerFreq0));
            Log.d("WIFI channelWidth:", Integer.toString(result.channelWidth));
        }


    }

    ;

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        Log.d("WIFI SCAN:", "Failure");
    }

    public void onRangingButtonClicked(View view) {
        Button rangingButton = findViewById(R.id.buttonRanging);
        rangingButton.setEnabled(false);
        RangingRequest.Builder builder = new RangingRequest.Builder();
        builder.addAccessPoints(ftmAPs);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        class DirectExecutor implements Executor {
            public void execute(Runnable r) {
                r.run();
            }
        }

        Toast.makeText(this, "开始执行测距请求", Toast.LENGTH_SHORT).show();
        RangingRequest req = builder.build();
        Executor executor = new DirectExecutor();
        wifiRttManager.startRanging(req, executor, new RangingResultCallback() {

            @Override
            public void onRangingFailure(int code) {
                Log.d("onRangingFailure", "Fail in ranging:" + Integer.toString(code));
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "测距请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onRangingResults(List<RangingResult> results) {
                Log.d("onRangingResults", "Success in ranging:");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "测距请求成功", Toast.LENGTH_SHORT).show());
            }
        });
    }

}