package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherFace extends CanvasWatchFaceService
{
    public WeatherFace()
        {
        }


    @Override
    public Engine onCreateEngine()
        {
            return new Engine();
        }


    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine
    {
        Paint mBackgroundPaint, mDatePaint;
        String dateString;
        WeatherModel model;
        LinearLayout view;
        TextView digitclock, dateText, temp;
        SimpleDateFormat sdf;
        SimpleDateFormat date;
        Drawable image;
        weatherChange receiver;


        @Override
        public void onCreate(SurfaceHolder holder)
            {
                super.onCreate(holder);
                receiver = new weatherChange();
                IntentFilter filter = new IntentFilter(WeatherListener.WEATHER_ACTION_REFRESH);
                LocalBroadcastManager.getInstance(WeatherFace.this).registerReceiver(receiver,
                        filter);
                Log.e("oncreate", "engine");
                view = (LinearLayout) LayoutInflater.from(WeatherFace.this).inflate(R.layout
                        .watchface_layout, null);
                digitclock = (TextView) view.findViewById(R.id.time);
                temp = ((TextView) view.findViewById(R.id.max_temp));
                dateText = ((TextView) view.findViewById(R.id.date));
                sdf = new SimpleDateFormat("hh:mm ");
                date = new SimpleDateFormat("dd/MM/yyyy ");
                dateText.setText(date.format(new Date()));
                digitclock.setText(sdf.format(new Date()));
                populateUI();
                mBackgroundPaint = new Paint();
                mBackgroundPaint.setColor(Color.BLUE);
            }


        @Override
        public void onPropertiesChanged(Bundle properties)
            {
                super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
            }


        @Override
        public void onTimeTick()
            {
                super.onTimeTick();
                Log.e("weaterface", "tick " + sdf.format(new Date()));
                digitclock.setText(sdf.format(new Date()));
                invalidate();
            /* the time changed */
            }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode)
            {
                Log.d("watch face", "onAmbientModeChanged");
                if (inAmbientMode)
                    {
                        mBackgroundPaint.setAntiAlias(false);
                        mBackgroundPaint.setColor(Color.BLACK);
                        temp.setCompoundDrawables(null, null, null, null);
                    } else
                    {
                        mBackgroundPaint.setAntiAlias(true);
                        mBackgroundPaint.setColor(Color.BLUE);
                        populateUI();
                    }
                super.onAmbientModeChanged(inAmbientMode);
                invalidate();
            /* the wearable switched between modes */
            }


        @Override
        public void onDraw(Canvas canvas, Rect bounds)
            {
            /* draw your watch face */
                canvas.drawPaint(mBackgroundPaint);
                view.measure(bounds.width(), bounds.height());
                view.layout(0, 0, bounds.width(), bounds.height());
                view.draw(canvas);

            }


        @Override
        public void onVisibilityChanged(boolean visible)
            {
                super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
                if (visible)
                    {
                        digitclock.setText(sdf.format(new Date()));
                        invalidate();
                    }
            }


        @Override
        public void onDestroy()
            {
                super.onDestroy();
                LocalBroadcastManager.getInstance(WeatherFace.this).unregisterReceiver(receiver);
            }


        void populateUI()
            {
                WeatherPreferences preferences = WeatherPreferences.getInstance(WeatherFace.this);
//                long date = System.currentTimeMillis();
//                String dateString = sdf.format(date);
//                Log.d("watch", "string date " + dateString);
                if (preferences.getWeathererror() != null)
                    {
                        temp.setText(preferences.getWeathererror());
                        MainActivity.sendMsgToFetchData(WeatherFace.this);
                        return;
                    }
                model = preferences.getWeatherInfo();
                if (model.maxTemp == null)
                    {
                        temp.setText("No weather data please refresh");
                        MainActivity.sendMsgToFetchData(WeatherFace.this);
                        return;
                    }
                Log.d("watch", "string date " + dateString);
                temp.setText(model.maxTemp + "  " + model.minTemp);
                image = Drawable.createFromPath
                        (model.Url);
                image.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen
                        .drawable_size), getResources().getDimensionPixelSize(R.dimen
                        .drawable_size));
                if (!isInAmbientMode())
                    {
                        temp.setCompoundDrawables(image, null,
                                null, null);
                    }

            }


        class weatherChange extends BroadcastReceiver
        {
            @Override
            public void onReceive(Context context, Intent intent)
                {
                    Log.d("watchface", "weatherChange");
                    populateUI();
                    invalidate();

                }
        }
    }

}
