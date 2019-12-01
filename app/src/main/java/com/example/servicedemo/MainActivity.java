package com.example.servicedemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "service-MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    private TextView txtNamespaceId, txtInstanceId, txtPower;
    private Button btnBGService, btnClearCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVar();

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

        btnBGService.setOnClickListener(new View.OnClickListener() {
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

        btnClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelloService.eddystoneFounds.clear();
            }
        });

        setData();
    }

    private void setData() {
        Intent intent = getIntent();

        String beaconId = intent.getStringExtra(HelloService.BEACON_ID_TRANSFER);

        Log.d(TAG, intent.getExtras().toString());

        if (beaconId != null) {
            Log.d(TAG, beaconId);
            Beacon beacon = HelloService.eddystoneFounds.get(beaconId);
            if (beacon != null) {
                txtNamespaceId.setText(beacon.getId1().toString());
                txtInstanceId.setText(beacon.getId2().toString());
                txtPower.setText(beacon.getTxPower() + "");
            }
        }
    }

    private void setVar() {
        btnBGService = findViewById(R.id.btnBGService);
        btnClearCache = findViewById(R.id.btnClearCache);

        txtInstanceId = findViewById(R.id.txtInstanceId);
        txtNamespaceId = findViewById(R.id.txtNamespaceId);
        txtPower = findViewById(R.id.txtPower);
    }

}
