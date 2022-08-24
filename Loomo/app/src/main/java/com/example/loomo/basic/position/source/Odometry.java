package com.example.loomo.basic.position.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.loomo.basic.movement.pid.MotionConfig;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;


/**
 * @date: 28.12.2022
 * @author Zhouyao
 *
 * @Description:
 * Class for Odometry sensor, used to read Angular and position data
 * Need to run in subThread to get
 *
 * The coordination system is as followed:
 * ******************************************************
 *                                                      *
 *                                   X_Axis + (W)       *
 *                                                      *
 *                              0                       *
 *       Y_Axis + (S)           |                       *
 *                            (W)                       *
 *              0.5 PI  --(S)    (N)--  -0.5 PI         *
 *                           (E)                        *
 *                            |                         *
 *                        PI    - PI                    *
 *                                                      *
 * ******************************************************
 * variable OFFSET can be used to adjust the coordination
 */
public class Odometry extends Thread {
    private static final float pi = (float) Math.PI;
    private static final String TAG = "LOOMO PositionUnit.Odometry";

    /**
     * The time of odometry sensor, called as a param in getOdometry();
     * When it is -1, the function will return the current pose
     */
    public static final long SAMPLE_TIME = -1;

    /**
     * The Data of angular get every 50 ms
     */
    public static final long DATA_INTERVAL = 50; // unit: ms

    /**
     * Offset add to theta reading, used to adjust coordinate system
     */
    private static float ANGULAR_OFFSET = MotionConfig.ANGULAR_OFFSET;

    private DataListener listener;
    private float theta;
    private float posX;
    private float posY;

    /**
     * Listener
     * Initialized by PositionUnit
     */
    public interface DataListener {
        void onDataReturned(float posX, float posY, float tehta);
    }

    /**
     * Flag to mark whether the pos is initialized
     */
    private boolean posInitFlag;
    private float originPosX = 0;
    private float originPosY = 0;

    Base base;


    public Odometry(Context context, DataListener listener){
        this.listener = listener;

        this.base = Base.getInstance();
        base.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {

            }

            @Override
            public void onUnbind(String reason) {

            }
        });
    }


    /**
     * To reset the Origin point of Loomo
     * @param x
     * @param y
     */
    public void setOriginPoint(float x, float y){
        this.originPosX = x;
        this.originPosY = y;

        this.posInitFlag = true;
    }

    /**
     * Entry of Class
     * Run every 50 ms
     * Read odometry data
     */
    @SuppressLint("LongLogTag")
    @Override
    public void run() {
        while (true){
            try {
                sleep(DATA_INTERVAL);
            } catch (InterruptedException e) {
                Log.e(TAG, "run: Error encounter when sleep thread");
                e.printStackTrace();
            }

            /* ************************
             *    Get Angle Reading   *
             **************************/
            theta = base.getOdometryPose(SAMPLE_TIME).getTheta() + ANGULAR_OFFSET;
            if(theta > pi){
                theta -= 2 * pi;
            }else if(theta < - pi){
                theta += 2 * pi;
            }
//            Log.i(TAG, "Position unit: theta=" + theta);

            /* *********************
             *   Get Pos Reading   *
             ***********************/
            // Reset the origin point
            if(!posInitFlag){
                posInitFlag = true;
                originPosX = base.getOdometryPose(SAMPLE_TIME).getX();
                originPosY = base.getOdometryPose(SAMPLE_TIME).getY();
            }

            posX = base.getOdometryPose(SAMPLE_TIME).getX() - originPosX;
            posY = base.getOdometryPose(SAMPLE_TIME).getY() - originPosY;

//            Log.i(TAG, "Position unit: X=" + posX + ", Y=" + posY);
            listener.onDataReturned(posX, posY, theta);

        }

    }


}
