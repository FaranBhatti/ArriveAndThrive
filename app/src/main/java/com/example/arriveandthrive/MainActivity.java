package com.example.arriveandthrive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import android.util.Log;

//import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DecimalFormat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import okhttp3.OkHttpClient;
import java.io.IOException;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {
    private final String forecast14Days_url = "https://api.openweathermap.org/data/2.5/forecast/daily";

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
    private int leaveDayLoop = 1;
    private String leaveDayLoop_str = "1";
    private int daysInArriveMonth;
    private int daysInLeaveMonth;
    private long totalTripDays;
    private int daysBtwNowAndArrive;
    private int daysBtwNowAndLeave;
    private String countryCode;
    private String cityName;
    private boolean arriveMoreThan14Future = false;
    private boolean leaveMoreThan14Future = false;
    private List<WeatherDay> weatherDayList = new ArrayList<>();
    private int dayNum = 0;

    private String weather_days_str;
    private OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // INITIALIZE EACH WeatherDay OBJECT IN weatherDayList
        for (int i = 0; i < 14; i++) {
            weatherDayList.add(new WeatherDay(0, 0, 0, 0, 0));
        }

        // DUMMY VARIABLES FOR TESTING
        arriveYear = 2023;
        arriveMonth = 4; //4(works with current date forced into the future) or 4(works) or 5(works) or 6(works)
        arriveDay = 3; //28(works with current date forced into the future) or 3(works) or 27(works) or 9(works)
        leaveYear = 2023;
        leaveMonth = 4; //5(works with current date forced into the future) or 4(works) or 6(works) or 6(works)
        leaveDay = 8; //2(works with current date forced into the future) or 8(works) or 3(works) or 15(works)
        countryCode = "CA";
        cityName = "Vancouver";


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

        arriveMonth_str = Integer.toString(arriveMonth);
        arriveDay_str = Integer.toString(arriveDay);
        leaveMonth_str = Integer.toString(leaveMonth);
        leaveDay_str = Integer.toString(leaveDay);


        // CALCULATION OF TOTAL TRIP LENGTH IN DAYS
        LocalDate arriveDate = LocalDate.of(arriveYear, arriveMonth, arriveDay);
        LocalDate leaveDate = LocalDate.of(leaveYear, leaveMonth, leaveDay);
        long totalTripDays = (leaveDate.toEpochDay() - arriveDate.toEpochDay()) + 1;
        Log.d("DEBUG LOG", "totalTripDays: " + totalTripDays);


        // CALCULATION OF CURRENT DATE GIVEN PST TIME ZONE
        LocalDate localDate = LocalDate.now();
        ZoneId zone = ZoneId.of("America/Los_Angeles"); // PST
        ZonedDateTime zonedDate = localDate.atStartOfDay(zone);
        LocalDate currentZonedDate = zonedDate.toLocalDate();
        // must change this to be towards end of month to test 14 DAY API leaveMonth != arriveMonth
        //currentZonedDate = LocalDate.of(2023, 4, 25);
        Log.d("DEBUG LOG", "currentZonedDate: " + currentZonedDate);


        // CALCULATION OF DAYS IN ARRIVE MONTH and LEAVE MONTH
        if (arriveMonth != leaveMonth) {
            daysInArriveMonth = arriveDate.lengthOfMonth() - arriveDay + 1;
            daysInLeaveMonth = leaveDay;
        } else {
            daysInArriveMonth = leaveDay - arriveDay + 1;
            daysInLeaveMonth = leaveDay - arriveDay + 1;
        }


        // CALCULATION TO SEE IF -ARRIVE- DATE IS MORE THAN 14 DAYS IN THE FUTURE
        if ((arriveDate.toEpochDay() - currentZonedDate.toEpochDay()) <= 14) {
            arriveMoreThan14Future = false;
        } else {
            arriveMoreThan14Future = true;
        }


        // CALCULATION OF WHETHER -LEAVE- DATE IS MORE THAN 14 DAYS IN THE FUTURE
        if ((leaveDate.toEpochDay() - currentZonedDate.toEpochDay()) <= 14) {
            leaveMoreThan14Future = false;

            daysBtwNowAndArrive = (int) (arriveDate.toEpochDay() - currentZonedDate.toEpochDay());
            Log.d("DEBUG LOG", "daysBtwNowAndArrive: " + daysBtwNowAndArrive);
            daysBtwNowAndLeave = (int) (leaveDate.toEpochDay() - currentZonedDate.toEpochDay());
            Log.d("DEBUG LOG", "daysBtwNowAndLeave: " + daysBtwNowAndLeave);

        } else {
            leaveMoreThan14Future = true;
        }


        //////////// DECISION TREE TO USE 14 DAY, HISTORICAL, OR COMBO APIs ////////////

        // USE 14 DAY API
        if (arriveMoreThan14Future == false && leaveMoreThan14Future == false) {

            // IF ARRIVE MONTH != LEAVE MONTH //////////////////////////////////////////////////////////////////
            if (arriveMonth != leaveMonth) {

                dayNum = 0;
                Log.d("DEBUG LOG", "arriveMonth != leaveMonth");
                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                    Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();

                } else {
                    tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                    Log.d("DEBUG LOG", "tempUrl arrive: " + tempUrl);

                    NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                        @Override
                        public void onNetworkTaskComplete(String response) {
                            // Do something with the response here
                            Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);

                            // Handle the response
                            if (response != null) {

                                double averageMax = 0;
                                double averageMin = 0;
                                double chanceOfRain = 0;
                                double averageCloudCoverage = 0;
                                for (int i = daysBtwNowAndArrive - 1; i < (daysBtwNowAndArrive + daysInArriveMonth) - 1; i++) {

                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                    JsonArray listArray = jsonObject.getAsJsonArray("list");
                                    JsonObject firstListObject = listArray.get(i).getAsJsonObject();
                                    JsonObject tempObject = firstListObject.getAsJsonObject("temp");
                                    averageMax = tempObject.get("max").getAsDouble();
                                    averageMin = tempObject.get("min").getAsDouble();
                                    chanceOfRain = 100 * (firstListObject.get("pop").getAsDouble());
                                    averageCloudCoverage = firstListObject.get("clouds").getAsDouble();
                                    Log.d("DEBUG LOG", "Arrive averageMax: " + ((int) Math.round(averageMax)));
                                    Log.d("DEBUG LOG", "Arrive averageMin: " + ((int) Math.round(averageMin)));
                                    Log.d("DEBUG LOG", "Arrive chanceOfRain: " + ((int) Math.round(chanceOfRain)));
                                    Log.d("DEBUG LOG", "Arrive averageCloudCoverage: " + ((int) Math.round(averageCloudCoverage)));

                                    if (weatherDayList != null) {
                                        weatherDayList.get(dayNum).setAvgMaxTemp((int) Math.round(averageMax));
                                        weatherDayList.get(dayNum).setAvgMinTemp((int) Math.round(averageMin));
                                        weatherDayList.get(dayNum).setChanceOfRain((int) Math.round(chanceOfRain));
                                        weatherDayList.get(dayNum).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                    }

                                    dayNum = dayNum + 1;

                                }

                            } else {
                                // Handle the error or null response
                                dayNum = dayNum;
                            }
                        }
                    });
                    networkTask.execute();
                }

                Log.d("DEBUG LOG", "daysInLeaveMonth: " + daysInLeaveMonth);
                Log.d("DEBUG LOG", "LeaveDay initial: " + leaveDay);

                if (cityName.equals("") || countryCode.equals("") || leaveMonth_str.equals("")) {
                    Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();

                } else {
                    tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                    Log.d("DEBUG LOG", "tempUrl leave: " + tempUrl);

                    NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                        @Override
                        public void onNetworkTaskComplete(String response) {
                            Log.d("DEBUG LOG", "Leave NT Response in MAIN: " + response);

                            // Handle the response
                            if (response != null) {

                                double averageMax = 0;
                                double averageMin = 0;
                                double chanceOfRain = 0;
                                double averageCloudCoverage = 0;
                                for (int i = daysBtwNowAndArrive + daysInArriveMonth - 1; i < (daysBtwNowAndArrive + totalTripDays) - 1; i++) {

                                    Log.d("DEBUG LOG", "Leave count: " + dayNum);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                    JsonArray listArray = jsonObject.getAsJsonArray("list");
                                    JsonObject firstListObject = listArray.get(i).getAsJsonObject();
                                    JsonObject tempObject = firstListObject.getAsJsonObject("temp");
                                    averageMax = tempObject.get("max").getAsDouble();
                                    averageMin = tempObject.get("min").getAsDouble();
                                    chanceOfRain = 100 * (firstListObject.get("pop").getAsDouble());
                                    averageCloudCoverage = firstListObject.get("clouds").getAsDouble();
                                    Log.d("DEBUG LOG", "Leave averageMax: " + ((int) Math.round(averageMax)));
                                    Log.d("DEBUG LOG", "Leave averageMin: " + ((int) Math.round(averageMin)));
                                    Log.d("DEBUG LOG", "Leave chanceOfRain: " + ((int) Math.round(chanceOfRain)));
                                    Log.d("DEBUG LOG", "Leave averageCloudCoverage: " + ((int) Math.round(averageCloudCoverage)));

                                    if (weatherDayList != null) {
                                        weatherDayList.get(dayNum).setAvgMaxTemp((int) Math.round(averageMax));
                                        weatherDayList.get(dayNum).setAvgMinTemp((int) Math.round(averageMin));
                                        weatherDayList.get(dayNum).setChanceOfRain((int) Math.round(chanceOfRain));
                                        weatherDayList.get(dayNum).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                    }

                                    dayNum = dayNum + 1;

                                }

                            } else {
                                // Handle the error or null reponse
                                dayNum = dayNum;
                            }
                        }
                    });
                    networkTask.execute();
                }

            // IF ARRIVE MONTH = LEAVE MONTH //
            } else {

                dayNum = 0;
                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                    Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();

                } else {
                    tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                    Log.d("DEBUG LOG", "tempUrl: " + tempUrl);

                    NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                        @Override
                        public void onNetworkTaskComplete(String response) {
                            Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);

                            // Handle the response
                            if (response != null) {

                                double averageMax = 0;
                                double averageMin = 0;
                                double chanceOfRain = 0;
                                double averageCloudCoverage = 0;
                                for (int i = daysBtwNowAndArrive - 1; i < (daysBtwNowAndArrive + totalTripDays) - 1; i++) {

                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                    JsonArray listArray = jsonObject.getAsJsonArray("list");
                                    JsonObject firstListObject = listArray.get(i).getAsJsonObject();
                                    JsonObject tempObject = firstListObject.getAsJsonObject("temp");
                                    averageMax = tempObject.get("max").getAsDouble();
                                    averageMin = tempObject.get("min").getAsDouble();
                                    chanceOfRain = 100 * (firstListObject.get("pop").getAsDouble());
                                    averageCloudCoverage = firstListObject.get("clouds").getAsDouble();
                                    Log.d("DEBUG LOG", "Arrive averageMax: " + ((int) Math.round(averageMax)));
                                    Log.d("DEBUG LOG", "Arrive averageMin: " + ((int) Math.round(averageMin)));
                                    Log.d("DEBUG LOG", "Arrive chanceOfRain: " + ((int) Math.round(chanceOfRain)));
                                    Log.d("DEBUG LOG", "Arrive averageCloudCoverage: " + ((int) Math.round(averageCloudCoverage)));

                                    if (weatherDayList != null) {
                                        weatherDayList.get(dayNum).setAvgMaxTemp((int) Math.round(averageMax));
                                        weatherDayList.get(dayNum).setAvgMinTemp((int) Math.round(averageMin));
                                        weatherDayList.get(dayNum).setChanceOfRain((int) Math.round(chanceOfRain));
                                        weatherDayList.get(dayNum).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                    }

                                    dayNum = dayNum + 1;

                                }

                            } else {
                                // Handle the error or null reponse
                                dayNum = dayNum;
                            }
                        }
                    });
                    networkTask.execute();
                }
            }

        // USE HISTORICAL API
        } else if (arriveMoreThan14Future && leaveMoreThan14Future) {

            if (arriveMonth != leaveMonth) {
                dayNum = 0;
                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                Log.d("DEBUG LOG", "daysInLeaveMonth: " + daysInLeaveMonth);
                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);
                Log.d("DEBUG LOG", "leaveDayLoop initial: " + leaveDayLoop);

                for (int i = 0; i < daysInArriveMonth; i++) {
                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);

                                // Handle the response
                                if (response != null) {

                                    dayNum = dayNum + 1;
                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                    double averageMax = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_max").getAsDouble() - 273.15;
                                    double averageMin = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_min").getAsDouble() - 273.15;
                                    double averageRainMM = 100 * (jsonObject.getAsJsonObject("result").getAsJsonObject("precipitation").get("mean").getAsDouble());
                                    double averageCloudCoverage = jsonObject.getAsJsonObject("result").getAsJsonObject("clouds").get("mean").getAsDouble();

                                    if (weatherDayList != null) {
                                        weatherDayList.get(dayNum - 1).setAvgMaxTemp((int) Math.round(averageMax));
                                        weatherDayList.get(dayNum - 1).setAvgMinTemp((int) Math.round(averageMin));
                                        weatherDayList.get(dayNum - 1).setAvgRainMM((int) Math.round(averageRainMM));
                                        weatherDayList.get(dayNum - 1).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                    }
                                } else {
                                    // Handle the error or null reponse
                                    dayNum = dayNum;
                                }
                            }
                        });
                        networkTask.execute();
                        arriveDay = arriveDay + 1;
                        arriveDay_str = Integer.toString(arriveDay);
                    }
                }

                leaveDayLoop = 1;
                leaveDayLoop_str = Integer.toString(leaveDayLoop);

                for (int i = 0; i < daysInLeaveMonth; i++) {
                    if (cityName.equals("") || countryCode.equals("") || leaveMonth_str.equals("")) {
                        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + leaveMonth_str + "&day=" + leaveDayLoop_str + "&appid=" + appid;

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                Log.d("DEBUG LOG", "Leave NT Response in MAIN: " + response);

                                // Handle the response
                                if (response != null) {

                                    dayNum = dayNum + 1;
                                    Log.d("DEBUG LOG", "Leave count: " + dayNum);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                    double averageMax = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_max").getAsDouble() - 273.15;
                                    double averageMin = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_min").getAsDouble() - 273.15;
                                    double averageRainMM = 100 * (jsonObject.getAsJsonObject("result").getAsJsonObject("precipitation").get("mean").getAsDouble());
                                    double averageCloudCoverage = jsonObject.getAsJsonObject("result").getAsJsonObject("clouds").get("mean").getAsDouble();

                                    if (weatherDayList != null) {
                                        weatherDayList.get(dayNum - 1).setAvgMaxTemp((int) Math.round(averageMax));
                                        weatherDayList.get(dayNum - 1).setAvgMinTemp((int) Math.round(averageMin));
                                        weatherDayList.get(dayNum - 1).setAvgRainMM((int) Math.round(averageRainMM));
                                        weatherDayList.get(dayNum - 1).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                    }

                                } else {
                                    // Handle the error or null reponse
                                    dayNum = dayNum;
                                }
                            }
                        });
                        networkTask.execute();
                        leaveDayLoop = leaveDayLoop + 1;
                        leaveDayLoop_str = Integer.toString(leaveDayLoop);
                    }
                }

            // IF ARRIVE MONTH = LEAVE MONTH
            } else {

                dayNum = 0;
                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                for (int i = 0; i < daysInArriveMonth; i++) {
                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);

                                // Handle the response
                                if (response != null) {

                                    dayNum = dayNum + 1;
                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                    Gson gson = new Gson();
                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                    double averageMax = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_max").getAsDouble() - 273.15;
                                    double averageMin = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_min").getAsDouble() - 273.15;
                                    double averageRainMM = 100 * (jsonObject.getAsJsonObject("result").getAsJsonObject("precipitation").get("mean").getAsDouble());
                                    double averageCloudCoverage = jsonObject.getAsJsonObject("result").getAsJsonObject("clouds").get("mean").getAsDouble();

                                    if (weatherDayList != null) {
                                        weatherDayList.get(dayNum - 1).setAvgMinTemp((int) Math.round(averageMin));
                                        weatherDayList.get(dayNum - 1).setAvgRainMM((int) Math.round(averageRainMM));
                                        weatherDayList.get(dayNum - 1).setAvgMaxTemp((int) Math.round(averageMax));
                                        weatherDayList.get(dayNum - 1).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                    }
                                } else {
                                    // Handle the error or null reponse
                                    dayNum = dayNum;
                                }
                            }
                        });
                        networkTask.execute();
                        arriveDay = arriveDay + 1;
                        arriveDay_str = Integer.toString(arriveDay);
                    }
                }
            }
        }

        //arriveMoreThan14Future == false but leaveMoreThan14Future == true
        // USE COMBO API
        else {
            int test = 1;
        }
    }
}













































