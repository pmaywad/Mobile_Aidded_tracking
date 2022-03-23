package com.example.mycovidapp.symptoms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

/**
 * Accelerometer service runs in background to sense and return X values for processing by the HomeScreen activity
 */
public class AccService extends Service implements SensorEventListener {

    private SensorManager accelManager;
    private Sensor senseAccel;
    private ArrayList<Integer> accelValuesX = new ArrayList<>();
    private ArrayList<Integer> accelValuesY = new ArrayList<>();
    private ArrayList<Integer> accelValuesZ = new ArrayList<>();

    @Override
    public void onCreate(){

      //  Log.i("log", "Accel Service started");
        accelManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Registers accelerometer sensor data listener
        accelManager.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accelValuesX.clear();
        accelValuesY.clear();
        accelValuesZ.clear();
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor genericSensor = sensorEvent.sensor;
        if (genericSensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //Stores accelerometer sensor data and converts it to integers multiplied by 100
            accelValuesX.add((int)(sensorEvent.values[0] * 100));
            accelValuesY.add((int)(sensorEvent.values[1] * 100));
            accelValuesZ.add((int)(sensorEvent.values[2] * 100));

            //Sensing stops after 45s at approximately 230 data points
            if(accelValuesX.size() >= 230){
                stopSelf();
            }
        }
    }

    @Override
    public void onDestroy(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                //Unregisters accelerometer sensor data listener
                accelManager.unregisterListener(AccService.this);
               // Log.i("service", "Service stopping");

                //Broadcasts accelerometer X values
                Intent intent = new Intent("broadcastingAccelData");
                Bundle b = new Bundle();
                b.putIntegerArrayList("accelValuesX", accelValuesX);
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(AccService.this).sendBroadcast(intent);
            }
        });
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
