package com.android.sun2meg.safetyapp;

import static android.graphics.Color.GREEN;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.location.LocationRequest;
public class IntervalActivity extends AppCompatActivity implements LocationListener {
    public static final String ACTION_SOS = "com.android.sun2meg.safetyapp.ACTION_SOS";


    private BroadcastReceiver sosReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SOS.equals(intent.getAction())) {
                sos(); // Call the SOS method
            }
        }
    };
    private ToggleButton enableIterationToggle;
    private RadioGroup frequencyGroup;

    private int frequency = 2; // default frequency of 2 minutes
    private boolean isIterationEnabled = false; // flag to track if iteration is enabled
//    private Handler handler = new Handler();
    private Handler handler = new Handler(Looper.getMainLooper());
//    private Runnable updateRunnable;
    private static final int LOCATION_PERMISSION_REQUEST = 123;
    private boolean isSendingUpdates = false;

///////////////////////////////////////////////////
private static final String PREF_INTERVAL_KEY = "interval_key";
    private static final long DEFAULT_INTERVAL_MILLIS = 2 * 60 * 1000; // Default interval is 2 minutes

    private Runnable runnable = null;
    private long intervalMillis;


    //    LocationTrack locationTrack;
    LocationManager nManager;
    String myLocation;
    double latitude;
    double longitude;
    SmsManager manager = SmsManager.getDefault();
    Location location;
    String ENUM;
    String MSG;

    SharedPreferences sharedPreferences;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_TIME = 5000; // 5 seconds
    private static final long INTERVAL = 100; // Progress update interval
    private boolean enableIterationToggleState = false;
    private int selectedFrequencyId = -1;
    private int count;
    Boolean sent =false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @Override
    protected void onResume() {
        super.onResume();
        if (foregroundServiceRunning()) {
            stopSOSService();
        }
        setSchedulingEnabled(false);
//        startService();
        enableIterationToggleState = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getBoolean("ENABLE_ITERATION_TOGGLE_STATE", false);
//        locationTrack.removeLocationUpdates();
        // Restore the state of the enableIterationToggle button
        enableIterationToggle.setChecked(enableIterationToggleState);

        // Handle the state of frequencyGroup based on the enableIterationToggle state
        if (enableIterationToggleState) {
            frequencyGroup.setVisibility(View.VISIBLE);
            // Restore the state of the frequencyGroup
            if (selectedFrequencyId != -1) {
                RadioButton selectedButton = findViewById(selectedFrequencyId);
                if (selectedButton != null) {
                    selectedButton.setChecked(true);
                    intervalMillis = getSavedInterval();
                    startSOSService(intervalMillis);
//                    setSchedulingEnabled(true);
//                    scheduleSOS();
                }
            }
        removeLocationUpdates();
        } else {
            stopSendingUpdates();
            frequencyGroup.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        savePreferences();
        // Save the state of the enableIterationToggle button
        enableIterationToggleState = enableIterationToggle.isChecked();
        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .edit()
                .putBoolean("ENABLE_ITERATION_TOGGLE_STATE", enableIterationToggleState)
                .apply();
        // Save the state of the frequencyGroup
        SharedPreferences.Editor editor = getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit();
        editor.putInt("SELECTED_FREQUENCY_ID", selectedFrequencyId);
        editor.apply();
    }
    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
        }
        return false;
    }

    public void startService() {
        Intent notificationIntent = new Intent(this, com.android.sun2meg.safetyapp.LockService.class);
        notificationIntent.setAction("Start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(notificationIntent);
        } else
            ContextCompat.startForegroundService(this, notificationIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval);
                if (!foregroundServiceRunning()) {
            startService();
        }
        IntentFilter filter = new IntentFilter(ACTION_SOS);
        registerReceiver(sosReceiver, filter);

        enableIterationToggle = findViewById(R.id.enable_iteration_toggle);
        frequencyGroup = findViewById(R.id.frequency_group);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        enableIterationToggleState = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getBoolean("ENABLE_ITERATION_TOGGLE_STATE", false);

        // Initialize the selectedFrequencyId from the stored value
        selectedFrequencyId = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getInt("SELECTED_FREQUENCY_ID", -1);


        // Set the initial state of the enableIterationToggle button
        enableIterationToggle.setChecked(enableIterationToggleState);

        nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        ENUM = sharedPreferences.getString("ENUM","NONE");
        MSG = sharedPreferences.getString("MSG","NONE");
        /////////////////////intvx

        intervalMillis = getSavedInterval();
        // Schedule SOS with the loaded interval
//        scheduleSOS();
        /////////////////////////intv x
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    longitude= location.getLongitude();
                    latitude=location.getLatitude();
                }
            }
        };
