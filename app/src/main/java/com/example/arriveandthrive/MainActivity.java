package com.example.arriveandthrive;

import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import java.time.LocalDate;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MainActivity extends AppCompatActivity {

    EditText etCity;
    EditText etCountryCode;
    EditText etMonthNumber;
    TextView tvResult;
    Button btnGetData;
    private final String forecast_url = "https://pro.openweathermap.org/data/2.5/forecast/climate";

    private final String historical_month_url = "https://history.openweathermap.org/data/2.5/aggregated/day";
    // see above API info here: https://openweathermap.org/api/statistics-api
    private final String appid = "5a055dbf56ab6f415567d8a482c453f2";
    private String tempUrl;
    DecimalFormat df = new DecimalFormat("#.##");

    private int arriveYear;
    private int arriveMonth;
    private String arriveMonth_str;
    private int arriveDay;
    private String arriveDay_str;
    private int leaveYear;
    private int leaveMonth;
    private String leaveMonth_str;
    private int leaveDay;
    private String leaveDay_str;
    private String countryCode;
    private String cityName;
    private boolean arriveMoreThan14Future = false;
    private boolean leaveMoreThan14Future = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // INSTANTIATE WeatherDay OBJECT
        WeatherDay[] weatherDay = new WeatherDay[14];

        // BUNDLE FOR RETRIEVING DATE AND LOCATION INFORMATION
//        Bundle dates_and_location = getIntent().getExtras();
//        if (dates_and_location != null) {
//            arriveYear = dates_and_location.getString("departYear");
//            arriveMonth = dates_and_location.getString("departMonth");
//            arriveDay = dates_and_location.getString("departDay");
//            leaveYear = dates_and_location.getString("returnYear");
//            leaveMonth = dates_and_location.getString("returnYear");
//            leaveDay =  dates_and_location.getString("returnYear");
//            countryCode = dates_and_location.getString("countryCode");
//            cityName = dates_and_location.getString("cityName");
//        }


        // DUMMY VARIABLES FOR TESTING
        arriveYear = 2023;
        arriveMonth = 5;
        arriveDay = 27;
        leaveYear = 2023;
        leaveMonth = 5;
        leaveDay = 30;
        countryCode = "CA";
        cityName = "Vancouver";

        arriveMonth_str = Integer.toString(arriveMonth);
        arriveDay_str = Integer.toString(arriveDay);
        leaveMonth_str = Integer.toString(leaveMonth);
        leaveDay_str = Integer.toString(leaveDay);


        // CALCULATION OF TOTAL TRIP LENGTH IN DAYS
        LocalDate arriveDate = LocalDate.of(arriveYear, arriveMonth, arriveDay);
        LocalDate leaveDate = LocalDate.of(leaveYear, leaveMonth, leaveDay);
        long totalTripDays = leaveDate.toEpochDay() - arriveDate.toEpochDay();


        // CALCULATION OF CURRENT DATE GIVEN Canada/Vancouver TIME ZONE
        LocalDate localDate = LocalDate.now();
        ZoneId zone = ZoneId.of("Canada/Vancouver");
        ZonedDateTime zonedDate = localDate.atStartOfDay(zone);
        LocalDate currentZonedDate = zonedDate.toLocalDate();


        // CALCULATION TO SEE IF -ARRIVE- DATE IS MORE THAT 14 DAYS IN THE FUTURE
        if ((arriveDate.toEpochDay() - currentZonedDate.toEpochDay()) <= 14) {
            arriveMoreThan14Future = false;
        }
        else {
            arriveMoreThan14Future = true;
        }


        // CALCULATION OF WHETHER -LEAVE- DATE IS MORE THAN 14 DAYS IN THE FUTURE
        if ((leaveDate.toEpochDay() - currentZonedDate.toEpochDay()) <= 14) {
            leaveMoreThan14Future = false;
        }
        else {
            leaveMoreThan14Future = true;
        }


        // DECISION TREE TO USE 14 DAY, HISTORICAL, OR COMBO APIs
        if (leaveMoreThan14Future == false) {
        // USE 14 DAY API

        }


        else if (leaveMoreThan14Future && arriveMoreThan14Future) {
            // USE HISTORICAL API
            int daysInArriveMonth = arriveDate.lengthOfMonth() - arriveDay;

            if (arriveMonth != leaveMonth) {
                int testCount = 0;
                for (int i = 1; i < daysInArriveMonth; i++) {
                    testCount++;
                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        tvResult.setText("Fields cannot be empty!");
                    } else {
                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;
                    }
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    // iFinal solved weird error in object array setter funtion seen in weatherDay[iFinal].setAvgMinTemp( etc. )
                    int iFinal = i;
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, tempUrl, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                //JSONObject jsonObjectResult = response.getJSONObject("result");
                                JSONObject jsonObjectAvgMinTemp = response.getJSONObject("result").getJSONObject("temp");
                                //double avg_max_temp = jsonObjectAvgMinTemp.getDouble("average_max") - 273.15;
                                weatherDay[iFinal].setAvgMinTemp(jsonObjectAvgMinTemp.getDouble("average_min") - 273.15);
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

                    // increment from arrive day up until last day of the month
                    arriveDay++;
                    arriveDay_str = Integer.toString(arriveDay);
                }

                String currentCountry = countryCode;
                String currentCity = cityName;
                for (int i = 1; i < daysInArriveMonth; i++) {
                    tvResult.setText("Current weather of " + currentCity + " (" + currentCountry + ")\n"
                            + "Average Min. Temp: " + df.format(weatherDay[i].getAvgMinTemp()) + " \u2103\n");
                }
            }
        }

