package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Shweta
 */
public class WeatherPreferences
{

    private final String URL = "URL";
    private final String MIN_TEMP = "MIN_TEMP";
    private final String MAX_TEMP = "MAX_TEMP";
    private final String ERROR = "ERROR";

    private static WeatherPreferences _Instance;
    private SharedPreferences prefs;

    private WeatherPreferences(Context mContext)
        {
            prefs = mContext.getSharedPreferences(mContext.getPackageName(),
                    Context.MODE_PRIVATE);
        }

    public synchronized static WeatherPreferences getInstance(Context mContext)
        {
            if (_Instance == null)
                {
                    _Instance = new WeatherPreferences(mContext);
                }
            return _Instance;
        }

    public WeatherModel getWeatherInfo()
        {
            WeatherModel weatherModel = new WeatherModel();
            weatherModel.maxTemp = prefs.getString(MAX_TEMP, null);
            weatherModel.minTemp = prefs.getString(MIN_TEMP, null);
            weatherModel.Url = prefs.getString(URL, null);
            return weatherModel;
        }

    public void setWeatherInfo(WeatherModel value)
        {
            Log.d("pref","WeatherModel "+value);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MAX_TEMP, value.maxTemp);
            editor.putString(MIN_TEMP, value.minTemp);
            editor.putString(URL, value.Url);
            editor.commit();
            setWeathererror(null);
        }

    public void setWeathererror(String value)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ERROR, value);
            editor.commit();
        }

    public String getWeathererror()
        {
            return prefs.getString(ERROR, null);
        }
}
