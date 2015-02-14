package com.painless.glclock.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.painless.glclock.Globals;

public final class CompassService implements RService, SensorEventListener {

    private final SensorManager manager;

    public CompassService(Context context) {
        this.manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void start() {
        Sensor s = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        manager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
//        Debug.log("CompassService started");
    }

    @Override
    public void stop() {
        manager.unregisterListener(this);
//        Debug.log("CompassService stopped");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Globals.compassRotation = event.values[0];
    }
}