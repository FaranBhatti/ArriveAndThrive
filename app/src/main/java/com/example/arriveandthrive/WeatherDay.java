package com.example.arriveandthrive;

import java.io.Serializable;

public class WeatherDay implements Serializable {


    // INSTANCE VARIABLES
    private int avgMaxTemp;
    private int avgMinTemp;
    private int avgRainMM;
    private int chanceOfRain;
    private int avgCloudCvrg;
    private String apiType;
    private int month;
    private int day;


    // CONSTRUCTOR
    public WeatherDay(int avgMaxTemp, int avgMinTemp, int avgRainMM, int chanceOfRain, int avgCloudCvrg, String apiType, int month, int day) {
        this.avgMaxTemp = avgMaxTemp;
        this.avgMinTemp = avgMinTemp;
        this.avgRainMM = avgRainMM;
        this.chanceOfRain = chanceOfRain;
        this.avgCloudCvrg = avgCloudCvrg;
        this.apiType = apiType;
    }

    // GETTER
    public int getAvgMaxTemp() {
        return avgMaxTemp;
    }
    public int getAvgMinTemp() {
        return avgMinTemp;
    }
    public int getAvgRainMM() {
        return avgRainMM;
    }
    public int getChanceOfRain() {
        return chanceOfRain;
    }
    public int getAvgCloudCvrg() {
        return avgCloudCvrg;
    }
    public String getApiType() {
        return apiType;
    }
    public int getMonth() {
        return month;
    }
    public int getDay() {
        return day;
    }


    // SETTERS
    public void setAvgMaxTemp(int avgMaxTemp) {
        this.avgMaxTemp = avgMaxTemp;
    }
    public void setAvgMinTemp(int avgMinTemp) {
        this.avgMinTemp = avgMinTemp;
    }
    public void setAvgRainMM(int avgRainMM) {
        this.avgRainMM = avgRainMM;
    }
    public void setChanceOfRain(int chanceOfRain) {
        this.chanceOfRain = chanceOfRain;
    }
    public void setAvgCloudCvrg(int avgCloudCvrg) {
        this.avgCloudCvrg = avgCloudCvrg;
    }
    public void setApiType(String apiType) {
        this.apiType = apiType;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public void setDay(int day) {
        this.day = day;
    }

}




//
//    // ALTERNATE/OVERRIDEN??? CONSTRUCTOR
//    public WeatherDay(double avgMinTemp) {
//        this.avgMinTemp = avgMinTemp;
//    }