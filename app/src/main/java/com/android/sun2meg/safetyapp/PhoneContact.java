package com.android.sun2meg.safetyapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneContact extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT =1;
    private TextView phone;
    private Button select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_phone_contact);

        phone = findViewById (R.id.phone);
        select = findViewById (R.id.select);

        select.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent in = new Intent (Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult (in, RESULT_PICK_CONTACT);
            }
        });

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

            // Extract country code from the contact's number
            String countryCode = getCountryCodeFromPhoneNumber(phoneNo);

            Toast.makeText(PhoneContact.this, countryCode, Toast.LENGTH_LONG).show();

            phone.setText (phoneNo);


        } catch (Exception e) {
            e.printStackTrace ();
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
}
