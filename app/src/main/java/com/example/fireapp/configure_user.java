package com.example.fireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private final String WeatherAPIKey = "5a055dbf56ab6f415567d8a482c453f2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_user);

        // obtaining the buttons
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnResetFields = findViewById(R.id.btnResetFields);
        Button btnBack = findViewById(R.id.btnBack);

        // obtaining the fields the user has entered
        EditText edtCity = findViewById(R.id.city_edittext);
        EditText edtCountryCode = findViewById(R.id.country_edittext);
        EditText edtLengthOfTrip = findViewById(R.id.length_edittext);

        // btnResetFields on click listener to reset
        btnResetFields.setOnClickListener(view -> {
            // set the fields to empty
            edtCity.setText("");
            edtCountryCode.setText("");
            edtLengthOfTrip.setText("");
        });

        // btnBacks on click listener to go back to the previous activity
        btnBack.setOnClickListener(view -> {
            finish();
        });

        // btnConfirm on click listener to confirm the user's input
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = edtCity.getText().toString();
                String countryCode = edtCountryCode.getText().toString();
                String lengthStr = edtLengthOfTrip.getText().toString();
            }
        });
    }
    // function to test if user enters a valid city
    public static boolean isValidCity(String cityName, String apiKey) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            // print value of responseCode
            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // function to test if user enters a valid country code
    public static boolean isValidCountryCode(String countryCode, String apiKey) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=London," + countryCode + "&appid=" + apiKey;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            String code = jsonObject.getJSONObject("sys").getString("country");
            return code.equalsIgnoreCase(countryCode);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}