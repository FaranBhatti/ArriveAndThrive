package com.example.arriveandthrive;

public class WeatherDay {


    // INSTANCE VARIABLES
    private int avgMaxTemp;
    private int avgMinTemp;
    private int avgRainMM;
    private int chanceOfRain;
    private int avgCloudCvrg;

    // CONSTRUCTOR
    public WeatherDay(int avgMaxTemp, int avgMinTemp, int avgRainMM, int chanceOfRain, int avgCloudCvrg) {
        this.avgMaxTemp = avgMaxTemp;
        this.avgMinTemp = avgMinTemp;
        this.avgRainMM = avgRainMM;
        this.chanceOfRain = chanceOfRain;
        this.avgCloudCvrg = avgCloudCvrg;
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
}




//
//    // ALTERNATE/OVERRIDEN??? CONSTRUCTOR
//    public WeatherDay(double avgMinTemp) {
//        this.avgMinTemp = avgMinTemp;
//    }