package com.example.fireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.fireapp.R;

public class home_page extends AppCompatActivity {

    // variables for the buttons
    private Button btnConfigure, btnWeather, btnCurrencyConverter, btnTicketmaster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // connect the buttons to the xml
        btnConfigure = findViewById(R.id.btnConfigureUser);
        btnWeather = findViewById(R.id.btnWeather);
        btnCurrencyConverter = findViewById(R.id.btnCurrConverter);

        // Configure User button
        btnConfigure.setOnClickListener(view -> {
            // take to the configure user page
            Intent intent = new Intent(home_page.this, configure_user.class);
            // obtain bundle data from the previous activity
            Bundle bundle = getIntent().getExtras();

            // extract the id
            String id = bundle.getString("id");

            // pass this value to the next activity
            intent.putExtra("id", id);

            startActivity(intent);
        });

        // Weather button
        btnWeather.setOnClickListener(view -> {
            // take to the weather page
            Intent intent = new Intent(home_page.this, destination_weather.class);
            startActivity(intent);

            // obtain bundle data from the previous activity
            Bundle bundle = getIntent().getExtras();

            // extract the data
            String cityName = bundle.getString("city");
            String countryCode = bundle.getString("countryCode");

            String arriveMonth = bundle.getString("arriveMonth");
            String arriveDay = bundle.getString("arriveDay");
            String arriveYear = bundle.getString("arriveYear");

            String leaveMonth = bundle.getString("leaveMonth");
            String leaveDay = bundle.getString("leaveDay");
            String leaveYear = bundle.getString("leaveYear");

            // pass these values to the next activity
            intent.putExtra("cityName", cityName);
            intent.putExtra("countryCode", countryCode);
            intent.putExtra("arriveMonth", arriveMonth);
            intent.putExtra("arriveDay", arriveDay);
            intent.putExtra("arriveYear", arriveYear);
            intent.putExtra("leaveMonth", leaveMonth);
            intent.putExtra("leaveDay", leaveDay);
            intent.putExtra("leaveYear", leaveYear);

            // create a new intent and pass the bundle to the next activity
            intent.putExtras(bundle);
            startActivity(intent);
        });

        // Currency Converter button
        btnCurrencyConverter.setOnClickListener(view -> {
            // take to the currency converter page
            Intent intent = new Intent(home_page.this, currency_converter.class);
            startActivity(intent);
        });

        // Ticketmaster button
        //btnTicketmaster.setOnClickListener(view -> {
            // take to the ticketmaster page
            //Intent intent = new Intent(home_page.this, ticketmaster.class);
            //startActivity(intent);
       // });
    }
}