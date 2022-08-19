package com.example.loomo.basic;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.SensorData;

import java.util.Arrays;

public class UltrasonicUnit{

      private static Sensor sensor;

      public UltrasonicUnit(Context context){
            if(sensor == null){
                  sensor = Sensor.getInstance();
            }

            bindService(context);
      }

      /**
       * Method to get Ultrasonic sensor
       * @return
       */
      public static Sensor getUnit(){
            return sensor;
      }

      /**
       * Method to bind Ultrasonic sensor
       * @param context
       */
      public static void bindService(Context context){
            sensor.bindService(context, new ServiceBinder.BindStateListener() {
                  @Override
                  public void onBind() {

                  }

                  @Override
                  public void onUnbind(String reason) {

                  }
            });
      }

      /**
       * Method to get Distance detected by Ultrasonic sensor
       * @return {float} the distance
       */
      public static float getDist(){
            SensorData ultrasonicData = sensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
            float ultrasonicDist = ultrasonicData.getIntData()[0];
            return ultrasonicDist;
      }

      /**
       * Method to print the distance data on view
       * @param v {View} no idea, but must input
       * @Param distText {TextView} is the text component on the view
       */
      public static void showDistOnView(View v, TextView distText) {
            //asking ultrasonic sensor for current distance
            SensorData mUltrasonicData = sensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
            float mUltrasonicDistance = mUltrasonicData.getIntData()[0];

            //Updating the new distance into the TextView on screen
            distText.setText(String.format("%f", mUltrasonicDistance));
      }
}
