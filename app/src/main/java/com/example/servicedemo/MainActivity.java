package com.example.servicedemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

//public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity-";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnNoti = findViewById(R.id.btnNoti);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        btnNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HelloService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        });
    }

//    @Override
//    public void onBeaconServiceConnect() {
//        Log.i(TAG, "onBeaconServiceConnect");
//        Region region = new Region("all", null, null, null);
//        try {
//            beaconManager.startRangingBeaconsInRegion(region);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        beaconManager.addRangeNotifier(this);
//    }
//
//    @Override
//    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
//        for (Beacon beacon : collection) {
//            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
//                // This is a Eddystone-UID frame
//                Identifier namespaceId = beacon.getId1();
//                Identifier instanceId = beacon.getId2();
//                Log.d(TAG, "I see a beacon transmitting namespace id: " + namespaceId +
//                        " and instance id: " + instanceId +
//                        " approximately " + beacon.getDistance() + " meters away.");
//
//                // Do we have telemetry data?
//                if (beacon.getExtraDataFields().size() > 0) {
//                    long telemetryVersion = beacon.getExtraDataFields().get(0);
//                    long batteryMilliVolts = beacon.getExtraDataFields().get(1);
//                    long pduCount = beacon.getExtraDataFields().get(3);
//                    long uptime = beacon.getExtraDataFields().get(4);
//
//                    Log.d(TAG, "The above beacon is sending telemetry version " + telemetryVersion +
//                            ", has been up for : " + uptime + " seconds" +
//                            ", has a battery level of " + batteryMilliVolts + " mV" +
//                            ", and has transmitted " + pduCount + " advertisements.");
//
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
//        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
//        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
//        beaconManager.bind(this);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        beaconManager.unbind(this);
//    }
}
