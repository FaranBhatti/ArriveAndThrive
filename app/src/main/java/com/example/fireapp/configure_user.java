package com.example.fireapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.LocalDate;
public class configure_user extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_user);

        // obtaining the buttons
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnBack = findViewById(R.id.btnBack);

        // obtaining the spinners for city and country code
        Spinner citySpinner = findViewById(R.id.city_spinner);
        Spinner countrySpinner = findViewById(R.id.country_code_spinner);

        // obtaining the spinners for departure dates
        Spinner departMonthSpinner = findViewById(R.id.departure_month_spinner);
        Spinner departDaySpinner = findViewById(R.id.departure_day_spinner);
        Spinner departYearSpinner = findViewById(R.id.departure_year_spinner);

        // obtaining the spinners for arrival dates
        Spinner arriveMonthSpinner = findViewById(R.id.arrival_month_spinner);
        Spinner arriveDaySpinner = findViewById(R.id.arrival_day_spinner);
        Spinner arriveYearSpinner = findViewById(R.id.arrival_year_spinner);

        // btnBacks on click listener to go back to the previous activity
        btnBack.setOnClickListener(view -> finish());


        // update the city spinner based on the selected country
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedCountryCode = adapterView.getSelectedItem().toString(); // get the selected country code
                updateCitySpinner(selectedCountryCode); // update the city spinner based on the selected country
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // update the departDaySpinner based on the departMonthSpinner
        departMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedMonth = adapterView.getSelectedItem().toString(); // get the selected month
                updateDepartDaySpinner(selectedMonth); // update the departDaySpinner based on the selected month
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // update the arriveDaySpinner based on the arriveMonthSpinner
        arriveMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedMonth = adapterView.getSelectedItem().toString(); // get the selected month
                updateArriveDaySpinner(selectedMonth); // update the arriveDaySpinner based on the selected month
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // btnConfirm on click listener to confirm the user's input
        btnConfirm.setOnClickListener(v -> {

            // obtain city, country code and length of trip
            String cityName = citySpinner.getSelectedItem().toString();
            String countryCode = countrySpinner.getSelectedItem().toString();

            // obtain departure date
            String departMonth = departMonthSpinner.getSelectedItem().toString();
            String departDay = departDaySpinner.getSelectedItem().toString();
            String departYear = departYearSpinner.getSelectedItem().toString();

            // obtain arrive date
            String arriveMonth = arriveMonthSpinner.getSelectedItem().toString();
            String arriveDay = arriveDaySpinner.getSelectedItem().toString();
            String arriveYear = arriveYearSpinner.getSelectedItem().toString();

            // function converting the month to a number
            departMonth = String.valueOf(convertMonth(departMonth));
            arriveMonth = String.valueOf(convertMonth(arriveMonth));

            if (error_check(departMonth, departDay, departYear, arriveMonth, arriveDay, arriveYear)) {
                // store those in a bundle and pass it to the next activity
                Bundle bundle = new Bundle();
                bundle.putString("city", cityName);
                bundle.putString("countryCode", countryCode);

                // arrive date
                bundle.putString("arriveMonth", arriveMonth);
                bundle.putString("arriveDay", arriveDay);
                bundle.putString("arriveYear", arriveYear);

                // departure date
                bundle.putString("leaveMonth", departMonth);
                bundle.putString("leaveDay", departDay);
                bundle.putString("leaveYear", departYear);

                // create a new intent and pass the bundle to the next activity
                Intent intent = new Intent(configure_user.this, home_page.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            else {
                // if error_check function is false then do something
                Toast.makeText(configure_user.this, "Please enter valid date inputs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // function to update the arriveDaySpinner based on the selected month
    private void updateArriveDaySpinner(String selectedMonth) {
        ArrayAdapter<CharSequence> arriveDayAdapter;
        Spinner arriveDaySpinner = findViewById(R.id.arrival_day_spinner);

        // create an adapter for the departDaySpinner based on the selected month
        if (selectedMonth.equals("February")) {
            arriveDayAdapter = ArrayAdapter.createFromResource(this, R.array.feb_days, android.R.layout.simple_spinner_item);
        }
        else if (selectedMonth.equals("April") || selectedMonth.equals("June") || selectedMonth.equals("September") || selectedMonth.equals("November")) {
            arriveDayAdapter = ArrayAdapter.createFromResource(this, R.array.apr_jun_sep_nov_days, android.R.layout.simple_spinner_item);
        }
        else {
            arriveDayAdapter = ArrayAdapter.createFromResource(this, R.array.jan_mar_may_jul_aug_oct_dec_days, android.R.layout.simple_spinner_item);
        }

        // Set the adapter for the departure day spinner
        arriveDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arriveDaySpinner.setAdapter(arriveDayAdapter);
    }

    // function to update the departDaySpinner based on the selected month
    private void updateDepartDaySpinner(String selectedMonth) {
        ArrayAdapter<CharSequence> departDayAdapter;
        Spinner departDaySpinner = findViewById(R.id.departure_day_spinner);

        // create an adapter for the departDaySpinner based on the selected month
        if (selectedMonth.equals("February")) {
            departDayAdapter = ArrayAdapter.createFromResource(this, R.array.feb_days, android.R.layout.simple_spinner_item);
        }
        else if (selectedMonth.equals("April") || selectedMonth.equals("June") || selectedMonth.equals("September") || selectedMonth.equals("November")) {
            departDayAdapter = ArrayAdapter.createFromResource(this, R.array.apr_jun_sep_nov_days, android.R.layout.simple_spinner_item);
        }
        else {
            departDayAdapter = ArrayAdapter.createFromResource(this, R.array.jan_mar_may_jul_aug_oct_dec_days, android.R.layout.simple_spinner_item);
        }

        // Set the adapter for the departure day spinner
        departDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departDaySpinner.setAdapter(departDayAdapter);
    }

    // function to convert the month to a number
    private int convertMonth(String Month) {
        int monthNumber = 0;

        switch (Month.toLowerCase()) {
            case "january":
                monthNumber = 1;
                break;
            case "february":
                monthNumber = 2;
                break;
            case "march":
                monthNumber = 3;
                break;
            case "april":
                monthNumber = 4;
                break;
            case "may":
                monthNumber = 5;
                break;
            case "june":
                monthNumber = 6;
                break;
            case "july":
                monthNumber = 7;
                break;
            case "august":
                monthNumber = 8;
                break;
            case "september":
                monthNumber = 9;
                break;
            case "october":
                monthNumber = 10;
                break;
            case "november":
                monthNumber = 11;
                break;
            case "december":
                monthNumber = 12;
                break;
            default:
                // Throw an exception or return a default value if the month string is invalid
                break;
        }
        return monthNumber;
    }

    // function to check for errors in the date inputs
    private boolean error_check(String departMonth, String departDay, String departYear, String arriveMonth, String arriveDay, String arriveYear) {

        // Obtaining the amount of days between the trip
        LocalDate arriveDate = LocalDate.of(Integer.parseInt(arriveYear), Integer.parseInt(arriveMonth), Integer.parseInt(arriveDay));
        LocalDate leaveDate = LocalDate.of(Integer.parseInt(departYear), Integer.parseInt(departMonth), Integer.parseInt(departDay));
        long totalTripDays = (leaveDate.toEpochDay() - arriveDate.toEpochDay()) + 1;

        // log output for the totalTripDays
        Log.d("totalTripDays", String.valueOf(totalTripDays));

        // log output for arriveDate and leaveDate
        Log.d("arriveDate", String.valueOf(arriveDate));
        Log.d("leaveDate", String.valueOf(leaveDate));


        // check if departure data is before the arrival date and if the dates are within 14 days of each other
        if (arriveDate.isBefore(leaveDate) && totalTripDays <= 14) {
            // toast for successfully configuring user
            Toast.makeText(configure_user.this, "Successfully configured user", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            return false;
        }
    }

    // dynamically updating the city spinner
    private void updateCitySpinner(String countryCode) {
        ArrayAdapter<CharSequence> cityAdapter;
        Spinner citySpinner = findViewById(R.id.city_spinner);

        // Create an adapter for the city spinner based on the selected country code
        switch (countryCode) {
            case "US":
                cityAdapter = ArrayAdapter.createFromResource(this, R.array.us_cities, android.R.layout.simple_spinner_item);
                break;
            case "CA":
                cityAdapter = ArrayAdapter.createFromResource(this, R.array.ca_cities, android.R.layout.simple_spinner_item);
                break;
            case "CH":
                cityAdapter = ArrayAdapter.createFromResource(this, R.array.ch_cities, android.R.layout.simple_spinner_item);
                break;
            case "DE":
                cityAdapter = ArrayAdapter.createFromResource(this, R.array.de_cities, android.R.layout.simple_spinner_item);
                break;
            case "SE":
                cityAdapter = ArrayAdapter.createFromResource(this, R.array.se_cities, android.R.layout.simple_spinner_item);
                break;
            default:
                cityAdapter = ArrayAdapter.createFromResource(this, R.array.empty_cities, android.R.layout.simple_spinner_item);
                break;
        }
        // Set the adapter for the city spinner
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);
    }
}