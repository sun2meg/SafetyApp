package com.android.sun2meg.safetyapp;

//import androidx.annotation.NonNull;
import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.location.LocationListener;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class Push extends AppCompatActivity implements LocationListener {



    boolean isRunning = false;

    FusedLocationProviderClient fusedLocationClient;
    SmsManager manager = SmsManager.getDefault();
    String myLocation;
    SharedPreferences sharedPreferences;
    TextView msg;
    String msgText;
    Button button;

    Location location;
    String ENUM;
    String MSG;
    String IMEI;

    LocationTrack locationTrack;
    LocationManager nManager;
    double latitude, longitude;
    private static final int REQUEST_LOCATION = 1;
    MyTelephonyManager myTelephonyManager;
    String  imeiNo;
    private static final long COUNTDOWN_TIME = 5000; // 5 seconds
    private static final long INTERVAL = 100; // Progress update interval

    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    TextView titleText;

//    double longitude;
//    double latitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        msg=findViewById(R.id.editMsg);
        button=findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);

        // Set the maximum value of the progress bar to the countdown time
        progressBar.setMax((int) COUNTDOWN_TIME);
        titleText = findViewById(R.id.title_text);
        titleText.setText("Calculating Your Location ...");

        startCountdown();
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//        init();

//////////////////////////////////////////////////////////////////////////////////////////////
        nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!nManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {


            locationTrack = new LocationTrack(Push.this);


            if (locationTrack.canGetLocation()) {


                longitude = locationTrack.getLongitude();
                latitude = locationTrack.getLatitude();


            } else {
                myLocation = "Unable to Find Location :(";
                locationTrack.showSettingsAlert();
            }


        }
        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        ENUM = sharedPreferences.getString("ENUM","NONE");
        MSG = sharedPreferences.getString("MSG","NONE");
        IMEI = sharedPreferences.getString("IMEI","NONE");
//        msg.append("\n"+MSG+"\n"+"Click on the link to view my Location:");
/////////////////////////////////////////////////////////////////////////////////////////////////////
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                sendsos();
            }
        });

        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//            sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
//             ENUM = sharedPreferences.getString("ENUM","NONE");
//             MSG = sharedPreferences.getString("MSG","NONE");
//             IMEI = sharedPreferences.getString("IMEI","NONE");
//        Toast.makeText(this, imeiNo, Toast.LENGTH_SHORT).show();

    }
    private void startCountdown() {
//        locationTrack.stopListener();
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the progress bar with the remaining time
                progressBar.setProgress((int) (COUNTDOWN_TIME - millisUntilFinished));

            }

            @Override
            public void onFinish() {
                if( (latitude == 0.0)  && (longitude ==0.0) ){
                    Toast.makeText(Push.this, "Try Again!!!", Toast.LENGTH_SHORT).show();
                    finish();

                } else{
                    // Hide the progress bar
//
//                    Toast.makeText(Push.this, "Location Found!!!", Toast.LENGTH_SHORT).show();
                    titleText.setText("Location Found!!!");
                    titleText.setTextColor(BLUE);
                    progressBar.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);

//                    msg.setVisibility(View.VISIBLE);

                    // Display the "Finish" toast message
//                    msg.append("\n"+MSG+"\n"+"Click on the link to view my Location:");
                    msg.append("Push Button to send Your Location");
                }
            }
        };

        // Start the countdown timer
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel the countdown timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    public void sendsos(){
//    init();
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);


        if ((latitude == 0.0)  || (longitude ==0.0)){
            Toast.makeText(getApplicationContext(), "Zero error Please Wait!!!", Toast.LENGTH_SHORT).show();
//                                getLocation();
            finish();
        } else{
            myLocation = "http://maps.google.com/maps?q=loc:" + Double.toString(latitude) + "," + Double.toString(longitude);
            manager.sendTextMessage(ENUM,null,MSG+"\n"+"Click on the link to view my Location:"+myLocation,null,null);
//            manager.sendTextMessage(ENUM,null,MSG+"\n"+"Click on the link to view my Location:"+myLocation+" track me this IMEI No: "+IMEI,null,null);
            msg.setText("MESSAGE SENT!!");

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
            locationTrack.stopListener();
            finish();

        }


    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
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

    @Override
    protected void onResume() {
        super.onResume();
//        locationTrack.requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationTrack.removeLocationUpdates();
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {

        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        locationTrack.updateLocation(location);
        latitude=locationTrack.getLatitude();
        longitude=locationTrack.getLatitude();
    }


}


