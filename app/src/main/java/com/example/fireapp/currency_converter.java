package com.example.fireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class currency_converter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        // obtain the variables
        Spinner fromSpinner = findViewById(R.id.from_spinner);
        Spinner toSpinner = findViewById(R.id.to_spinner);
        EditText amountEditText = findViewById(R.id.amount_edit_text);
        Button convertButton = findViewById(R.id.btnConvert);
        Button backButton = findViewById(R.id.btnBack);
        TextView resultTextView = findViewById(R.id.result_text_view);

        // set the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.currency_array));
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        // Back button's on click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Convert button's on click listener
        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fromCurrency = fromSpinner.getSelectedItem().toString();
                String toCurrency = toSpinner.getSelectedItem().toString();
                String amountString = amountEditText.getText().toString();
                if (amountString.isEmpty()) {
                    Toast.makeText(currency_converter.this, "Please enter amount to convert", Toast.LENGTH_SHORT).show();
                    return;
                }
                double amount = Double.parseDouble(amountString);
                String url = "https://api.apilayer.com/exchangerates_data/convert?to=" + toCurrency + "&from=" + fromCurrency + "&amount=" + amount + "&apikey=oCBKrw4QyUeDNDvxzfzmPBQ7fiT8bURV";
                RequestQueue queue = Volley.newRequestQueue(currency_converter.this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            double result = jsonObject.getDouble("result");
                            resultTextView.setText(String.format("%.2f", result) + " " + toCurrency);
                        } catch (JSONException e) {
                            Toast.makeText(currency_converter.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(currency_converter.this, "Error making request", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });
                queue.add(stringRequest);
            }
        });
    }
}