////////////// DECISION TREE TO USE 14 DAY, HISTORICAL, OR COMBO APIs ////////////
//
//        // USE 14 DAY API
//        if (arriveMoreThan14Future == false && leaveMoreThan14Future == false) {
//
//            // IF ARRIVE MONTH != LEAVE MONTH //////////////////////////////////////////////////////////////////
//            if (arriveMonth != leaveMonth) {
//
//                dayNum = 0;
//                Log.d("DEBUG LOG", "arriveMonth != leaveMonth");
//                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
//                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);
//
//                if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
//                    Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//
//                } else {
//                    tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
//                    Log.d("DEBUG LOG", "tempUrl arrive: " + tempUrl);
//
//                    NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
//                        @Override
//                        public void onNetworkTaskComplete(String response) {
//                            // Do something with the response here
//                            Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);
//
//                            // Handle the response
//                            if (response != null) {
//
//                                double averageMax = 0;
//                                double averageMin = 0;
//                                double chanceOfRain = 0;
//                                double averageCloudCoverage = 0;
//                                for (int i = daysBtwNowAndArrive - 1; i < (daysBtwNowAndArrive + daysInArriveMonth) - 1; i++) {
//
//                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
//                                    Gson gson = new Gson();
//                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
//                                    JsonArray listArray = jsonObject.getAsJsonArray("list");
//                                    JsonObject firstListObject = listArray.get(i).getAsJsonObject();
//                                    JsonObject tempObject = firstListObject.getAsJsonObject("temp");
//                                    averageMax = tempObject.get("max").getAsDouble();
//                                    averageMin = tempObject.get("min").getAsDouble();
//                                    chanceOfRain = 100 * (firstListObject.get("pop").getAsDouble());
//                                    averageCloudCoverage = firstListObject.get("clouds").getAsDouble();
//                                    Log.d("DEBUG LOG", "Arrive averageMax: " + ((int) Math.round(averageMax)));
//                                    Log.d("DEBUG LOG", "Arrive averageMin: " + ((int) Math.round(averageMin)));
//                                    Log.d("DEBUG LOG", "Arrive chanceOfRain: " + ((int) Math.round(chanceOfRain)));
//                                    Log.d("DEBUG LOG", "Arrive averageCloudCoverage: " + ((int) Math.round(averageCloudCoverage)));
//
//                                    if (weatherDayList != null) {
//                                        weatherDayList.get(dayNum).setAvgMaxTemp((int) Math.round(averageMax));
//                                        weatherDayList.get(dayNum).setAvgMinTemp((int) Math.round(averageMin));
//                                        weatherDayList.get(dayNum).setChanceOfRain((int) Math.round(chanceOfRain));
//                                        weatherDayList.get(dayNum).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
//                                    }
//
//                                    dayNum = dayNum + 1;
//
//                                }
//
//                            } else {
//                                // Handle the error or null response
//                                dayNum = dayNum;
//                            }
//                        }
//                    });
//                    networkTask.execute();
//                }
//
//                Log.d("DEBUG LOG", "daysInLeaveMonth: " + daysInLeaveMonth);
//                Log.d("DEBUG LOG", "LeaveDay initial: " + leaveDay);
//
//                if (cityName.equals("") || countryCode.equals("") || leaveMonth_str.equals("")) {
//                    Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//
//                } else {
//                    tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
//                    Log.d("DEBUG LOG", "tempUrl leave: " + tempUrl);
//
//                    NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
//                        @Override
//                        public void onNetworkTaskComplete(String response) {
//                            Log.d("DEBUG LOG", "Leave NT Response in MAIN: " + response);
//
//                            // Handle the response
//                            if (response != null) {
//
//                                double averageMax = 0;
//                                double averageMin = 0;
//                                double chanceOfRain = 0;
//                                double averageCloudCoverage = 0;
//                                for (int i = daysBtwNowAndArrive + daysInArriveMonth - 1; i < (daysBtwNowAndArrive + totalTripDays) - 1; i++) {
//
//                                    Log.d("DEBUG LOG", "Leave count: " + dayNum);
//                                    Gson gson = new Gson();
//                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
//                                    JsonArray listArray = jsonObject.getAsJsonArray("list");
//                                    JsonObject firstListObject = listArray.get(i).getAsJsonObject();
//                                    JsonObject tempObject = firstListObject.getAsJsonObject("temp");
//                                    averageMax = tempObject.get("max").getAsDouble();
//                                    averageMin = tempObject.get("min").getAsDouble();
//                                    chanceOfRain = 100 * (firstListObject.get("pop").getAsDouble());
//                                    averageCloudCoverage = firstListObject.get("clouds").getAsDouble();
//                                    Log.d("DEBUG LOG", "Leave averageMax: " + ((int) Math.round(averageMax)));
//                                    Log.d("DEBUG LOG", "Leave averageMin: " + ((int) Math.round(averageMin)));
//                                    Log.d("DEBUG LOG", "Leave chanceOfRain: " + ((int) Math.round(chanceOfRain)));
//                                    Log.d("DEBUG LOG", "Leave averageCloudCoverage: " + ((int) Math.round(averageCloudCoverage)));
//
//                                    if (weatherDayList != null) {
//                                        weatherDayList.get(dayNum).setAvgMaxTemp((int) Math.round(averageMax));
//                                        weatherDayList.get(dayNum).setAvgMinTemp((int) Math.round(averageMin));
//                                        weatherDayList.get(dayNum).setChanceOfRain((int) Math.round(chanceOfRain));
//                                        weatherDayList.get(dayNum).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
//                                    }
//
//                                    dayNum = dayNum + 1;
//
//                                }
//
//                            } else {
//                                // Handle the error or null reponse
//                                dayNum = dayNum;
//                            }
//                        }
//                    });
//                    networkTask.execute();
//                }
//
//            // IF ARRIVE MONTH = LEAVE MONTH //
//            } else {
//
//                dayNum = 0;
//                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
//                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);
//
//                if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
//                    Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//
//                } else {
//                    tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
//                    Log.d("DEBUG LOG", "tempUrl: " + tempUrl);
//
//                    NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
//                        @Override
//                        public void onNetworkTaskComplete(String response) {
//                            Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);
//
//                            // Handle the response
//                            if (response != null) {
//
//                                double averageMax = 0;
//                                double averageMin = 0;
//                                double chanceOfRain = 0;
//                                double averageCloudCoverage = 0;
//                                for (int i = daysBtwNowAndArrive - 1; i < (daysBtwNowAndArrive + totalTripDays) - 1; i++) {
//
//                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
//                                    Gson gson = new Gson();
//                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
//                                    JsonArray listArray = jsonObject.getAsJsonArray("list");
//                                    JsonObject firstListObject = listArray.get(i).getAsJsonObject();
//                                    JsonObject tempObject = firstListObject.getAsJsonObject("temp");
//                                    averageMax = tempObject.get("max").getAsDouble();
//                                    averageMin = tempObject.get("min").getAsDouble();
//                                    chanceOfRain = 100 * (firstListObject.get("pop").getAsDouble());
//                                    averageCloudCoverage = firstListObject.get("clouds").getAsDouble();
//                                    Log.d("DEBUG LOG", "Arrive averageMax: " + ((int) Math.round(averageMax)));
//                                    Log.d("DEBUG LOG", "Arrive averageMin: " + ((int) Math.round(averageMin)));
//                                    Log.d("DEBUG LOG", "Arrive chanceOfRain: " + ((int) Math.round(chanceOfRain)));
//                                    Log.d("DEBUG LOG", "Arrive averageCloudCoverage: " + ((int) Math.round(averageCloudCoverage)));
//
//                                    if (weatherDayList != null) {
//                                        weatherDayList.get(dayNum).setAvgMaxTemp((int) Math.round(averageMax));
//                                        weatherDayList.get(dayNum).setAvgMinTemp((int) Math.round(averageMin));
//                                        weatherDayList.get(dayNum).setChanceOfRain((int) Math.round(chanceOfRain));
//                                        weatherDayList.get(dayNum).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
//                                    }
//
//                                    dayNum = dayNum + 1;
//
//                                }
//
//                            } else {
//                                // Handle the error or null reponse
//                                dayNum = dayNum;
//                            }
//                        }
//                    });
//                    networkTask.execute();
//                }
//            }
//
//        // USE HISTORICAL API
//        } else if (arriveMoreThan14Future && leaveMoreThan14Future) {
//
//            if (arriveMonth != leaveMonth) {
//                dayNum = 0;
//                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
//                Log.d("DEBUG LOG", "daysInLeaveMonth: " + daysInLeaveMonth);
//                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);
//                Log.d("DEBUG LOG", "leaveDayLoop initial: " + leaveDayLoop);
//
//                for (int i = 0; i < daysInArriveMonth; i++) {
//                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
//                        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;
//
//                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
//                            @Override
//                            public void onNetworkTaskComplete(String response) {
//                                Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);
//
//                                // Handle the response
//                                if (response != null) {
//
//                                    dayNum = dayNum + 1;
//                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
//                                    Gson gson = new Gson();
//                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
//                                    double averageMax = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_max").getAsDouble() - 273.15;
//                                    double averageMin = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_min").getAsDouble() - 273.15;
//                                    double averageRainMM = 100 * (jsonObject.getAsJsonObject("result").getAsJsonObject("precipitation").get("mean").getAsDouble());
//                                    double averageCloudCoverage = jsonObject.getAsJsonObject("result").getAsJsonObject("clouds").get("mean").getAsDouble();
//
//                                    if (weatherDayList != null) {
//                                        weatherDayList.get(dayNum - 1).setAvgMaxTemp((int) Math.round(averageMax));
//                                        weatherDayList.get(dayNum - 1).setAvgMinTemp((int) Math.round(averageMin));
//                                        weatherDayList.get(dayNum - 1).setAvgRainMM((int) Math.round(averageRainMM));
//                                        weatherDayList.get(dayNum - 1).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
//                                    }
//                                } else {
//                                    // Handle the error or null reponse
//                                    dayNum = dayNum;
//                                }
//                            }
//                        });
//                        networkTask.execute();
//                        arriveDay = arriveDay + 1;
//                        arriveDay_str = Integer.toString(arriveDay);
//                    }
//                }
//
//                leaveDayLoop = 1;
//                leaveDayLoop_str = Integer.toString(leaveDayLoop);
//
//                for (int i = 0; i < daysInLeaveMonth; i++) {
//                    if (cityName.equals("") || countryCode.equals("") || leaveMonth_str.equals("")) {
//                        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + leaveMonth_str + "&day=" + leaveDayLoop_str + "&appid=" + appid;
//
//                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
//                            @Override
//                            public void onNetworkTaskComplete(String response) {
//                                Log.d("DEBUG LOG", "Leave NT Response in MAIN: " + response);
//
//                                // Handle the response
//                                if (response != null) {
//
//                                    dayNum = dayNum + 1;
//                                    Log.d("DEBUG LOG", "Leave count: " + dayNum);
//                                    Gson gson = new Gson();
//                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
//                                    double averageMax = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_max").getAsDouble() - 273.15;
//                                    double averageMin = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_min").getAsDouble() - 273.15;
//                                    double averageRainMM = 100 * (jsonObject.getAsJsonObject("result").getAsJsonObject("precipitation").get("mean").getAsDouble());
//                                    double averageCloudCoverage = jsonObject.getAsJsonObject("result").getAsJsonObject("clouds").get("mean").getAsDouble();
//
//                                    if (weatherDayList != null) {
//                                        weatherDayList.get(dayNum - 1).setAvgMaxTemp((int) Math.round(averageMax));
//                                        weatherDayList.get(dayNum - 1).setAvgMinTemp((int) Math.round(averageMin));
//                                        weatherDayList.get(dayNum - 1).setAvgRainMM((int) Math.round(averageRainMM));
//                                        weatherDayList.get(dayNum - 1).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
//                                        Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
//                                    }
//
//                                } else {
//                                    // Handle the error or null reponse
//                                    dayNum = dayNum;
//                                }
//                            }
//                        });
//                        networkTask.execute();
//                        leaveDayLoop = leaveDayLoop + 1;
//                        leaveDayLoop_str = Integer.toString(leaveDayLoop);
//                    }
//                }
//
//            // IF ARRIVE MONTH = LEAVE MONTH
//            } else {
//
//                dayNum = 0;
//                Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
//                Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);
//
//                for (int i = 0; i < daysInArriveMonth; i++) {
//                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
//                        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;
//
//                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
//                            @Override
//                            public void onNetworkTaskComplete(String response) {
//                                Log.d("DEBUG LOG", "Arrive NT Response in MAIN: " + response);
//
//                                // Handle the response
//                                if (response != null) {
//
//                                    dayNum = dayNum + 1;
//                                    Log.d("DEBUG LOG", "Arrive count: " + dayNum);
//                                    Gson gson = new Gson();
//                                    JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
//                                    double averageMax = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_max").getAsDouble() - 273.15;
//                                    double averageMin = jsonObject.getAsJsonObject("result").getAsJsonObject("temp").get("average_min").getAsDouble() - 273.15;
//                                    double averageRainMM = 100 * (jsonObject.getAsJsonObject("result").getAsJsonObject("precipitation").get("mean").getAsDouble());
//                                    double averageCloudCoverage = jsonObject.getAsJsonObject("result").getAsJsonObject("clouds").get("mean").getAsDouble();
//
//                                    if (weatherDayList != null) {
//                                        weatherDayList.get(dayNum - 1).setAvgMinTemp((int) Math.round(averageMin));
//                                        weatherDayList.get(dayNum - 1).setAvgRainMM((int) Math.round(averageRainMM));
//                                        weatherDayList.get(dayNum - 1).setAvgMaxTemp((int) Math.round(averageMax));
//                                        weatherDayList.get(dayNum - 1).setAvgCloudCvrg((int) Math.round(averageCloudCoverage));
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
//                                        Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
//                                    }
//                                } else {
//                                    // Handle the error or null reponse
//                                    dayNum = dayNum;
//                                }
//                            }
//                        });
//                        networkTask.execute();
//                        arriveDay = arriveDay + 1;
//                        arriveDay_str = Integer.toString(arriveDay);
//                    }
//                }
//            }
//        }
//
//        //arriveMoreThan14Future == false but leaveMoreThan14Future == true
//        // USE COMBO API
//        else {
//            int test = 1;
//        }


























































