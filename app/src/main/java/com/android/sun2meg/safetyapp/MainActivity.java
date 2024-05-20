package com.android.sun2meg.safetyapp;

import android.app.ActivityManager;
import android.content.IntentSender;
import android.location.Location;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private boolean intervalsEnabled = false;
    private static final String MENU_ID = "interval_2min";
    Button button;
    SmsManager manager = SmsManager.getDefault();
    TelephonyManager telephonyManager;
    ServiceMine s;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
    private Menu menu;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    LocationManager nManager;
    private String[] permissions = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private int frequency = 2; // default frequency of 2 minutes
    private boolean isIterationEnabled = false; // flag to track if iteration is enabled
    ///////////////////////////////
    private static final String PREF_INTERVAL_KEY = "interval_key";
    private static final long DEFAULT_INTERVAL_MILLIS = 2 * 60 * 1000; // Default interval is 2 minutes

    private Runnable runnable = null;
    private long intervalMillis;
    private int itemId;
    //////////////////////////////
    private Handler handler = new Handler();
    private Runnable iterationTask = new Runnable() {
        @Override
        public void run() {
            // Do the iteration task here
            // ...

            // Schedule the next iteration call
            handler.postDelayed(iterationTask, frequency * 1000 * 60);
        }
    };
    LocationTrack locationTrack;

    private ToggleButton toggleButton;
    private RadioGroup frequencyGroup;


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String ENUM = sharedPreferences.getString("ENUM", "NONE");
        if (ENUM.equalsIgnoreCase("NONE")) {
            startActivity(new Intent(this, RegisterNumberActivity.class));
        } else {
            TextView textView = findViewById(R.id.textNum);
            textView.setText("SOS Will Be Sent To\n" + ENUM);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);

        }
        nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        displayLocationSettingsRequest(this);

//        if (!foregroundServiceRunning()) {
//            startService();
//        }

        intervalMillis = getSavedInterval();
        // Schedule SOS with the loaded interval
