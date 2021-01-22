package com.example.wifiscanjava.scan;

import android.content.Context;
import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import android.util.Log;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.rtt.RangingResult.STATUS_SUCCESS;

public class AP {
    public String macAddress;
    private ArrayList<Integer> distanceData = new ArrayList<>();
    private ArrayList<Integer> rssiData = new ArrayList<>();
    private ArrayList<Long> timestampData = new ArrayList<>();
    public TableRow row;
    private TextView macAddressView;
    private TextView rssiView;
    private TextView distanceView;

    AP(Context context, String macAddress) {
        row = new TableRow(context);
        this.macAddress = macAddress;
        macAddressView = new TextView(context);
        rssiView = new TextView(context);
        distanceView = new TextView(context);
        row.addView(macAddressView);
        row.addView(rssiView);
        row.addView(distanceView);
        macAddressView.setText(macAddress.toString());
    }

    public void addData(RangingResult result) {
        if (result.getStatus() == STATUS_SUCCESS) {
            distanceData.add(result.getDistanceMm());
            rssiData.add(result.getRssi());
            timestampData.add(result.getRangingTimestampMillis());
            rssiView.setText(Integer.toString(result.getRssi()));
            distanceView.setText(Integer.toString(result.getDistanceMm()));
            //Log.d("addData", Integer.toString(distanceData.size()));
        }
        else{
            rssiView.setText("***");
            distanceView.setText("ERROR");
        }
    }
}
