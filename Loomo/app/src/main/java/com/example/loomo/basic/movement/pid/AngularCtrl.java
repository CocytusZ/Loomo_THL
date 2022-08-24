package com.example.loomo.basic.movement.pid;

import android.util.Log;

import com.example.loomo.tool.math.Calc;

/**
 * @Description:
 * Class to encapsulate PI Control for angular movement
 * Use initCtrl() to initialize before each task
 * Use getVelocity to get velocity (speed and dir)
 *
 * @author : Zhouyao
 * Date: 2021/12/26
 */
public class AngularCtrl {
    /* ********************************************************
     *                                                        *
     *            Constants for Angular movement              *
     *                                                        *
     **********************************************************/
    // Param for portion loop
    private final float THETA_KP = MotionConfig.THETA_KP;
    private final float THETA_KP_DISABLE_THRE = MotionConfig.THETA_KP_DISABLE_THRE; // unit: radian

    // Param for integral loop
    private final float THETA_KI = MotionConfig.THETA_KI;
    private final int THETA_TI = MotionConfig.THETA_TI; // unit: *50 ms

    private final float MAX_ANGULAR_P_SPEED = MotionConfig.MAX_ANGULAR_P_SPEED;
    private final float MAX_ANGULAR_I_SPEED = MotionConfig.MAX_ANGULAR_I_SPEED;

    // Param for Terminate Condition
    private final float ANGULAR_ARR_DELTA = MotionConfig.ANGULAR_ARR_DELTA;
    private final float ANGULAR_ARR_VARIANCE = MotionConfig.ANGULAR_ARR_VARIANCE;


    private static final String TAG = "Angular Move Control";

    // Variable for data processing
    private MotionDataSlot thetaSlot = new MotionDataSlot(8);
    private float target;

    private boolean isFinish;

    /**
     * Check whether movement is finished
     * @return
     */
    public boolean isFinished(){
        return isFinish;
    }

    public AngularCtrl(){}

    public void initCtrl(float target){
        this.target = target;
        this.isFinish = false;

        thetaSlot.clearData();
    }


    /**
     * Rotate the robot to a designated angle
     *
     * @param current is current angle in radian
     */
    public float getVelocity(float current) {
        Log.i(TAG, "Angular: Get velocity ");
        /* *****************************
         *       Update data slot      *
         *******************************/
        if(thetaSlot.isClear()){
            for(int i = 0; i < thetaSlot.getLength(); i++){
                thetaSlot.push(current);
            }
        }else {
            thetaSlot.push(current);
        }


        // Var for portion
        float pSpeed = 0;
        float delt= 0;

        // var for integral
        float iSpeed = 0;
        float thetaError = 0;

        // var for output
        float speed = 0;

        /* ***********************
         *    Calculate Speed    *
         *************************/

        // Calculate the Magnitude of speed gain in portion element
        // Portion element only work when delta distance is bigger than DISABLE_THREAD
        delt = Math.abs(target - current);
        pSpeed = delt > THETA_KP_DISABLE_THRE ? delt * THETA_KP : 0;
        pSpeed = Calc.clamp(MAX_ANGULAR_P_SPEED, pSpeed);

        // Calculate the Magnitude of speed gain in integral element
        thetaError = Math.abs(THETA_TI * target - thetaSlot.getUpdateSum(THETA_TI) );
        iSpeed = delt < THETA_KP_DISABLE_THRE ? thetaError * THETA_KI : 0;
        iSpeed = Calc.clamp(MAX_ANGULAR_I_SPEED, iSpeed);

        speed = pSpeed + iSpeed;


        /* *********************************************
         *      Determine the direction of speed       *
         ***********************************************/
        // Initialize parameter for judge direction
        float pi = (float) Math.PI;
        float leftEnd = target - pi;
        float rightEnd = target + pi;
        float relativeTheta = current;

        if (current < leftEnd) {
            relativeTheta += 2 * pi;
        }

        if (current > rightEnd) {
            relativeTheta -= 2 * pi;
        }

        if (relativeTheta > target && relativeTheta <= rightEnd) {
            speed = -speed;
        }

        /* *********************
         * Terminate condition *
         ***********************/
        if (Math.abs(thetaSlot.getAvg() - target) < ANGULAR_ARR_DELTA && thetaSlot.getVariance() < ANGULAR_ARR_VARIANCE) {
            Log.d(TAG, "Angular Position Arrived, Readings in Average: " + (thetaSlot.getAvg() - target));
            this.isFinish = true;
            return 0;
        } else {
            return speed;
        }
    }
}
