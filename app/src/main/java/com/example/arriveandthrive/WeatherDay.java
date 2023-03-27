package com.example.arriveandthrive;

public class WeatherDay {


    // INSTANCE VARIABLES
    private double avgMinTemp;


    // DEFAULT CONSTRUCTOR
    public WeatherDay() {
        avgMinTemp = 0.0;
    }


    // CALCULATE CONSTRUCTOR
    public WeatherDay(double avgMinTemp) {
        this.avgMinTemp = avgMinTemp;
    }


    // ACCESSORS
    public double getAvgMinTemp() {
        return avgMinTemp;
    }


    // MUTATORS
    public void setAvgMinTemp(double newAvgMinTemp) {
        avgMinTemp = newAvgMinTemp;
    }
}
