package com.example.servicedemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HelloService extends Service implements BeaconConsumer, RangeNotifier {
    private static final String TAG = "service-HelloService";
    private static final String CHANNEL_ID = "11111";
    private static final String CHANNEL_NAME = "ForegroundServiceChannel";
    private static final String NOTIFICATION_ID_RUNNING = "1";
    private static int NOTIFICATION_ID_FOUND = 100;
    private static final String NOTIFICATION_TITLE_FOUND = "A eddystone-UID found";
    private static final int DELAY_TIME = 300000;

    private BluetoothAdapter bluetoothAdapter;
    private BeaconManager beaconManager;
    public static Map<String, Beacon> eddystoneFounds;
    public static final String BEACON_ID_TRANSFER = "BEACON_ID_TRANSFER";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "STATE_ON");
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        eddystoneFounds = new HashMap<>();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID).build();
            startForeground(Integer.parseInt(NOTIFICATION_ID_RUNNING), notification);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.bind(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {

                Log.i(TAG, "Going" + time);
                enableBluetooth();
                time++;

                handler.postDelayed(this, DELAY_TIME);
            }
        }, DELAY_TIME);

        return Service.START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        unregisterReceiver(broadcastReceiver);
        beaconManager.unbind(this);
    }

    private void enableBluetooth() {
        Log.i(TAG, "enableBluetooth");
        if (bluetoothAdapter == null) {
            Log.i(TAG, "Does not have BT capabilities");
        }
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon : beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();
                Log.d(TAG, "I see a beacon transmitting namespace id: " + namespaceId +
                        " and instance id: " + instanceId +
                        " approximately " + beacon.getDistance() + " meters away.");
                if (!eddystoneFounds.containsKey(beacon.getId1().toString())) {
                    Log.d(TAG, "add " + beacon.getId1());
                    eddystoneFounds.put(beacon.getId1().toString(), beacon);
                    showNotification(getNotification(beacon.getId1().toString(), beacon.getId1()));
                }

            }
        }
    }

    private Notification getNotification(String content, Identifier beaconId1) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(BEACON_ID_TRANSFER, beaconId1.toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Bundle bundle = new Bundle();
        bundle.putString(BEACON_ID_TRANSFER, beaconId1.toString());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle(NOTIFICATION_TITLE_FOUND);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setExtras(bundle);
        return builder.build();
    }

    private void showNotification(Notification notification) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        manager.notify(NOTIFICATION_ID_FOUND++, notification);
    }
}
