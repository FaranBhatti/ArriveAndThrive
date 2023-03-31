package com.example.arriveandthrive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
        btnTicketmaster = findViewById(R.id.btnTicketmaster);

        // Configure User button
        btnConfigure.setOnClickListener(view -> {
            // take to the configure user page
            //Intent intent = new Intent(home_page.this, configure_user.class);
            //startActivity(intent);
        });

        // Weather button
        btnConfigure.setOnClickListener(view -> {
            // take to the weather page
            //Intent intent = new Intent(home_page.this, weather.class);
            //startActivity(intent);
        });

        // Currency Converter button
        btnCurrencyConverter.setOnClickListener(view -> {
            // take to the currency converter page
            //Intent intent = new Intent(home_page.this, currency_converter.class);
            //startActivity(intent);
        });

        // Ticketmaster button
        btnTicketmaster.setOnClickListener(view -> {
            // take to the ticketmaster page
            //Intent intent = new Intent(home_page.this, ticketmaster.class);
            //startActivity(intent);
        });
    }
}