///////////////////////////////////////

        handler.postDelayed(runnable, 1000); //duration to start
        //////////////////////////////////////

        enableIterationToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setSchedulingEnabled(false);
                    if (!nManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        OnGPS();
                        enableIterationToggle.setChecked(false);
                    } else {
                             requestLocationUpdates();
//                            longitude = locationTrack.getLongitude();
//                            latitude = locationTrack.getLatitude();
                        frequencyGroup.setVisibility(View.VISIBLE);
                        frequencyGroup.clearCheck(); // Reset the checked state of buttons in frequencyGroup
                    }

//                            enableIterationToggle.setChecked(false);

                } else {
                     setSchedulingEnabled(false);
                     stopSOSService();
                    if (runnable != null) {
                        handler.removeCallbacks(runnable);
                        runnable = null;
                    }
                    if (isSendingUpdates) {
                        stopSendingUpdates();
                    }
//                    stopIteration();
                    frequencyGroup.setVisibility(View.GONE);
                }
            }
        });

        frequencyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectedFrequencyId = checkedId; // Update the selected frequency ID
                switch (checkedId) {
                    case R.id.frequency_1:
                        showMessage("10sec");
//                        handler.removeCallbacks(runnable);
                        setSchedulingEnabled(true);
                        intervalMillis = 10 * 1000;
                        saveInterval(intervalMillis);

                        startSOSService(intervalMillis);
//                       scheduleSOS();
                        break;
                    case R.id.frequency_2:
                        showMessage("2");
                        handler.removeCallbacks(runnable);
                        // Handle frequency 2 selection
                        setSchedulingEnabled(true);
                        intervalMillis = 2 * 60 * 1000;
                        saveInterval(intervalMillis);
                        startSOSService(intervalMillis);
//                        scheduleSOS();
                        break;
                    case R.id.frequency_10:
                        showMessage("10");
                        // Handle frequency 10 selection
                        handler.removeCallbacks(runnable);
                        setSchedulingEnabled(true);
                        intervalMillis = 10 * 60 * 1000; // 10 minutes in milliseconds
                        saveInterval(intervalMillis);
                        startSOSService(intervalMillis);
//                        scheduleSOS();
                        break;
                    case R.id.frequency_30:
                        showMessage("30");
                        handler.removeCallbacks(runnable);
                        setSchedulingEnabled(true);
                        intervalMillis = 30 * 60 * 1000; // 10 minutes in milliseconds
                        saveInterval(intervalMillis);
                        startSOSService(intervalMillis);

//                        scheduleSOS();
                        break;
                }
            }
        });
    }

    private void startSendingUpdates() {
             requestLocationUpdates();
    }

    private void stopSendingUpdates() {
             removeLocationUpdates();
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // Update interval in milliseconds (example: every 1 second)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }
void showMessage(String msg){
    Toast.makeText(IntervalActivity.this, msg, Toast.LENGTH_SHORT).show();

}
    private void removeLocationUpdates() {
        Toast.makeText(IntervalActivity.this, "update removed", Toast.LENGTH_SHORT).show();

        handler.removeCallbacks(runnable);
        fusedLocationClient.removeLocationUpdates(locationCallback);
                Toast.makeText(getApplicationContext(), "update stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            }
        }
    }


    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the progress bar with the remaining time

            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "Finished count", Toast.LENGTH_SHORT).show();
