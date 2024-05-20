package com.android.sun2meg.safetyapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

//public class SOSService extends Service {
//
//
//
//    //    LocationTrack locationTrack;
//    LocationManager nManager;
//    String myLocation;
////    double latitude;
////    double longitude;
//    SmsManager manager = SmsManager.getDefault();
//    String ENUM;
//    String MSG;
//
//    SharedPreferences sharedPreferences;
//
//    private Handler handler = new Handler(Looper.getMainLooper());
//    private Runnable runnable;
//    private long intervalMillis = 1000; // Default interval
//    private boolean isRunning = false;
//    private FusedLocationProviderClient fusedLocationClient;
//    private LocationCallback locationCallback;
//    private double latitude;
//    private double longitude;
//    private int locationChangeCounter;
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        startForeground(1, createNotification());
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
//        ENUM = sharedPreferences.getString("ENUM","NONE");
//        MSG = sharedPreferences.getString("MSG","NONE");
//        setupLocationCallback();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent != null) {
//            long newInterval = intent.getLongExtra("intervalMillis", 1000);
//            if (intervalMillis != newInterval) {
//                intervalMillis = newInterval;
//                restartSOS(); // Restart with new interval
//            } else {
//                startSOS(); // Start if not already running
//            }
//        }
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        stopSOS();
//    }
//
//    private void startSOS() {
//        if (!isRunning) {
//            isRunning = true;
//            runnable = new Runnable() {
//                @Override
//                public void run() {
//                    requestLocationUpdates(); // Request location updates
//                    handler.postDelayed(this, intervalMillis);
//                }
//            };
//            handler.postDelayed(runnable, intervalMillis);
//        }
//    }
//
//    private void stopSOS() {
//        if (isRunning) {
//            handler.removeCallbacks(runnable);
//            handler.removeCallbacksAndMessages(null);
//            fusedLocationClient.removeLocationUpdates(locationCallback);
//            isRunning = false;
//        }
//    }
//
//    private void restartSOS() {
//        stopSOS();
//        startSOS();
//    }
//
//    private void requestLocationUpdates() {
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setInterval(intervalMillis);
//        locationRequest.setFastestInterval(intervalMillis / 2);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
//    }
//
//    private void setupLocationCallback() {
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//                    locationChangeCounter++;
//                   if (locationChangeCounter >= 5) {
//
////                        sos(latitude, longitude); // Call the SOS method with location data
//                        sendsos();
////                                    locationChangeCounter = 0; // Reset the counter
//                    }
////
//                }
//            }
//        };
//    }
//    public void sendsos() {
//
//        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//
//
//        if ((latitude == 0.0) || (longitude == 0.0)) {
//            Toast.makeText(getApplicationContext(), "Zero error Please Wait!!!", Toast.LENGTH_SHORT).show();
//
//        } else {
//            myLocation = "http://maps.google.com/maps?q=loc:" + Double.toString(latitude) + "," + Double.toString(longitude);
//            manager.sendTextMessage(ENUM, null, MSG + "\n" + "Click on the link to view my Location:" + myLocation, null, null);
////            manager.sendTextMessage(ENUM,null,MSG+"\n"+"Click on the link to view my Location:"+myLocation+" track me this IMEI No: "+IMEI,null,null);
////            msg.setText("MESSAGE SENT!!");
//            Toast.makeText(getApplicationContext(), "sent", Toast.LENGTH_SHORT).show();
//
//            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
////            sent=true;
////            removeLocationUpdates();
////            finish();
//
//        }
//
//    }
//
//    private void sos(double latitude, double longitude) {
//        // Your SOS method implementation
//        String message = "SOS! Location: " + latitude + ", " + longitude;
//        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//        Toast.makeText(SOSService.this, message, Toast.LENGTH_LONG).show();
//
//        // Optionally update the notification with new location
//        Notification notification = new NotificationCompat.Builder(this, "SOSServiceChannel")
//                .setContentTitle("SOS Service")
//                .setContentText(message)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .build();
//        startForeground(1, notification);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private Notification createNotification() {
//        NotificationChannel channel = new NotificationChannel(
//                "SOSServiceChannel",
//                "SOS Service Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//        );
//        NotificationManager manager = getSystemService(NotificationManager.class);
//        if (manager != null) {
//            manager.createNotificationChannel(channel);
//        }
//
//        return new NotificationCompat.Builder(this, "SOSServiceChannel")
//                .setContentTitle("SOS Service")
//                .setContentText("Sending SOS location updates...")
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .build();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
//

public class SOSService extends Service {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private long intervalMillis = 1000; // Default interval
    private boolean isRunning = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            long newInterval = intent.getLongExtra("intervalMillis", 1000);
            if (intervalMillis != newInterval) {
                intervalMillis = newInterval;
                restartSOS(); // Restart with new interval
            } else {
                startSOS(); // Start if not already running
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSOS();
    }

    private void startSOS() {
        if (!isRunning) {
            isRunning = true;
            runnable = new Runnable() {
                @Override
                public void run() {
                    sendSOSBroadcast();
//                    sos(); // Call the SOS method
                    handler.postDelayed(this, intervalMillis);
                }
            };
            handler.postDelayed(runnable, intervalMillis);
        }
    }
    private void sendSOSBroadcast() {
        Intent intent = new Intent(IntervalActivity.ACTION_SOS);
        sendBroadcast(intent);
    }
    private void stopSOS() {
        if (isRunning) {
            handler.removeCallbacks(runnable);
            handler.removeCallbacksAndMessages(null);
            isRunning = false;
        }
    }

    private void restartSOS() {
        stopSOS();
        startSOS();
    }

    private void sos() {
        // Implement your SOS logic here
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        Toast.makeText(getApplicationContext(), "new send sms", Toast.LENGTH_SHORT).show();
    }

//    private void sos() {
//        // Your SOS method implementation
//        Toast.makeText(SOSService.this, "SOS called", Toast.LENGTH_SHORT).show();
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                "SOSServiceChannel",
                "SOS Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, "SOSServiceChannel")
                .setContentTitle("SOS Service")
                .setContentText("Sending SOS location updates...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