//package com.android.sun2meg.safetyapp;
//
////import androidx.annotation.NonNull;
//import static android.graphics.Color.BLUE;
//import static android.graphics.Color.GREEN;
//import static android.graphics.Color.RED;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import android.location.LocationListener;
//import android.os.CountDownTimer;
//import android.provider.Settings;
//import android.Manifest;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.location.LocationManager;
//import android.media.AudioManager;
//import android.media.ToneGenerator;
//import android.os.Bundle;
//import android.telephony.SmsManager;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
////import com.github.tbouron.shakedetector.library.ShakeDetector;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//
//public class Push extends AppCompatActivity implements LocationListener {
//    boolean isRunning = false;
//    FusedLocationProviderClient fusedLocationClient;
//    SmsManager manager = SmsManager.getDefault();
//    String myLocation;
//    SharedPreferences sharedPreferences;
//    TextView msg;
//    String msgText;
//    Button button;
//
//    Location location;
//    String ENUM;
//    String MSG;
//    String IMEI;
//
//    LocationTrack locationTrack;
//    LocationManager nManager;
//    double latitude, longitude;
//    private static final int REQUEST_LOCATION = 1;
//    MyTelephonyManager myTelephonyManager;
//    String  imeiNo;
//    private static final long COUNTDOWN_TIME = 5000; // 5 seconds
//    private static final long INTERVAL = 100; // Progress update interval
//
//    private ProgressBar progressBar;
//    private CountDownTimer countDownTimer;
//    TextView titleText;
//
////    double longitude;
////    double latitude;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_push);
//        nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (!nManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            OnGPS();
//        } else {
//            locationTrack = new LocationTrack(Push.this);
//            if (locationTrack.canGetLocation()) {
//                longitude = locationTrack.getLongitude();
//                latitude = locationTrack.getLatitude();
//            } else {
//                myLocation = "Unable to Find Location :(";
//                locationTrack.showSettingsAlert();
//            }
//        }
//        msg=findViewById(R.id.editMsg);
//        button=findViewById(R.id.button);
//        progressBar = findViewById(R.id.progressBar);
//
//        // Set the maximum value of the progress bar to the countdown time
//        progressBar.setMax((int) COUNTDOWN_TIME);
//        titleText = findViewById(R.id.title_text);
//        titleText.setText("Calculating Your Location ...");
//        ActivityCompat.requestPermissions( this,
//                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//        startCountdown();
//
////////////////////////////////////////////////////////////////////////////////////////////////
//
//        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
//        ENUM = sharedPreferences.getString("ENUM","NONE");
//        MSG = sharedPreferences.getString("MSG","NONE");
//        IMEI = sharedPreferences.getString("IMEI","NONE");
////        msg.append("\n"+MSG+"\n"+"Click on the link to view my Location:");
///////////////////////////////////////////////////////////////////////////////////////////////////////
//        button.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                sendsos();
//            }
//        });
//
//        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
////            sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
////             ENUM = sharedPreferences.getString("ENUM","NONE");
////             MSG = sharedPreferences.getString("MSG","NONE");
////             IMEI = sharedPreferences.getString("IMEI","NONE");
////        Toast.makeText(this, imeiNo, Toast.LENGTH_SHORT).show();
//
//    }
//    private void startCountdown() {
////        locationTrack.stopListener();
//        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, INTERVAL) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                // Update the progress bar with the remaining time
//                progressBar.setProgress((int) (COUNTDOWN_TIME - millisUntilFinished));
//
//            }
//
//            @Override
//            public void onFinish() {
//                if( (latitude == 0.0)  && (longitude ==0.0) ){
//                    Toast.makeText(Push.this, "Try Again!!!", Toast.LENGTH_SHORT).show();
//                    finish();
//
//                } else{
//                    // Hide the progress bar
////
////                    Toast.makeText(Push.this, "Location Found!!!", Toast.LENGTH_SHORT).show();
//                    titleText.setText("Location Found!!!");
//                    titleText.setTextColor(RED);
//                    progressBar.setVisibility(View.GONE);
//                    button.setVisibility(View.VISIBLE);
//
//                    msg.setVisibility(View.VISIBLE);
//
//                    // Display the "Finish" toast message
////                    msg.append("\n"+MSG+"\n"+"Click on the link to view my Location:");
//                    msg.append("Push Button to send Your Location");
//                }
//            }
//        };
//
//        // Start the countdown timer
//        countDownTimer.start();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        // Cancel the countdown timer to avoid memory leaks
//        if (countDownTimer != null) {
//            countDownTimer.cancel();
//        }
//    }
//
//
//    public void sendsos(){
////    init();
//        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//
//
//        if ((latitude == 0.0)  || (longitude ==0.0)){
//            Toast.makeText(getApplicationContext(), "Zero error Please Wait!!!", Toast.LENGTH_SHORT).show();
////                                getLocation();
//            finish();
//        } else{
//            myLocation = "http://maps.google.com/maps?q=loc:" + Double.toString(latitude) + "," + Double.toString(longitude);
//            manager.sendTextMessage(ENUM,null,MSG+"\n"+"Click on the link to view my Location:"+myLocation,null,null);
////            manager.sendTextMessage(ENUM,null,MSG+"\n"+"Click on the link to view my Location:"+myLocation+" track me this IMEI No: "+IMEI,null,null);
//            msg.setText("MESSAGE SENT!!");
//
//            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
//            locationTrack.stopListener();
//            finish();
//
//        }
//
//
//    }
//
//    private void OnGPS() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//            }
//        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        locationTrack.requestLocationUpdates();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        locationTrack.removeLocationUpdates();
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (isTaskRoot()) {
//
//        } else {
//            super.onBackPressed();
//        }
//    }
//    @Override
//    public void onLocationChanged(Location location) {
//        locationTrack.updateLocation(location);
//        latitude=locationTrack.getLatitude();
//        longitude=locationTrack.getLatitude();
//    }
//
//
//}