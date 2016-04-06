package com.example.android.sunshine.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class MainActivity extends Activity
{

    String TAG = "wear device";
    weatherChange receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.rect_activity_main);
            receiver = new weatherChange();
            IntentFilter filter = new IntentFilter(WeatherListener.WEATHER_ACTION_REFRESH);
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
            populateUI();
        }

    @Override
    protected void onDestroy()
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            super.onDestroy();
        }

    void populateUI()
        {
            WeatherPreferences preferences = WeatherPreferences.getInstance(this);
            TextView temp = ((TextView) findViewById(R.id.max_temp));
            long date = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy ");
            String dateString = sdf.format(date);
            Log.d("watch", "string date " + dateString);
            ((TextView) findViewById(R.id.date)).setText(dateString);
            if (preferences.getWeathererror() != null)
                {
                    temp.setText(preferences.getWeathererror());
                    sendMsgToFetchData();
                    return;
                }
            WeatherModel weatherModel = preferences.getWeatherInfo();
            if (weatherModel.maxTemp == null)
                {
                    temp.setText("No weather data please refresh");
                    sendMsgToFetchData();
                    return;
                }
            Log.d("watch", "string date " + dateString);
            temp.setText(weatherModel.maxTemp + "  " + weatherModel.minTemp);
            temp.setCompoundDrawablesRelativeWithIntrinsicBounds(Drawable.createFromPath
                            (weatherModel.Url), null,
                    null, null);

        }
void sendMsgToFetchData(){
    Intent intent=new Intent(this,WeatherListener.class);
    intent.setAction(WeatherListener.WEATHER_ACTION_FETCH);
    startService(intent);
}
    class weatherChange extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
            {
                Log.d("watch", "weatherChange");
                populateUI();
            }
    }
}
