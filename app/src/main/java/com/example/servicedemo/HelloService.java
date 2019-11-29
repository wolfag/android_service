package com.example.servicedemo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class HelloService extends Service {
    private static final String TAG = "HelloService";
    private static final String CHANNEL_ID = "11111";
    private static final String CHANNEL_NAME = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID).build();
            startForeground(1, notification);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            private long time = 0;

            @Override
            public void run() {

                Log.i(TAG, "Going" + time);
                time++;
                handler.postDelayed(this, 1000);
            }
        }, 1000);

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
    }
}
