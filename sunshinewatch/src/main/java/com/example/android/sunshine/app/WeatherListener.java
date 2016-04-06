package com.example.android.sunshine.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class WeatherListener extends WearableListenerService
{
    final private String MAX_TEMP = "max_temp";
    final private String MIN_TEMP = "min_temp";
    final private String REFRESHED_TIME = "time";
    final private String ASSET = "image";
    final private String PATH = "/count";
    String TAG = "wear device service";
    private GoogleApiClient mGoogleApiClient;
    static final String WEATHER_ACTION_REFRESH = "refresh_data";
    static final String WEATHER_ACTION_FETCH= "fetch_data";

    public WeatherListener()
        {
            Log.e(TAG, "instatiated: ");
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
        {
            Log.e(TAG, "onStartCommand: ");

            return super.onStartCommand(intent, flags, startId);
        }

    @Override
    public void onCreate()
        {
            super.onCreate();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
                    {
                        @Override
                        public void onConnected(Bundle connectionHint)
                            {
                                Log.e(TAG, "onConnected: " + connectionHint);
                                // Now you can use the Data Layer API
                                Wearable.DataApi.addListener(mGoogleApiClient, WeatherListener
                                        .this);
                            }

                        @Override
                        public void onConnectionSuspended(int cause)
                            {
                                Log.e(TAG, "onConnectionSuspended: " + cause);
                            }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener()
                    {
                        @Override
                        public void onConnectionFailed(ConnectionResult result)
                            {
                                Log.e(TAG, "onConnectionFailed: " + result);
                            }
                    })
                            // Request access only to the Wearable API
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.connect();
        }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents)
        {
            super.onDataChanged(dataEvents);
            Log.e(this.getClass().getName(), "dataitem ondatchanged");
            long latestDate = 0;

            DataMap dataMap, selectedMap = null;
            for (DataEvent item :
                    dataEvents)
                {

                    if (item.getType() == DataEvent.TYPE_CHANGED)
                        {
                            DataItem data = item.getDataItem();
                            Log.e(this.getClass().getName(), "dataitem changed");
                            if (data.getUri().getPath().compareTo(PATH) == 0)
                                {
                                    dataMap = DataMapItem.fromDataItem(data).getDataMap();
                                    if (latestDate < dataMap.getLong(REFRESHED_TIME))
                                        {
                                            latestDate = dataMap.getLong(REFRESHED_TIME);
                                            selectedMap = dataMap;
                                        }
                                    Log.e(this.getClass().getName(), "dataitem " + dataMap);
                                }
                        }
                }
            if (latestDate != 0)
                {
                    parseData(selectedMap);
                    sendLocalNotification();
                }
        }

    void sendLocalNotification()
        {
            Intent broadcastIntent = new Intent(WEATHER_ACTION_REFRESH);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }

    void parseData(DataMap selectedMap)
        {
            WeatherModel model = new WeatherModel();
            model.maxTemp = selectedMap.getString(MAX_TEMP);
            model.minTemp = selectedMap.getString(MIN_TEMP);
            model.Url = loadBitmapFromAsset(selectedMap.getAsset(ASSET));
            WeatherPreferences.getInstance(this).setWeatherInfo(model);
        }

    public String loadBitmapFromAsset(Asset asset)
        {
            if (asset == null)
                {
                    throw new IllegalArgumentException("Asset must be non-null");
                }
            ConnectionResult result =
                    mGoogleApiClient.blockingConnect(30000, TimeUnit.MILLISECONDS);
            if (!result.isSuccess())
                {
                    return null;
                }
            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            mGoogleApiClient.disconnect();

            if (assetInputStream == null)
                {
                    Log.w(TAG, "Requested an unknown Asset.");
                    return null;
                }
            // decode the stream into a bitmap
            File temppath = new File(getCacheDir().getAbsolutePath() + File.pathSeparator
                    + "temp.jpg");
            FileOutputStream fileOutputStream = null;
            Bitmap bmp = BitmapFactory.decodeStream(assetInputStream);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            try
                {
                    fileOutputStream = new FileOutputStream(temppath);
                    fileOutputStream.write(byteArray, 0, byteArray.length);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    return temppath.getAbsolutePath();
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            return null;
        }
}
