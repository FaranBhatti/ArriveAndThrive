package com.example.fireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.time.Month;
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

public class destination_weather extends AppCompatActivity {
    private final String forecast14Days_url = "https://api.openweathermap.org/data/2.5/forecast/daily";
    private final String historical_month_url = "https://history.openweathermap.org/data/2.5/aggregated/day";
    // see above API info here: https://openweathermap.org/api/statistics-api
    private final String appid = "5a055dbf56ab6f415567d8a482c453f2";
    private String tempUrl;
    private DecimalFormat df = new DecimalFormat("#.##");
    private int arriveYear;
    private String arriveYear_str;
    private String arriveYear_str_og;
    private int arriveMonth;
    private String arriveMonth_str;
    private String arriveMonth_str_og;
    private int arriveDay;
    private String arriveDay_str;
    private String arriveDay_str_og;
    private int leaveYear;
    private String leaveYear_str;
    private String leaveYear_str_og;
    private int leaveMonth;
    private String leaveMonth_str;
    private String leaveMonth_str_og;
    private int leaveDay;
    private String leaveDay_str;
    private String leaveDay_str_og;
    private int leaveDayLoop = 1;
    private String leaveDayLoop_str = "1";
    private int daysInArriveMonth;
    private int daysInLeaveMonth;
    private long totalTripDays;
    private int daysBtwNowAndArrive;
    private int daysBtwNowAndLeave;
    private int daysUntilAPICutOff;
    private int daysAfterAPICutOff;
    private int cutOffMonth;
    private int cutOffDay;
    private int num14APIDays;
    private int numHistoricalAPIDays;
    private String countryCode;
    private String cityName;
    private boolean arriveMoreThan14Future = false;
    private boolean leaveMoreThan14Future = false;
    private List<WeatherDay> weatherDayList = new ArrayList<>();
    private int dayNum = 0;
    private int tempLeaveDay;
    private String tempLeaveDay_str;
    private String weather_days_str;
    private OkHttpClient client = new OkHttpClient();
    private boolean historical = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_weather);


        // INITIALIZE EACH WeatherDay OBJECT IN weatherDayList
        for (int i = 0; i < 14; i++) {
            weatherDayList.add(new WeatherDay(0, 0, 0, 0, 0, "ERROR", 0, 0));
        }


        // CALCULATION OF CURRENT DATE GIVEN PST TIME ZONE
        LocalDate localDate = LocalDate.now();
        ZoneId zone = ZoneId.of("America/Los_Angeles"); // PST
        ZonedDateTime zonedDate = localDate.atStartOfDay(zone);
        LocalDate currentZonedDate = zonedDate.toLocalDate();


        // BUNDLE FOR RETRIEVING DATE AND LOCATION INFORMATION
        Bundle dates_and_location = getIntent().getExtras();
        if (dates_and_location != null) {
            countryCode = dates_and_location.getString("countryCode");
            cityName = dates_and_location.getString("cityName");
            arriveYear = Integer.parseInt(dates_and_location.getString("arriveYear"));
            arriveMonth = Integer.parseInt(dates_and_location.getString("arriveMonth"));
            arriveDay = Integer.parseInt(dates_and_location.getString("arriveDay"));
            leaveYear = Integer.parseInt(dates_and_location.getString("leaveYear"));
            leaveMonth = Integer.parseInt(dates_and_location.getString("leaveMonth"));
            leaveDay = Integer.parseInt(dates_and_location.getString("leaveDay"));
        }

        Log.d("bundle_data_in_weather", "countryCode: " + countryCode);
        Log.d("bundle_data_in_weather", "cityName: " + cityName);
        Log.d("bundle_data_in_weather", "arriveYear: " + arriveYear);
        Log.d("bundle_data_in_weather", "arriveMonth: " + arriveMonth);
        Log.d("bundle_data_in_weather", "arriveDay: " + arriveDay);
        Log.d("bundle_data_in_weather", "leaveYear: " + leaveYear);
        Log.d("bundle_data_in_weather", "leaveMonth: " + leaveMonth);
        Log.d("bundle_data_in_weather", "leaveDay: " + leaveDay);


        // DUMMY VARIABLES FOR TESTING
        arriveYear = 2023;
        arriveMonth = 6;
        arriveDay = 10;

        leaveYear = 2023;
        leaveMonth = 6;
        leaveDay = 23;

        countryCode = "CA";
        cityName = "Vancouver";


        // must change this to be towards end of month to test 14 DAY API leaveMonth != arriveMonth
        //currentZonedDate = LocalDate.of(2023, 3, 25);
        Log.d("DEBUG LOG", "currentZonedDate: " + currentZonedDate);



        // |_________SCENARIO_________|__ARRIVE__|__LEAVE__|__"TODAY"__|__VERIFIED__|
        // | Historical only: aM=lM   |   6/10   |  6/23   |   Real    |    YES     |
        // | Historical only: aM!=lM  |   6/25   |  7/8    |   Real    |    YES     |
        // |   14 Day only: aM=lM     |   4/2    |  4/15   |   Real    |    YES     |
        // |   14 Day only: aM!=lM    |   4/25   |  5/8    |   4/24    |    YES     |
        // |   Combo: aM=cM,lM=cM     |   2/7    |  2/20   |   2/1     |    YES     |
        // |   Combo: aM!=cM,lM=cM    |   3/27   |  4/9    |   3/25    |    YES     |

        // CONVERT MONTHS AND DAYS TO STRINGS
        arriveYear_str = Integer.toString(arriveYear);
        arriveYear_str_og = Integer.toString(arriveYear);
        arriveMonth_str_og = Integer.toString(arriveMonth);
        arriveMonth_str = Integer.toString(arriveMonth);
        arriveDay_str_og = Integer.toString(arriveDay);
        arriveDay_str = Integer.toString(arriveDay);
        leaveYear_str_og = Integer.toString(leaveYear);
        leaveYear_str = Integer.toString(leaveYear);
        leaveMonth_str_og = Integer.toString(leaveMonth);
        leaveMonth_str = Integer.toString(leaveMonth);
        leaveDay_str_og = Integer.toString(leaveDay);
        leaveDay_str = Integer.toString(leaveDay);


        // CALCULATION OF TOTAL TRIP LENGTH IN DAYS
        LocalDate arriveDate = LocalDate.of(arriveYear, arriveMonth, arriveDay);
        LocalDate leaveDate = LocalDate.of(leaveYear, leaveMonth, leaveDay);
        totalTripDays = (leaveDate.toEpochDay() - arriveDate.toEpochDay()) + 1;
        Log.d("DEBUG LOG", "totalTripDays: " + totalTripDays);


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
        } else {
            leaveMoreThan14Future = true;
        }


        // CALCULATION OF DAYS BETWEEN NOW AND ARRIVE/LEAVE
        daysBtwNowAndArrive = (int) (arriveDate.toEpochDay() - currentZonedDate.toEpochDay());
        Log.d("DEBUG LOG", "daysBtwNowAndArrive: " + daysBtwNowAndArrive);
        daysBtwNowAndLeave = (int) (leaveDate.toEpochDay() - currentZonedDate.toEpochDay());
        Log.d("DEBUG LOG", "daysBtwNowAndLeave: " + daysBtwNowAndLeave);


        // CALCULATION OF 14/15 DAY API CUTOFF MONTHS
        LocalDate cutOffDate = currentZonedDate.plusDays(14);
        Month cutOffDateMonth = cutOffDate.getMonth();
        cutOffDay = cutOffDate.getDayOfMonth();
        Log.d("DEBUG LOG", "cutOffDay: " + cutOffDay);
        cutOffMonth = cutOffDateMonth.getValue();
        Log.d("DEBUG LOG", "cutOffMonth: " + cutOffMonth);

        if ((arriveMonth != cutOffMonth) && (leaveMonth == cutOffMonth)) {
            num14APIDays = ((int) cutOffDate.toEpochDay()) - ((int) arriveDate.toEpochDay()) + 1;
            Log.d("DEBUG LOG", "num14APIDays: " + num14APIDays);
            numHistoricalAPIDays = leaveDay - cutOffDay;
            Log.d("DEBUG LOG", "numHistoricalAPIDays: " + numHistoricalAPIDays);

        } else if ((arriveMonth == cutOffMonth) && (leaveMonth == cutOffMonth)) {
            num14APIDays = cutOffDay - arriveDay + 1;
            Log.d("DEBUG LOG", "num14APIDays: " + num14APIDays);
            numHistoricalAPIDays = leaveDay - cutOffDay;
            Log.d("DEBUG LOG", "numHistoricalAPIDays: " + numHistoricalAPIDays);

        } else {
            Log.d("DEBUG LOG", "COMBO APIs not needed");
        }


        //////////// DECISION TREE TO USE 14 DAY, HISTORICAL, OR COMBO APIs ////////////
        if (totalTripDays <= 14) {

            ///////////////////////////////////////////////////////// USE 14 DAY API ////////////////////////////////////////////////////////
            if (arriveMoreThan14Future == false && leaveMoreThan14Future == false) {

                // IF ARRIVE MONTH != LEAVE MONTH
                if (arriveMonth != leaveMonth) {

                    dayNum = 0;
                    Log.d("DEBUG LOG", "arriveMonth != leaveMonth");
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                        Log.d("DEBUG LOG", "ArriveMonth != LeaveMonth 14 Day API tempUrl: " + tempUrl);

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                Log.d("DEBUG LOG", "Arrive 14 Day API Response in MAIN: " + response);

                                // Handle the response
                                if (response != null) {

                                    double averageMax = 0;
                                    double averageMin = 0;
                                    double chanceOfRain = 0;
                                    double averageCloudCoverage = 0;
                                    for (int i = daysBtwNowAndArrive - 1; i <= num14APIDays-1; i++)  {

                                        Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                        Gson gson = new Gson();
                                        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                        JsonArray listArray = jsonObject.getAsJsonArray("list");
                                        JsonObject firstListObject = listArray.get(i).getAsJsonObject();
                                        JsonObject tempObject = firstListObject.getAsJsonObject("temp");
                                        averageMax = tempObject.get("max").getAsDouble() - 273.15;
                                        averageMin = tempObject.get("min").getAsDouble() - 273.15;
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
                                            weatherDayList.get(dayNum).setApiType("FORECAST");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] ApiType: " + weatherDayList.get(dayNum).getApiType());
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


                    // 14 DAY API IF ARRIVE MONTH = LEAVE MONTH
                } else {

                    dayNum = 0;
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                        Log.d("DEBUG LOG", "ArriveMonth = LeaveMonth 14 Day API tempUrl: " + tempUrl);

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                Log.d("DEBUG LOG", "ArriveMonth = LeaveMonth 14 Day API Response: " + response);

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
                                        averageMax = tempObject.get("max").getAsDouble() - 273.15;
                                        averageMin = tempObject.get("min").getAsDouble() - 273.15;
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
                                            weatherDayList.get(dayNum).setApiType("FORECAST");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] ApiType: " + weatherDayList.get(dayNum).getApiType());
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

                //////////////////////////////////////////////////// USE HISTORICAL API /////////////////////////////////////////////////
            } else if (arriveMoreThan14Future && leaveMoreThan14Future) {

                if (arriveMonth != leaveMonth) {
                    dayNum = 0;
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "daysInLeaveMonth: " + daysInLeaveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);
                    Log.d("DEBUG LOG", "leaveDayLoop initial: " + leaveDayLoop);

                    for (int i = 0; i < daysInArriveMonth; i++) {
                        if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                            Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                        } else {
                            tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;
                            Log.d("DEBUG LOG", "ArriveMonth != LeaveMonth Historical API ARRIVE tempUrl: " + tempUrl);

                            NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                                @Override
                                public void onNetworkTaskComplete(String response) {
                                    Log.d("DEBUG LOG", "Arrive Historical API Response: " + response);

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
                                            weatherDayList.get(dayNum - 1).setApiType("HISTORICAL");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] ApiType: " + weatherDayList.get(dayNum - 1).getApiType());
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
                            Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                        } else {
                            tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + leaveMonth_str + "&day=" + leaveDayLoop_str + "&appid=" + appid;
                            Log.d("DEBUG LOG", "ArriveMonth != LeaveMonth Historical API LEAVE tempUrl: " + tempUrl);

                            NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                                @Override
                                public void onNetworkTaskComplete(String response) {
                                    Log.d("DEBUG LOG", "Leave Historical API Response: " + response);

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
                                            weatherDayList.get(dayNum - 1).setApiType("HISTORICAL");
                                            Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                            Log.d("DEBUG LOG", "Leave weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] ApiType: " + weatherDayList.get(dayNum - 1).getApiType());
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

                    // HISTORICAL API IF ARRIVE MONTH = LEAVE MONTH
                } else {

                    dayNum = 0;
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                    for (int i = 0; i < daysInArriveMonth; i++) {
                        if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                            Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                        } else {
                            tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + arriveDay_str + "&appid=" + appid;
                            Log.d("DEBUG LOG", "ArriveMonth = LeaveMonth Historical API tempUrl: " + tempUrl);

                            NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                                @Override
                                public void onNetworkTaskComplete(String response) {
                                    Log.d("DEBUG LOG", "ArriveMonth = LeaveMonth Historical Response: " + response);

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
                                            weatherDayList.get(dayNum - 1).setApiType("HISTORICAL");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] ApiType: " + weatherDayList.get(dayNum - 1).getApiType());
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

            //////////////////////////////// arriveMoreThan14Future == false but leaveMoreThan14Future == true ////////////////////////////////
            // USE COMBO API
            else {
                // COMBO BRANCH A: CALCULATION OF COMBO API CUTOFF SPANS
                daysUntilAPICutOff = 14 - daysBtwNowAndArrive;
                daysAfterAPICutOff = daysBtwNowAndLeave - 14;
                Log.d("DEBUG LOG", "ENTERED COMBO BRANCH");

                if ((arriveMonth == cutOffMonth) && (leaveMonth == cutOffMonth)) {
                    Log.d("DEBUG LOG", "ENTERED COMBO BRANCH (A)");

                    // COMBO BRANCH A: USE 14 DAY API ARRIVE MONTH = CUTOFF MONTH
                    dayNum = 0;
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                        Log.d("DEBUG LOG", "BRANCH A - 14 DAY API tempUrl: " + tempUrl);

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                Log.d("DEBUG LOG", "BRANCH A - 14 API Response: " + response);

                                // Handle the response
                                if (response != null) {

                                    double averageMax = 0;
                                    double averageMin = 0;
                                    double chanceOfRain = 0;
                                    double averageCloudCoverage = 0;
                                    for (int i = daysBtwNowAndArrive - 1; i < (daysBtwNowAndArrive + num14APIDays) - 1; i++) {

                                        Log.d("DEBUG LOG", "daysBtwNowAndArrive: " + daysBtwNowAndArrive);
                                        Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                        Gson gson = new Gson();
                                        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                        JsonArray listArray = jsonObject.getAsJsonArray("list");
                                        JsonObject firstListObject = listArray.get(i).getAsJsonObject();
                                        JsonObject tempObject = firstListObject.getAsJsonObject("temp");
                                        averageMax = tempObject.get("max").getAsDouble() - 273.15;
                                        averageMin = tempObject.get("min").getAsDouble() - 273.15;
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
                                            weatherDayList.get(dayNum).setApiType("FORECAST");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] ApiType: " + weatherDayList.get(dayNum).getApiType());
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

                    // COMBO BRANCH A: USE HISTORICAL API LEAVE MONTH = CUTOFF MONTH
                    //dayNum = 0;
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                    for (int i = cutOffDay + 1; i <= leaveDay; i++) {
                        if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                            Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                        } else {
                            tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + arriveMonth_str + "&day=" + i + "&appid=" + appid;
                            Log.d("DEBUG LOG", "BRANCH A - Historical API tempUrl: " + tempUrl);

                            NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                                @Override
                                public void onNetworkTaskComplete(String response) {
                                    Log.d("DEBUG LOG", "BRANCH A - Historical API Response: " + response);

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
                                            weatherDayList.get(dayNum - 1).setApiType("HISTORICAL");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] ApiType: " + weatherDayList.get(dayNum - 1).getApiType());
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

                    // COMBO BRANCH B
                } else if ((arriveMonth != cutOffMonth) && (leaveMonth == cutOffMonth)) {
                    Log.d("DEBUG LOG", "ENTERED COMBO BRANCH (B)");

                    // COMBO BRANCH B: USE 14 DAY API ARRIVE MONTH != CUTOFF MONTH
                    dayNum = 0;
                    Log.d("DEBUG LOG", "arriveMonth != leaveMonth");
                    Log.d("DEBUG LOG", "daysInArriveMonth: " + daysInArriveMonth);
                    Log.d("DEBUG LOG", "arriveDay initial: " + arriveDay);

                    if (cityName.equals("") || countryCode.equals("") || arriveMonth_str.equals("")) {
                        Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                    } else {
                        tempUrl = forecast14Days_url + "?q=" + cityName + "," + countryCode + "&cnt=" + 14 + "&appid=" + appid;
                        Log.d("DEBUG LOG", "BRANCH B - Arrive Month 14 Day API tempUrl: " + tempUrl);

                        NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                            @Override
                            public void onNetworkTaskComplete(String response) {
                                // Do something with the response here
                                Log.d("DEBUG LOG", "BRANCH B - Arrive Month 14 Day API Response: " + response);

                                // Handle the response
                                if (response != null) {

                                    double averageMax = 0;
                                    double averageMin = 0;
                                    double chanceOfRain = 0;
                                    double averageCloudCoverage = 0;
                                    for (int i = daysBtwNowAndArrive - 1; i <= num14APIDays; i++) {

                                        Log.d("DEBUG LOG", "Arrive count: " + dayNum);
                                        Gson gson = new Gson();
                                        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                                        JsonArray listArray = jsonObject.getAsJsonArray("list");
                                        JsonObject firstListObject = listArray.get(i).getAsJsonObject();
                                        JsonObject tempObject = firstListObject.getAsJsonObject("temp");
                                        averageMax = tempObject.get("max").getAsDouble() - 273.15;
                                        averageMin = tempObject.get("min").getAsDouble() - 273.15;
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
                                            weatherDayList.get(dayNum).setApiType("FORECAST");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMaxTemp: " + weatherDayList.get(dayNum).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgMinTemp: " + weatherDayList.get(dayNum).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] chanceOfRain: " + weatherDayList.get(dayNum).getChanceOfRain());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum) + "] ApiType: " + weatherDayList.get(dayNum).getApiType());
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


                    // COMBO BRANCH B: USE HISTORICAL API LEAVE MONTH = CUTOFF MONTH
                    Log.d("DEBUG LOG", "daysAfterCutOff: " + numHistoricalAPIDays);
                    Log.d("DEBUG LOG", "dayNum: " + dayNum);

                    tempLeaveDay = leaveDay;

                    for (int i = cutOffDay + 1; i <= (numHistoricalAPIDays + cutOffDay); i++) {
                        if (cityName.equals("") || countryCode.equals("") || leaveMonth_str.equals("")) {
                            Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();

                        } else {
                            tempUrl = historical_month_url + "?q=" + cityName + "," + countryCode + "&month=" + leaveMonth_str + "&day=" + leaveDay_str + "&appid=" + appid;
                            Log.d("DEBUG LOG", "BRANCH B - Historical API tempUrl: " + tempUrl);

                            NetworkTask networkTask = new NetworkTask(tempUrl, new NetworkTaskListener() {
                                @Override
                                public void onNetworkTaskComplete(String response) {
                                    Log.d("DEBUG LOG", "BRANCH B - Historical API Response: " + response);

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
                                            weatherDayList.get(dayNum - 1).setApiType("HISTORICAL");
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMaxTemp: " + weatherDayList.get(dayNum - 1).getAvgMaxTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgMinTemp: " + weatherDayList.get(dayNum - 1).getAvgMinTemp());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgRainMM: " + weatherDayList.get(dayNum - 1).getAvgRainMM());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] AvgCloudCvrg: " + weatherDayList.get(dayNum - 1).getAvgCloudCvrg());
                                            Log.d("DEBUG LOG", "Arrive weatherDay[" + (dayNum - 1) + "] ApiType: " + weatherDayList.get(dayNum - 1).getApiType());
                                        }
                                    } else {
                                        // Handle the error or null reponse
                                        dayNum = dayNum;
                                    }
                                }
                            });
                            networkTask.execute();
                            tempLeaveDay = tempLeaveDay + 1;
                            tempLeaveDay_str = Integer.toString(tempLeaveDay);
                        }
                    }

                } else {
                    Toast.makeText(destination_weather.this, "ERROR!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(destination_weather.this, "ERROR! Trip longer than 14 Days!", Toast.LENGTH_SHORT).show();
            Log.d("DEBUG LOG", "ERROR! totalTripDays: " + totalTripDays);
        }

        String disclaimer = "PLEASE NOTE: The weather for any days of your trip that are more than 14 days in the future will" +
                " be predicted using historical statistical data. Historical data can only provide rainfall in mm, not" +
                " % chance of rain. Thank you for using ArriveAndThrive!";
        TextView textView_disclaimer = findViewById(R.id.textView_disclaimer);
        textView_disclaimer.setText(disclaimer);

    }

    public void onClickGenWeather(View viewGenWeather) {
        Log.d("DEBUG LOG", "Inside onClickGenWeather!");
        Log.d("DEBUG LOG", "onClick weatherDay[" + 0 + "] AvgMaxTemp: " + weatherDayList.get(0).getAvgMaxTemp());
        Log.d("DEBUG LOG", "onClick weatherDay[" + 0 + "] AvgMinTemp: " + weatherDayList.get(0).getAvgMinTemp());
        Log.d("DEBUG LOG", "onClick weatherDay[" + 0 + "] AvgRainMM: " + weatherDayList.get(0).getAvgRainMM());
        Log.d("DEBUG LOG", "onClick weatherDay[" + 0 + "] AvgRainMM: " + weatherDayList.get(0).getChanceOfRain());
        Log.d("DEBUG LOG", "onClick weatherDay[" + 0 + "] AvgCloudCvrg: " + weatherDayList.get(0).getAvgCloudCvrg());
        Log.d("DEBUG LOG", "onClick weatherDay[" + 0 + "] ApiType: " + weatherDayList.get(0).getApiType());

        Intent intent_MainActivity = new Intent(this, display_weather_report.class);
        Bundle bundle_MainActivity = new Bundle();
        bundle_MainActivity.putSerializable("weatherDayList",(Serializable)weatherDayList);
        intent_MainActivity.putExtra("weatherDayList", bundle_MainActivity);

        intent_MainActivity.putExtra("countryCode", countryCode);
        intent_MainActivity.putExtra("cityName", cityName);
        intent_MainActivity.putExtra("arriveYear", arriveYear_str_og);
        intent_MainActivity.putExtra("arriveMonth", arriveMonth_str_og);
        intent_MainActivity.putExtra("arriveDay", arriveDay_str_og);
        Log.d("DEBUG LOG", "arriveDay MAIN ACTIVITY BUNDLE: " + arriveDay_str_og);
        intent_MainActivity.putExtra("leaveYear", leaveYear_str_og);
        intent_MainActivity.putExtra("leaveMonth", leaveMonth_str_og);
        intent_MainActivity.putExtra("leaveDay", leaveDay_str_og);
        startActivity(intent_MainActivity);
    }
}