//        scheduleSOS();

        ///////////////////////////
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!nManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    OnGPS();
                } else {
                    locationTrack = new LocationTrack(MainActivity.this);
                    locationTrack.getLongitude();
                    locationTrack.getLatitude();

//                    Intent sendMsg = new Intent(MainActivity.this, Push.class);
                    Intent sendMsg = new Intent(MainActivity.this, RecordingService.class);
                    startActivity(sendMsg);
                }

            }
        });
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

    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                // Permissions not granted
                // Handle the case where some permissions are not granted
            }
        }
    }


    private ActivityResultLauncher<String[]> multiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {

            for (Map.Entry<String, Boolean> entry : result.entrySet())
                if (!entry.getValue()) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Permission Must Be Granted!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Grant Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            multiplePermissions.launch(new String[]{entry.getKey()});
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }


        }

    });


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
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
    public void PopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.changeNum) {
                    startActivity(new Intent(MainActivity.this, RegisterNumberActivity.class));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {

        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
//        int choosenId = getSavedId();
//        MenuItem selectedduationItem = menu.findItem(choosenId);
//        selectedduationItem.setChecked(true);
//        toggleIntervalSettingsVisibility(menu, intervalsEnabled);
//        scheduleSOS();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.action_enable_intervals:
                intervalsEnabled = !intervalsEnabled;
                item.setChecked(intervalsEnabled);
                toggleIntervalSettingsVisibility(menu, intervalsEnabled);
                if (intervalsEnabled) {
                    Toast.makeText(this, "Intervals Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Intervals Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), IntervalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            case R.id.changeNum:
                startActivity(new Intent(this, RegisterNumberActivity.class));
                return true;
            case R.id.exitapp:
               exit();
               return true;
            case R.id.interval_30sec:
                intervalMillis = 10 * 1000; // 2 minutes in milliseconds
                itemId = item.getItemId();
                showToast(String.valueOf(itemId));
                MenuItem selected30sec = menu.findItem(item.getItemId());
                selected30sec.setChecked(true);
                break;
            case R.id.interval_2min:
                intervalMillis = 2 * 60 * 1000; // 2 minutes in milliseconds
                itemId = id;
                showToast(String.valueOf(itemId));
                item.setChecked(true);
                break;
            case R.id.interval_5min:
                intervalMillis = 5 * 60 * 1000; // 5 minutes in milliseconds
                itemId = id;
                item.setChecked(true);
                break;
            case R.id.interval_10min:
                intervalMillis = 10 * 60 * 1000; // 10 minutes in milliseconds
                itemId = id;
                item.setChecked(true);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        // Save the selected interval
        saveInterval(intervalMillis, itemId);
        updateCheckableState(item);
//        scheduleSOS();
        return true;
    }


    public boolean onOptionsItemSelected0(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
           switch (item.getItemId()) {
               case R.id.action_enable_intervals:
                   intervalsEnabled = !intervalsEnabled;
                   item.setChecked(intervalsEnabled);
                   toggleIntervalSettingsVisibility(menu, intervalsEnabled);
                   if (intervalsEnabled) {
                       Toast.makeText(this, "Intervals Enabled", Toast.LENGTH_SHORT).show();
                   } else {
                       Toast.makeText(this, "Intervals Disabled", Toast.LENGTH_SHORT).show();
                   }
                   return true;
           case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), IntervalActivity.class);
                startActivity(intent);
                return true;
            case R.id.changeNum:
                startActivity(new Intent(this, RegisterNumberActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onIntervalSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.interval_2min:
                intervalMillis = 2 * 60 * 1000; // 2 minutes in milliseconds
                itemId = item.getItemId();
                showToast(String.valueOf(itemId));
                MenuItem selected2min = menu.findItem(item.getItemId());
                selected2min.setChecked(true);
                break;
            case R.id.interval_5min:
                intervalMillis = 5 * 60 * 1000; // 5 minutes in milliseconds
                itemId = item.getItemId();
                MenuItem selected5min = menu.findItem(item.getItemId());
                selected5min.setChecked(true);
                break;
            case R.id.interval_10min:
                intervalMillis = 10 * 60 * 1000; // 10 minutes in milliseconds
                itemId = item.getItemId();
                MenuItem selected10min = menu.findItem(item.getItemId());
                selected10min.setChecked(true);
                break;
        }
        // Save the selected interval
        saveInterval(intervalMillis,itemId);
        updateCheckableState(item);
        scheduleSOS();
    }
    private void updateCheckableState(MenuItem selectedItem) {
        if (selectedItem.hasSubMenu()) {
            SubMenu subMenu = selectedItem.getSubMenu();
            for (int i = 0; i < subMenu.size(); i++) {
                MenuItem item = subMenu.getItem(i);
                item.setChecked(item.getItemId() == selectedItem.getItemId());
            }
        }
    }
    public void toggleIntervalsEnabled(MenuItem item) {
        item.setChecked(!item.isChecked());
        intervalsEnabled = item.isChecked();
        toggleIntervalSettingsVisibility(intervalsEnabled);
        if (intervalsEnabled) {
            Toast.makeText(this, "Realtime Updates Enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Realtime Updates Disabled", Toast.LENGTH_SHORT).show();
        }
    }
    private void toggleIntervalSettingsVisibility(boolean visible) {
        MenuItem enableIntervalsItem = menu.findItem(R.id.action_enable_intervals);
        if (enableIntervalsItem != null && enableIntervalsItem.hasSubMenu()) {
            SubMenu intervalsSubMenu = enableIntervalsItem.getSubMenu();
            if (intervalsSubMenu != null) {
                MenuItem intervalItem = intervalsSubMenu.findItem(R.id.action_time);
                if (intervalItem != null) {
                    intervalItem.getSubMenu().setGroupVisible(R.id.interval_group, visible);
                    // Additionally, set the visibility of the parent menu item
                    intervalsSubMenu.setGroupVisible(R.id.intervals_group, visible);
                }
            }
        }
    }


    private void toggleIntervalSettingsVisibility(Menu menu, boolean visible) {
        if (menu != null) {
            MenuItem enableIntervalsItem = menu.findItem(R.id.action_enable_intervals);
            if (enableIntervalsItem != null && enableIntervalsItem.hasSubMenu()) {
                SubMenu intervalsSubMenu = enableIntervalsItem.getSubMenu();
                if (intervalsSubMenu != null) {
                    MenuItem intervalItem = intervalsSubMenu.findItem(R.id.action_time);
                    if (intervalItem != null) {
                        intervalItem.getSubMenu().setGroupVisible(R.id.interval_group, visible);
                    }
                }
            }
        }
    }


//    private void toggleIntervalSettingsVisibility(Menu menu, boolean visible) {
//        if (menu != null) {
//            MenuItem intervalItem = menu.findItem(R.id.action_time);
//            if (intervalItem != null && intervalItem.hasSubMenu()) {
//                intervalItem.getSubMenu().setGroupVisible(R.id.interval_group, visible);
//            }
//        }
//    }


    // Example method using the menu variable
    private void updateOptionsMenuTitle() {
        if (menu != null) {
            MenuItem enableIntervalsItem = menu.findItem(R.id.action_enable_intervals);
            if (enableIntervalsItem != null) {
                // Update the title of the menu item
                if (intervalsEnabled) {
                    enableIntervalsItem.setTitle("Disable Intervals");
                } else {
                    enableIntervalsItem.setTitle("Enable Intervals");
                }
            }
        }
    }

//    private void updateCheckableState(MenuItem selectedItem) {
//        Menu menu = selectedItem.getSubMenu();
//        for (int i = 0; i < menu.size(); i++) {
//            MenuItem item = menu.getItem(i);
//            item.setChecked(item.getItemId() == selectedItem.getItemId());
//        }
//    }

    private void scheduleSOS() {
        // Cancel any previously scheduled SOS calls
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        // Check if intervals are enabled before scheduling SOS calls
        if (intervalsEnabled) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    sos(); // Call the SOS method
                    handler.postDelayed(this, intervalMillis);
                }
            };
            handler.postDelayed(runnable, intervalMillis);
        }
    }


    private void scheduleSOS0() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                sos(); // Call the SOS method
                handler.postDelayed(this, intervalMillis);
            }
        };
        handler.postDelayed(runnable, intervalMillis);
    }

    private void sos() {
        // Implement your SOS logic here
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        Toast.makeText(getApplicationContext(), "int send sms", Toast.LENGTH_SHORT).show();

    }

    private void saveInterval(long intervalMillis,int id) {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_INTERVAL_KEY, intervalMillis);
        editor.putInt(MENU_ID, id);
        editor.apply();
    }

    private long getSavedInterval() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_INTERVAL_KEY, DEFAULT_INTERVAL_MILLIS);
    }
    private int getSavedId() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getInt(MENU_ID, R.id.interval_5min);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void startIteration(int frequency) {
        // Schedule the first iteration call
        Toast.makeText(MainActivity.this, frequency, Toast.LENGTH_LONG).show();
        handler.postDelayed(iterationTask, frequency * 1000 * 60);
    }

    private void stopIteration() {
        // Cancel the iteration call
        handler.removeCallbacks(iterationTask);
    }
    public void exit(){
        Intent serviceIntent = new Intent(this,LockService.class);
        stopService(serviceIntent);
        finishAffinity();
        System.exit(0);}


}