//                if (arriveMonth == leaveMonth) {
//                    if (cityName.equals("") || countryCode.equals("") || monthNumber.equals("")) {
//                        tvResult.setText("Fields cannot be empty!");
//                    } else {
//                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + monthNumber + "&appid=" + appid;
//                    }
//                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
//                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, tempUrl, null, new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            try {
//                                //JSONObject jsonObjectResult = response.getJSONObject("result");
//                                JSONObject jsonObjectAvgMinTemp = response.getJSONObject("result").getJSONObject("temp");
//                                double avg_min_temp = jsonObjectAvgMinTemp.getDouble("average_min") - 273.15;
//                                //                        double feelslike = jsonObjectMain.getDouble("feels_like") - 273.15;
//                                //                        int pressure = jsonObjectMain.getInt("pressure");
//                                //                        int humidity = jsonObjectMain.getInt("humidity");
//                                //
//                                //                        JSONArray result = response.getJSONArray("result");
//                                //                        JSONObject jsonObjectTemp = result.getJSONObject(1);
//                                //                        double avg_min_temp = jsonObjectTemp.getDouble("average_min") - 273.15;
//                                //                        String description = jsonObjectWeather.getString("description");
//                                //
//                                //                        JSONObject jsonObjectWind = response.getJSONObject("wind");
//                                //                        double speed = jsonObjectWind.getDouble("speed");
//                                //                        int degree = jsonObjectWind.getInt("deg");
//                                //
//                                //                        JSONObject jsonObjectClouds = response.getJSONObject("clouds");
//                                //                        int cloud = jsonObjectClouds.getInt("all");
//                                //
//                                //                        JSONObject jsonObjectSys = response.getJSONObject("sys");
//                                String currentCountry = countryCode;
//                                String currentCity = cityName;
//
//                                tvResult.setText("Current weather of " + currentCity + " (" + currentCountry + ")\n"
//                                        + "Average Min. Temp: " + df.format(avg_min_temp) + " \u2103\n");
//                                //                                + "Feels like: " + df.format(feelslike) + " \u2103\n"
//                                //                                + "Humidity: " + humidity + "%\n"
//                                //                                + "Description: " + description + "\n"
//                                //                                + "Wind speed: " + speed + "\n"
//                                //                                + "Wind degree: " + degree + "\n"
//                                //                                + "Cloudiness: " + cloud + "%\n"
//                                //                                + "Pressure: " + pressure + " hPa");
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    queue.add(request);
//                }
//
//            }
//        }
//
//
//        else if (leaveMoreThan14Future && !arriveMoreThan14Future) {
//            // USE COMBO of APIs
//        }
//
    }
}