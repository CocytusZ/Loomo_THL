package com.example.loomo.basic.sensorReg;

import android.content.Context;
import android.util.Log;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.perception.sensor.Sensor;

/**
 * @date 22.04.2022
 * @author Zhouyao
 * Use to manage the bind of all sensors
 */
public class SensorRegister {
    private String TAG = "Sensor register";
    private Context context;

    public SensorRegister(Context context) {
        this.context = context;
    }

    public void registerAll(){
        registerSensors();
    }

    public void registerSensors(){
        Sensor sensor = Sensor.getInstance();
        sensor.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.i(TAG, "Sensors bound");
            }

            @Override
            public void onUnbind(String reason) {

            }
        });
    }

}
