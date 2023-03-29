package com.example.arriveandthrive;

public class WeatherDay {


    // INSTANCE VARIABLES
    private Double avgMinTemp;

    // CONSTRUCTOR
    public WeatherDay(Double avgMinTemp) {
        this.avgMinTemp = avgMinTemp;
    }

    // GETTER
    public Double getAvgMinTemp() {
        return avgMinTemp;
    }

    // SETTER
    public void setAvgMinTemp(Double avgMinTemp) {
        this.avgMinTemp = avgMinTemp;
    }
}




//
//    // ALTERNATE/OVERRIDEN??? CONSTRUCTOR
//    public WeatherDay(double avgMinTemp) {
//        this.avgMinTemp = avgMinTemp;
//    }