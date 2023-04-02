package com.example.fireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class configure_user extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_user);

        // obtaining the buttons
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnBack = findViewById(R.id.btnBack);

        // obtaining the fields the user has entered
        Spinner edtCity = findViewById(R.id.city_spinner);
        Spinner edtCountryCode = findViewById(R.id.country_code_spinner);
        Spinner edtLengthOfTrip = findViewById(R.id.length_spinner);

        // btnBacks on click listener to go back to the previous activity
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // btnConfirm on click listener to confirm the user's input
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // obtain city, country code and length of trip
                String city = edtCity.getSelectedItem().toString();
                String countryCode = edtCountryCode.getSelectedItem().toString();
                String lengthStr = edtLengthOfTrip.getSelectedItem().toString();

                // if error_check function is true then do something
                if (error_check(city, countryCode, lengthStr)) {
                    // store those in a bundle and pass it to the next activity
                    Bundle bundle = new Bundle();
                    bundle.putString("city", city);
                    bundle.putString("countryCode", countryCode);
                    bundle.putString("lengthStr", lengthStr);

                    // create a new intent and pass the bundle to the next activity
                    Intent intent = new Intent(configure_user.this, home_page.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else {
                    // if error_check function is false then do something
                    Toast.makeText(configure_user.this, "Please enter valid input", Toast.LENGTH_SHORT).show();
                }

            }

            private boolean error_check(String city, String countryCode, String lengthStr) {
                boolean isValidCity = false;

                switch (countryCode) {
                    case "US":
                        if (city.equals("San Francisco") || city.equals("Los Angeles") || city.equals("New York")) {
                            isValidCity = true;
                        }
                        break;
                    case "CA":
                        if (city.equals("Toronto") || city.equals("Vancouver") || city.equals("Montreal")) {
                            isValidCity = true;
                        }
                        break;
                    case "CH":
                        if (city.equals("Zurich") || city.equals("Geneva") || city.equals("Bern")) {
                            isValidCity = true;
                        }
                        break;
                    case "DE":
                        if (city.equals("Berlin") || city.equals("Hamburg") || city.equals("Munich")) {
                            isValidCity = true;
                        }
                        break;
                    case "SE":
                        if (city.equals("Stockholm") || city.equals("Gothenburg") || city.equals("Malmö")) {
                            isValidCity = true;
                        }
                        break;
                    default:
                        break;
                }

                if (isValidCity) {
                    return true;
                } else {
                    // throw a toast error
                    Toast.makeText(configure_user.this, "Please enter a valid city", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }
}