//                sendsos();
            }
        };

        // Start the countdown timer
        countDownTimer.start();
    }


    public void sendsos() {

        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        startCountdown();

        if ((latitude == 0.0) || (longitude == 0.0)) {
            Toast.makeText(getApplicationContext(), "Zero error Please Wait!!!", Toast.LENGTH_SHORT).show();

        } else {
            myLocation = "http://maps.google.com/maps?q=loc:" + Double.toString(latitude) + "," + Double.toString(longitude);
            manager.sendTextMessage(ENUM, null, MSG + "\n" + "Click on the link to view my Location:" + myLocation, null, null);
//            manager.sendTextMessage(ENUM,null,MSG+"\n"+"Click on the link to view my Location:"+myLocation+" track me this IMEI No: "+IMEI,null,null);
//            msg.setText("MESSAGE SENT!!");
            Toast.makeText(getApplicationContext(), "sent", Toast.LENGTH_SHORT).show();

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
            sent=true;
          removeLocationUpdates();
            finish();

        }

    }


    @Override
    public void onLocationChanged(Location location) {
        ++count;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private Thread thread; // Declare the thread variable

    private void scheduleSOStry() {
        // If the previous thread is not null, interrupt it
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null; // Reset the thread reference
        }

        // Create a new thread if scheduling is enabled
        if (isSchedulingEnabled) {
            // Create a new thread
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        sos(); // Call the SOS method
                        try {
                            Thread.sleep(intervalMillis); // Sleep for the interval
                        } catch (InterruptedException e) {
                            // Thread interrupted, exit the loop
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });
            thread.start(); // Start the thread
        } else {
            // Show a toast message if scheduling is not enabled
            Toast.makeText(IntervalActivity.this, "Schedule not enabled", Toast.LENGTH_SHORT).show();
        }
    }


    private void scheduleSOS() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            handler.removeCallbacksAndMessages(null);
        } else
            Toast.makeText(IntervalActivity.this, "runnable null", Toast.LENGTH_SHORT).show();
        if (isSchedulingEnabled) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    sos(); // Call the SOS method
                    handler.postDelayed(this, intervalMillis);
                }
            };
            handler.postDelayed(runnable, intervalMillis);
        } else
            Toast.makeText(IntervalActivity.this, "schedule not enabled", Toast.LENGTH_SHORT).show();

    }

    private void sos() {
        // Implement your SOS logic here
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        Toast.makeText(getApplicationContext(), " Broadcst send sms", Toast.LENGTH_SHORT).show();
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit();
        editor.putBoolean("ENABLE_ITERATION_TOGGLE_STATE", enableIterationToggleState);
        editor.putInt("SELECTED_FREQUENCY_ID", selectedFrequencyId);
        editor.putLong(PREF_INTERVAL_KEY, intervalMillis);
        editor.apply();
    }

    private void saveInterval(long intervalMillis) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_INTERVAL_KEY, intervalMillis);
        editor.apply();
    }

    private long getSavedInterval() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_INTERVAL_KEY, DEFAULT_INTERVAL_MILLIS);
    }
    // Define a boolean flag to control the scheduling
    private boolean isSchedulingEnabled = false;
 // Method to enable/disable SOS scheduling
    private void setSchedulingEnabled(boolean enabled) {
        isSchedulingEnabled = enabled;
        if (!enabled) {
            // If scheduling is disabled, remove any pending SOS calls
            handler.removeCallbacksAndMessages(null);
            Toast.makeText(IntervalActivity.this, "Scheduling disabled", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopSOSService0() {
        Intent intent = new Intent(this, LockService.class);
        stopService(intent);
    }
    private void startSOSService(long interval) {
        Intent intent = new Intent(this, SOSService.class);
        intent.putExtra("intervalMillis", interval);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void stopSOSService() {
        Intent intent = new Intent(this, SOSService.class);
        stopService(intent);
    }

}