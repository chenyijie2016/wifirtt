package com.example.wifiscanjava.scan;

import android.content.Context;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingResult;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

public class RttResultManager {
    public static ArrayList<AP> activateAP = new ArrayList<>();
    public ArrayList<TableRow> rows = new ArrayList<>();

    public void updateAPs(Context context, ArrayList<ScanResult> ftmAPs) {
        for (ScanResult ftmAP : ftmAPs) {
            boolean find = false;
            for (AP ap : activateAP) {
                if (ap.macAddress.equals(ftmAP.BSSID)) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                AP ap = new AP(context, ftmAP.BSSID);
                activateAP.add(ap);
            }
        }
    }

    public void processResult(List<RangingResult> results) {
        rows.clear();
        for (RangingResult result : results) {
            for (AP ap : activateAP) {
                if (ap.macAddress.equals(result.getMacAddress().toString())) {
                    ap.addData(result);
                    rows.add(ap.row);
                }
            }
        }
    }
}
