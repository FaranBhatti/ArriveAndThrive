package com.example.arriveandthrive;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class WeatherDisplay extends AppCompatActivity {

    private List<WeatherDay> weatherDayList = new ArrayList<>();
    private List<WeatherDay> weatherDayList1 = new ArrayList<>();
    private String countryCode_str;
    private String cityName_str;
    private int arriveYear;
    private String arriveYear_str;
    private int arriveMonth;
    private String arriveMonth_str;
    private int arriveDay;
    private String arriveDay_str;
    private int leaveYear;
    private String leaveYear_str;
    private int leaveMonth;
    private String leaveMonth_str;
    private int leaveDay;
    private String leaveDay_str;
    private Month tempMonth;
    private String apiType;
    private String monthAbrv;
    private int resID;
    private int month;
    private int day;
    private String DEGC = "\u00B0" + "C";

    private LocalDate arriveDate;
    private LocalDate leaveDate;
    private LocalDate tempDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_display);

        Bundle extras_MainActivity = getIntent().getExtras();
        if (extras_MainActivity != null) {

            Intent intent = getIntent();
            Bundle args = intent.getBundleExtra("weatherDayList");
            weatherDayList = (List<WeatherDay>) args.getSerializable("weatherDayList");


            Log.d("DEBUG LOG", "Inside WeatherDisplay Bundle!");
            Log.d("DEBUG LOG", "WeatherDisplay weatherDay[" + 0 + "] AvgMaxTemp: " + weatherDayList.get(0).getAvgMaxTemp());
            Log.d("DEBUG LOG", "WeatherDisplay weatherDay[" + 0 + "] AvgMinTemp: " + weatherDayList.get(0).getAvgMinTemp());
            Log.d("DEBUG LOG", "WeatherDisplay weatherDay[" + 0 + "] AvgRainMM: " + weatherDayList.get(0).getAvgRainMM());
            Log.d("DEBUG LOG", "WeatherDisplay weatherDay[" + 0 + "] AvgRainMM: " + weatherDayList.get(0).getChanceOfRain());
            Log.d("DEBUG LOG", "WeatherDisplay weatherDay[" + 0 + "] AvgCloudCvrg: " + weatherDayList.get(0).getAvgCloudCvrg());
            Log.d("DEBUG LOG", "WeatherDisplay weatherDay[" + 0 + "] ApiType: " + weatherDayList.get(0).getApiType());


            countryCode_str = extras_MainActivity.getString("countryCode");
            cityName_str = extras_MainActivity.getString("cityName");
            arriveYear_str = extras_MainActivity.getString("arriveYear");
            arriveMonth_str = extras_MainActivity.getString("arriveMonth");
            arriveDay_str = extras_MainActivity.getString("arriveDay");
            leaveYear_str = extras_MainActivity.getString("leaveYear");
            leaveMonth_str = extras_MainActivity.getString("leaveMonth");
            leaveDay_str = extras_MainActivity.getString("leaveDay");

            arriveYear = parseInt(arriveYear_str);
            arriveMonth = parseInt(arriveMonth_str);
            arriveDay = parseInt(arriveDay_str);
            leaveYear = parseInt(leaveYear_str);
            leaveMonth = parseInt(leaveMonth_str);
            leaveDay = parseInt(leaveDay_str);

            Log.d("DEBUG LOG", "WeatherDisplay arriveYear int:" + arriveYear);
            Log.d("DEBUG LOG", "WeatherDisplay arriveMonth int:" + arriveMonth);
            Log.d("DEBUG LOG", "WeatherDisplay arriveDay int:" + arriveDay);
            Log.d("DEBUG LOG", "WeatherDisplay leaveYear int:" + leaveYear);
            Log.d("DEBUG LOG", "WeatherDisplay leaveMonth int:" + leaveMonth);
            Log.d("DEBUG LOG", "WeatherDisplay leaveDay int:" + leaveDay);

            LocalDate arriveDate = LocalDate.of(arriveYear, arriveMonth, arriveDay);
            LocalDate leaveDate = LocalDate.of(leaveYear, leaveMonth, leaveDay);
            tempDate = arriveDate;

            for (int i = 0; i < weatherDayList.size(); i++) {
                if(weatherDayList.get(i).getApiType().equals("ERROR")) {
                    // do nothing???
                } else {
                    if (i == 0) {
                        tempDate = arriveDate;
                        Log.d("DEBUG LOG", "tempDate: " + tempDate);
                        tempMonth = tempDate.getMonth();
                        month = tempMonth.getValue();
                        Log.d("DEBUG LOG", "month: " + month);
                        day = i;
                        Log.d("DEBUG LOG", "day: " + day);
                    } else {
                        tempDate = arriveDate.plusDays((long) i);
                        tempMonth = tempDate.getMonth();
                        Log.d("DEBUG LOG", "tempDate: " + tempDate);
                        month = tempMonth.getValue();
                        Log.d("DEBUG LOG", "month: " + month);
                        day = i;
                        Log.d("DEBUG LOG", "day: " + day);
                    }
                    switch (month) {
                        case 1:
                            monthAbrv = "JAN";
                            break;
                        case 2:
                            monthAbrv = "FEB";
                            break;
                        case 3:
                            monthAbrv = "MAR";
                            break;
                        case 4:
                            monthAbrv = "APR";
                            break;
                        case 5:
                            monthAbrv = "MAY";
                            break;
                        case 6:
                            monthAbrv = "JUN";
                            break;
                        case 7:
                            monthAbrv = "JUL";
                            break;
                        case 8:
                            monthAbrv = "AUG";
                            break;
                        case 9:
                            monthAbrv = "SEP";
                            break;
                        case 10:
                            monthAbrv = "OCT";
                            break;
                        case 11:
                            monthAbrv = "NOV";
                            break;
                        case 12:
                            monthAbrv = "DEC";
                            break;
                        default:
                            monthAbrv = "Invalid month number";
                            break;
                    }

                    String summaryBanner = "Summary of Weather for trip to\n" + cityName_str + "," + countryCode_str + " from\n"
                                            + arriveMonth_str + "/" + arriveDay_str + "/" + arriveYear_str + " to "
                                            + leaveMonth_str + "/" + leaveDay_str + "/" + leaveYear_str;
                    TextView textView_summary = findViewById(R.id.summary_banner);
                    textView_summary.setText(summaryBanner);

                    String tempMonthString = "textView_monthday" + String.valueOf(day);
                    Log.d("DEBUG LOG", "tempMonthDayString for resID: " + tempMonthString);
                    resID = getResources().getIdentifier(tempMonthString, "id", getPackageName());
                    TextView textView_month = findViewById(resID);
                    textView_month.setText(monthAbrv);
                    Log.d("DEBUG LOG", "Month for Display: " + tempMonth.getValue());

                    String tempDayString = "textView_day" + String.valueOf(day);
                    Log.d("DEBUG LOG", "tempDayString for resID: " + tempDayString);
                    resID = getResources().getIdentifier(tempDayString, "id", getPackageName());
                    TextView textView_day = findViewById(resID);
                    textView_day.setText(String.valueOf(tempDate.getDayOfMonth()));
                    Log.d("DEBUG LOG", "dayOfMonth for Display: " + tempDate.getDayOfMonth());

                    String tempApiString = "textView_api" + String.valueOf(day);
                    Log.d("DEBUG LOG", "tempApiString for resID: " + tempApiString);
                    resID = getResources().getIdentifier(tempApiString, "id", getPackageName());
                    TextView textView_api = findViewById(resID);
                    textView_api.setText(weatherDayList.get(i).getApiType());
                    Log.d("DEBUG LOG", "API for Display: " + weatherDayList.get(i).getApiType());

                    String tempMaxString = "textView_max" + String.valueOf(day);
                    Log.d("DEBUG LOG", "tempMaxString for resID: " + tempMaxString);
                    resID = getResources().getIdentifier(tempMaxString, "id", getPackageName());
                    TextView textView_max = findViewById(resID);
                    textView_max.setText("High\n" + weatherDayList.get(i).getAvgMaxTemp() + DEGC);
                    Log.d("DEBUG LOG", "MAX for Display: " + weatherDayList.get(i).getAvgMaxTemp());

                    String tempMinString = "textView_min" + String.valueOf(day);
                    Log.d("DEBUG LOG", "tempMinString for resID: " + tempMinString);
                    resID = getResources().getIdentifier(tempMinString, "id", getPackageName());
                    TextView textView_min = findViewById(resID);
                    textView_min.setText("Low\n" + weatherDayList.get(i).getAvgMinTemp() + DEGC);
                    Log.d("DEBUG LOG", "MIN for Display: " + weatherDayList.get(i).getAvgMinTemp() + DEGC);

                    if (weatherDayList.get(i).getApiType().equals("HISTORICAL")) {
                        String tempRainString = "textView_rain" + String.valueOf(day);
                        Log.d("DEBUG LOG", "tempRainString for resID: " + tempRainString);
                        resID = getResources().getIdentifier(tempRainString, "id", getPackageName());
                        TextView textView_rain = findViewById(resID);
                        textView_rain.setText("Rain\n" + weatherDayList.get(i).getAvgMinTemp() + "mm");
                        Log.d("DEBUG LOG", "RAIN for Display: " + weatherDayList.get(i).getAvgRainMM() + "mm");
                    } else {
                        String tempRainString = "textView_rain" + String.valueOf(day);
                        Log.d("DEBUG LOG", "tempRainString for resID: " + tempRainString);
                        resID = getResources().getIdentifier(tempRainString, "id", getPackageName());
                        TextView textView_rain = findViewById(resID);
                        textView_rain.setText("Rain\n" + weatherDayList.get(i).getChanceOfRain() + "%");
                        Log.d("DEBUG LOG", "RAIN for Display: " + weatherDayList.get(i).getChanceOfRain() + "%");
                    }

                    String tempCloudsString = "textView_clouds" + String.valueOf(day);
                    Log.d("DEBUG LOG", "tempCloudsString for resID: " + tempCloudsString);
                    resID = getResources().getIdentifier(tempCloudsString, "id", getPackageName());
                    TextView textView_clouds = findViewById(resID);
                    textView_clouds.setText("Cloud\n" + weatherDayList.get(i).getAvgCloudCvrg() + "%");
                    Log.d("DEBUG LOG", "CLOUDS for Display: " + weatherDayList.get(i).getAvgCloudCvrg() + "%");
                }
            }
        }
    }
}
