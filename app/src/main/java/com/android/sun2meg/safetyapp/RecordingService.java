package com.android.sun2meg.safetyapp;

import static android.graphics.Color.BLUE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class RecordingService extends AppCompatActivity implements LocationListener {

    private FusedLocationProviderClient mFusedLocationClient;
    private ProgressDialog progressDialog;

    SmsManager manager = SmsManager.getDefault();
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private android.widget.Button btnLocation;
//    private TextView txtLocation;
    private android.widget.Button btnContinueLocation;
    private TextView txtContinueLocation;
    private StringBuilder stringBuilder;
    SharedPreferences sharedPreferences;
    private static final long COUNTDOWN_TIME = 5000; // 5 seconds
    private static final long INTERVAL = 100; // Progress update interval
    LocationManager nManager;

    private boolean isContinue = true;
    private boolean isGPS = false;

    TextView msg;
    Button button;
    ProgressBar progressBar;
    CountDownTimer countDownTimer;
    TextView titleText;
    String ENUM, MSG, IMEI;
    private int locationChangeCounter = 0;
    boolean sent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        msg = findViewById(R.id.editMsg);
        button = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        titleText = findViewById(R.id.title_text);

        // Set the maximum value of the progress bar to the countdown time
//        progressBar.setMax((int) COUNTDOWN_TIME);
        titleText.setText("Calculating Your Location ...");
        stringBuilder = new StringBuilder();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2 * 1000); // 2 seconds
        locationRequest.setFastestInterval(1 * 1000); // 1 second
        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        double oldLatitude = wayLatitude;
        double oldLongitude = wayLongitude;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        if (!isContinue) {
//                            txtLocation.setText(String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude));
                        } else {
//                            stringBuilder.append(wayLatitude);
//                            stringBuilder.append("-");
//                            stringBuilder.append(wayLongitude);
//                            stringBuilder.append("\n\n");
//                            txtContinueLocation.setText(stringBuilder.toString());
                            if (wayLatitude != oldLatitude || wayLongitude != oldLongitude) {
                                locationChangeCounter++;
                                updateProgressDialog(locationChangeCounter * 20); // Example progress increment

                                if (locationChangeCounter >= 5) {
                                    startCountdown();
//                                    locationChangeCounter = 0; // Reset the counter
                                }
                            }
                        }
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

        showProgressDialog();
        // Start tracking location
//        startLocationTracking();
        getLocation();
