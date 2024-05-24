package com.android.sun2meg.safetyapp;

//import androidx.annotation.RequiresApi;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class RegisterNumberActivity extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT =3;
    private static final int REQUEST_CONTACTS_PERMISSION = 1;
    private static final int PICK_CONTACT_REQUEST = 2;
    TextInputEditText message;
    TextInputEditText number;

    TelephonyManager telephonyManager;
    String imei;
    String imeiNo;

    private String[] countryArray;

    private EditText phoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);
// Check if the permissions are already granted

        message = findViewById(R.id.messageEdit);
        number = findViewById(R.id.numberEdit);

        imeiNo = "56734";
        // Set up the country spinner


    }

    public void saveNumber(View view) {
        String mgsString = message.getText().toString();
        String phoneNumber = number.getText().toString();
//        String countryCode = getCountryCode();
//        String fullPhoneNumber = countryCode + phoneNumber;
        if (isValidPhoneNumber(phoneNumber)) {
//        if (isValidPhoneNumber(fullPhoneNumber)) {
            // Phone number is valid, display toast message with full phone number
            String message = "Full phone number: " + phoneNumber;
            Toast.makeText(RegisterNumberActivity.this, message, Toast.LENGTH_LONG).show();

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("ENUM", phoneNumber);
            myEdit.putString("MSG", mgsString);
            myEdit.putString("IMEI", imeiNo);
            myEdit.apply();
            Toast.makeText(this, imeiNo, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // Phone number is invalid, display error message
            number.setError("Invalid phone number");
        }
    }
    /////////////////////////

    public void pickContact(View view) {
        Intent in = new Intent (Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult (in, RESULT_PICK_CONTACT);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_CONTACTS},
//                    REQUEST_CONTACTS_PERMISSION);
//        } else {
//            pickContactLauncher.launch(null);
////            launchContactPicker();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Toast.makeText(this, "Failed To pick contact", Toast.LENGTH_SHORT).show();
        }
    }


    private void contactPicked(Intent data) {
        Cursor cursor = null;

        try {
            String phoneNo = null;
            Uri uri = data.getData ();
            cursor = getContentResolver ().query (uri, null, null,null,null);
            cursor.moveToFirst ();
            int phoneIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER);

            phoneNo = cursor.getString (phoneIndex);
            number.setText (phoneNo);
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
    //////////////////////////////////////
    private void launchContactPicker() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
//            handleContactResult(data);
//        }
//    }

    private void handleContactResult(Intent data) {
        Uri contactData = data.getData();
        Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int hasPhoneColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            String hasPhone = cursor.getString(hasPhoneColumnIndex);
            if (hasPhone != null && hasPhone.equals("1")) {
                int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = cursor.getString(phoneNumberIndex);
                // Set the selected phone number to your EditText
                // For example: editText.setText(phoneNumber);
                Toast.makeText(this, "Selected phone number: " + phoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Selected contact doesn't have a phone number", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot pick contact.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ////////////////////////

    // Add method to handle picking a contact from the phone book
    public void pickContact1(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }


    private boolean isValidPhoneNumber(String phoneNumber) {
        // Updated regex to allow optional spaces between digits
        String regex = "^[+]?[0-9 ]{10,15}$";
        // Use trim() to remove leading/trailing spaces before matching
        return phoneNumber.trim().matches(regex);
    }
    private boolean isValidPhoneNumber0(String phoneNumber) {
        String regex = "^[+]?[0-9]{10,13}$";
        return phoneNumber.matches(regex);
    }

    // Helper method to extract phone number from contact URI
    private String getPhoneNumberFromContactUri(Uri contactUri) {
        String phoneNumber = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int phoneColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                phoneNumber = cursor.getString(phoneColumnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return phoneNumber;
    }

    public void saveNumber0(View view) {
//            imei = telephonyManager.getImei();
        imei = imeiNo;
        String numberString = number.getText().toString();
        String mgsString = message.getText().toString();


        String phoneNumber = number.getText().toString();

        String fullPhoneNumber =  phoneNumber;

        if (isValidPhoneNumber(fullPhoneNumber)) {
            // Phone number is valid, display toast message with full phone number
            String message = "Full phone number: " + fullPhoneNumber;
            Toast.makeText(RegisterNumberActivity.this, message, Toast.LENGTH_LONG).show();

            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("ENUM", fullPhoneNumber);
            myEdit.putString("MSG", mgsString);
            myEdit.putString("IMEI", imei);
            myEdit.apply();
            Toast.makeText(this, imei, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,MainActivity.class));


        } else {
            // Phone number is invalid, display error message
            number.setError("Invalid phone number");
        }

    }


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
//                            handleSignInResult(result.getData());
                        }
//                        handleSignInResultx(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                    } else {
                    }
                }
            });
    private final ActivityResultLauncher<Void>  pickContactLauncher = registerForActivityResult(
            new ActivityResultContracts.PickContact(),
                new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            contactPicked(result);
        }
    });

        // ActivityResultLauncher for picking contact
    private final ActivityResultLauncher<Void> pickContactLauncher0 = registerForActivityResult(
            new ActivityResultContracts.PickContact(), this::contactPicked);
//                new ActivityResultContracts.PickContact(), this::handleContactResult);

private void contactPicked(Uri contactUri) {
    Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
    if (cursor != null && cursor.moveToFirst()) {
        int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        String phoneNumber = cursor.getString(phoneNumberIndex);

        Toast.makeText(this, "Selected phone number: " + phoneNumber, Toast.LENGTH_SHORT).show();
        cursor.close();
    } else {
        Toast.makeText(this, "Failed to retrieve contact information", Toast.LENGTH_SHORT).show();
    }
}

    private void handleContactResult(Uri contactUri) {
        if (contactUri != null) {
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (numberIndex != -1) {
                            String contactNumber = cursor.getString(numberIndex);

                            // Extract country code from the contact's number
                            String countryCode = getCountryCodeFromPhoneNumber(contactNumber);

                            // Set the country code and the selected contact number to the TextInputEditText
                            String fullPhoneNumber = countryCode + contactNumber;

                            Toast.makeText(RegisterNumberActivity.this, fullPhoneNumber, Toast.LENGTH_LONG).show();
//                            number.setText(fullPhoneNumber);
                        } else {
                            // Handle case when number index is not found
                            Log.e("HandleContactResult", "Number index not found in cursor");
                        }
                    } else {
                        // Handle case when cursor is empty
                        Log.e("HandleContactResult", "Cursor is empty");
                    }
                } finally {
                    cursor.close();
                }
            } else {
                // Handle case when cursor is null
                Log.e("HandleContactResult", "Cursor is null");
            }
        }
    }



    private String getCountryCodeFromPhoneNumber(String phoneNumber) {
        // Extract country code from the phone number
        // Here you need to implement the logic to extract the country code from the phone number
        // This can be done using various approaches such as using a library or parsing the number manually
        // For simplicity, let's assume the country code is the first two digits of the number
        String countryCode = phoneNumber.substring(0, 2); // Assuming the country code is the first two digits
        return countryCode;
    }

    // Handle the result of picking a contact
    private void handleContactResult0(Uri contactUri) {
        if (contactUri != null) {
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String contactNumber = cursor.getString(numberIndex);
                cursor.close();

                // Set the selected contact number to the TextInputEditText
                number.setText(contactNumber);
            }
        }
    }




}