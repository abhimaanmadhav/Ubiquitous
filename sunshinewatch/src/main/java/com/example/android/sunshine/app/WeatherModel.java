package com.example.android.sunshine.app;

/**
 * Created by Abhimaan on 05/04/16.
 */
public class WeatherModel
{
    String maxTemp, minTemp;
    String Url;

    @Override
    public String toString()
        {
            return "WeatherModel{" +
                    "maxTemp=" + maxTemp +
                    ", minTemp=" + minTemp +
                    ", Url='" + Url + '\'' +
                    '}';
        }
}