//        startCountdown();

        // Retrieve ENUM, MSG, and IMEI from SharedPreferences
        ENUM = sharedPreferences.getString("ENUM", "NONE");
        MSG = sharedPreferences.getString("MSG", "NONE");
        IMEI = sharedPreferences.getString("IMEI", "NONE");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSOS();
            }
        });

        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
            }
        });

    }
    private void showProgressDialog() {
        progressDialogFragment = ProgressDialogFragment.newInstance(100); // Set max progress to 100
        progressDialogFragment.show(getSupportFragmentManager(), "progress_dialog");
    }

    private void updateProgressDialog(int progress) {
        if (progressDialogFragment != null) {
            progressDialogFragment.updateProgress(progress);
            if (progress >= 100) {
                progressDialogFragment.dismissProgress();
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(RecordingService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RecordingService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RecordingService.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(RecordingService.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (isContinue) {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    } else {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(RecordingService.this, location -> {
                            if (location != null) {
                                wayLatitude = location.getLatitude();
                                wayLongitude = location.getLongitude();
                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        });
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    private ProgressDialogFragment progressDialogFragment;

    private void startCountdownx() {
        progressDialogFragment = ProgressDialogFragment.newInstance((int) COUNTDOWN_TIME);
        progressDialogFragment.show(getSupportFragmentManager(), "progress_dialog");

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long progress = COUNTDOWN_TIME - millisUntilFinished;
                if (progressDialogFragment != null) {
                    progressDialogFragment.updateProgress((int) progress);
                }
            }

            @Override
            public void onFinish() {
                if (progressDialogFragment != null) {
                    progressDialogFragment.dismiss();
                }
                // Handle finish logic
                if ((wayLatitude == 0.0) && (wayLongitude == 0.0)) {
                    Toast.makeText(RecordingService.this, "Try Again!!!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    titleText.setText("Location Found!!!");
                    titleText.setTextColor(BLUE);
                    progressBar.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                    if (!sent){
                        sendSOS();
                    }
                    Toast.makeText(RecordingService.this, "Location sent!!!", Toast.LENGTH_SHORT).show();
//                    msg.setVisibility(View.VISIBLE);
//                    msg.append("Push Button to send Your Location");
                }
            }
        };
        countDownTimer.start();
    }

    private void startCountdown() {
        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending location...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax((int) COUNTDOWN_TIME);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false); // Prevent dismissal by back button
        progressDialog.show();

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate the progress based on the remaining time
                long progress = COUNTDOWN_TIME - millisUntilFinished;
                progressDialog.setProgress((int) progress); // Update the progress dialog
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss(); // Dismiss the progress dialog
                if ((wayLatitude == 0.0) && (wayLongitude == 0.0)) {
                    Toast.makeText(RecordingService.this, "Try Again!!!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    titleText.setText("Location SENT!!!");
                    titleText.setTextColor(Color.BLUE); // Change to Color.BLUE
                    button.setVisibility(View.VISIBLE);
                    if (!sent){
                        sendSOS();
                    }
                    Toast.makeText(RecordingService.this, "Location sent!!!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        countDownTimer.start();
    }

    private void startCountdown0() {
        progressBar.setMax((int) COUNTDOWN_TIME); // Set the maximum progress of the ProgressBar

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate the progress based on the remaining time
                long progress = COUNTDOWN_TIME - millisUntilFinished;
                progressBar.setProgress((int) progress); // Update the progress bar
            }

//            @Override
//            public void onTick(long millisUntilFinished) {
//                progressBar.setProgress((int) (COUNTDOWN_TIME - millisUntilFinished));
//            }
            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                if ((wayLatitude == 0.0) && (wayLongitude == 0.0)) {
                    Toast.makeText(RecordingService.this, "Try Again!!!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    titleText.setText("Location Found!!!");
                    titleText.setTextColor(BLUE);
                    progressBar.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                    if (!sent){
                        sendSOS();
                    }
                    Toast.makeText(RecordingService.this, "Location sent!!!", Toast.LENGTH_SHORT).show();
//                    msg.setVisibility(View.VISIBLE);
//                    msg.append("Push Button to send Your Location");
                }
            }
        };
        countDownTimer.start();
    }


    public void sendSOS() {
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        if ((wayLatitude == 0.0) || (wayLongitude == 0.0)) {
            Toast.makeText(getApplicationContext(), "Zero error Please Wait!!!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String myLocation = "http://maps.google.com/maps?q=loc:" + Double.toString(wayLatitude) + "," + Double.toString(wayLongitude);
            manager.sendTextMessage(ENUM, null, MSG + "\n" + "Click on the link to view my Location:" + myLocation, null, null);
            msg.setText("MESSAGE SENT!!");
            sent = true;
            Toast.makeText(getApplicationContext(), "sent" + Double.toString(wayLatitude), Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(wayLongitude) + "\nLatitude:" + Double.toString(wayLatitude), Toast.LENGTH_SHORT).show();
           stopLocationUpdates();
            finish();
        }
    }


    public void stopLocationUpdates() {
        if (mFusedLocationClient != null && locationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }

    }

    public double getLongitude() {
        return wayLongitude;
    }

    public double getLatitude() {
        return wayLatitude;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check and request location updates
        if (checkLocationPermissions()) {
            getLocation();
        } else {
            requestLocationPermissions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates
        stopLocationUpdates();
    }

    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                AppConstants.LOCATION_REQUEST);
    }

    @Override
    public void onLocationChanged(Location location) {
        wayLatitude =getLatitude();
       wayLongitude =getLongitude();
        Toast.makeText(getApplicationContext(), "sent" , Toast.LENGTH_SHORT).show();
//        locationTrack.stopLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }
}