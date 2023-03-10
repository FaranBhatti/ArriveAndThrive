package com.example.arriveandthrive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    EditText etCity;
    EditText etCountryCode;
    EditText etMonthNumber;
    TextView tvResult;
    Button btnGetData;
    private final String forecast_url = "https://pro.openweathermap.org/data/2.5/forecast/climate";

    private final String historical_month_url = "https://history.openweathermap.org/data/2.5/aggregated/month";
    // see above API info here: https://openweathermap.org/api/statistics-api
    private final String appid = "5a055dbf56ab6f415567d8a482c453f2";
    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCity = findViewById(R.id.city);
        etCountryCode = findViewById(R.id.country_code);
        etMonthNumber = findViewById(R.id.month_number);
        tvResult = findViewById(R.id.result);
        btnGetData = findViewById(R.id.btnGetData);
        btnGetData.setOnClickListener(view -> {
            tvResult.setText("");
            String tempUrl = "";
            String cityName = etCity.getText().toString().trim();
            String countryCode = etCountryCode.getText().toString().trim();
            String monthNumber = etMonthNumber.getText().toString().trim();
            if (cityName.equals("") || countryCode.equals("") || monthNumber.equals("")) {
                tvResult.setText("Fields cannot be empty!");
            } else {
                tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + monthNumber + "&appid=" + appid;
            }
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, tempUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        //JSONObject jsonObjectResult = response.getJSONObject("result");
                        JSONObject jsonObjectAvgMinTemp = response.getJSONObject("result").getJSONObject("temp");
                        double avg_min_temp = jsonObjectAvgMinTemp.getDouble("average_min") - 273.15;
//                        double feelslike = jsonObjectMain.getDouble("feels_like") - 273.15;
//                        int pressure = jsonObjectMain.getInt("pressure");
//                        int humidity = jsonObjectMain.getInt("humidity");
//
//                        JSONArray result = response.getJSONArray("result");
//                        JSONObject jsonObjectTemp = result.getJSONObject(1);
//                        double avg_min_temp = jsonObjectTemp.getDouble("average_min") - 273.15;
//                        String description = jsonObjectWeather.getString("description");
//
//                        JSONObject jsonObjectWind = response.getJSONObject("wind");
//                        double speed = jsonObjectWind.getDouble("speed");
//                        int degree = jsonObjectWind.getInt("deg");
//
//                        JSONObject jsonObjectClouds = response.getJSONObject("clouds");
//                        int cloud = jsonObjectClouds.getInt("all");
//
//                        JSONObject jsonObjectSys = response.getJSONObject("sys");
                        String currentCountry = countryCode;
                        String currentCity = cityName;

                        tvResult.setText("Current weather of " + currentCity + " (" + currentCountry + ")\n"
                                + "Average Min. Temp: " + df.format(avg_min_temp) + " \u2103\n");
//                                + "Feels like: " + df.format(feelslike) + " \u2103\n"
//                                + "Humidity: " + humidity + "%\n"
//                                + "Description: " + description + "\n"
//                                + "Wind speed: " + speed + "\n"
//                                + "Wind degree: " + degree + "\n"
//                                + "Cloudiness: " + cloud + "%\n"
//                                + "Pressure: " + pressure + " hPa");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
        });
    }
}