//        if (count > daysInArriveMonth) {
//            Log.d("DEBUG LOG", "ENDcount: " + count);
//            for (int j = 0; j < daysInArriveMonth + 1; j++) {
//                weather_days_str = String.join("\n", Double.toString(weatherDayList.get(j).getAvgMinTemp()));
//                Log.d("DEBUG LOG", "ENDweatherDay[" + j + "]: " + weatherDayList.get(j).getAvgMinTemp());
//                TextView weather_days_textview = findViewById(R.id.weather_days);
//                weather_days_textview.setText(weather_days_str);
//            }
//        }








//for (int i = 0; i < daysInArriveMonth + 1; i++) {
//        if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
//        Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
//        } else {
//        tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;
//        }
//
//        // iFinal solved weird error in object array setter function seen in weatherDay[iFinal].setAvgMinTemp( etc. )
//        int iFinal = i;
//
//        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, tempUrl, null, new Response.Listener<JSONObject>() {
//
//
//@Override
//public void onResponse(JSONObject response) {
//        try {
//        count = count + 1;
//        Log.d("DEBUG LOG", "count: " + count);
//        Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
//        // Way to inspect the response from API
//        Log.d("DEBUG LOG", "Response: " + response.toString());
//        // PROBLEM !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        JSONObject jsonObjectAvgMinTemp = response.getJSONObject("result").getJSONObject("temp");
//        Double avg_min_temp = jsonObjectAvgMinTemp.getDouble("average_min") - 273.15;
//
//        if(count >= 5) {
//        notifyAll();
//        }
//
//        if (weatherDayList != null) {
//        weatherDayList.get(iFinal).setAvgMinTemp(avg_min_temp);
//        Log.d("DEBUG LOG", "avg_min_temp: " + avg_min_temp);
//        Log.d("DEBUG LOG", "weatherDay[" + iFinal + "]: " + weatherDayList.get(iFinal).getAvgMinTemp());
//        }
//
//        } catch (JSONException e) {
//        e.printStackTrace();
//        }
//        }
//        }, new Response.ErrorListener() {
//@Override
//public void onErrorResponse(VolleyError error) {
//        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
//        }
//        });
//        queue.add(request);
//
//        // increment from arrive day up until last day of the month (ARRIVEDAY++ DID NOT WORK!!!!!!!)
//        arriveDay = arriveDay + 1;
//        arriveDay_str = Integer.toString(arriveDay);
//
//        Log.d("DEBUG LOG", "ENDcount: " + count);
//        try {
//        wait();
//        } catch (InterruptedException e) {
//        throw new RuntimeException(e);
//        }
//        if (count > daysInArriveMonth) {
//        Log.d("DEBUG LOG", "ENDcount: " + count);
//        for (int j = 0; j < daysInArriveMonth + 1; j++) {
//        weather_days_str = String.join("\n", Double.toString(weatherDayList.get(i).getAvgMinTemp()));
//        Log.d("DEBUG LOG", "ENDweatherDay[" + j + "]: " + weatherDayList.get(i).getAvgMinTemp());
//        TextView weather_days_textview = findViewById(R.id.weather_days);
//        weather_days_textview.setText(weather_days_str);
//        }
//        }
//        }
















        // Wait for onResponse to finish
//        Log.d("DEBUG LOG", "count: " + count);
//        while (count <= daysInArriveMonth) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }





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