package com.example.loomo.basic.movement.pid;

import android.util.Log;

import com.example.loomo.tool.math.Calc;


/**
 * @Description:
 * Class to encapsulate PI Control for linear movement
 * Use initCtrl() to initialize before each task
 * Use getVelocity() to get velocity (speed and dir)
 *
 * @author : Zhouyao
 * Date: 2021/12/26
 */
public class LinearCtrl {
    /* ********************************************************
     *                                                        *
     *            Constants for Linear movement               *
     *                                                        *
     **********************************************************/
    // Param for portion loop
    private final float DIST_KP = MotionConfig.DIST_KP;
    private final float MAX_LINEAR_P_SPEED = MotionConfig.MAX_LINEAR_P_SPEED;
    private final float MIN_LINEAR_P_SPEED = MotionConfig.MIN_LINEAR_P_SPEED;

    // Param for integral loop
    private final float DIST_KD = MotionConfig.DIST_KI;
    private final float MAX_LINEAR_I_SPEED = MotionConfig.MAX_LINEAR_I_SPEED;

    private final float LINEAR_BRAKE_THRE = MotionConfig.LINEAR_BRAKE_THRE;
    private final float LINEAR_ARR_DELT = MotionConfig.LINEAR_ARR_DELT;
    private final float LINEAR_ARR_VARIANCE = MotionConfig.LINEAR_ARR_VARIANCE;


    private static final String TAG = "Linear Move Control";

    // Variable for data processing
    private float targetDist;
    private float originX, originY;
    private MotionDataSlot distSlot = new MotionDataSlot(5);
    private boolean isFinish;

    /**
     * Check whether movement is finished
     * @return
     */
    public boolean isFinished(){
        return isFinish;
    }

    /**
     * Constructor
     */
    public LinearCtrl(){}

    /**
     * Initial task for controller,
     *
     */
    public void initCtrl(float targetDist){
        this.targetDist = targetDist;
        this.isFinish = false;

        this.distSlot.clearData();
    }

    /**
     * Make Loomo move ahead for a certain distance
     * @param currentX is current X-axis position
     * @param currentY is current Y-axis position
     */
    public float getVelocity(float currentX, float currentY){
        /* **************
         * Initializing *
         ****************/
        float dist = 0;

        if(distSlot.isClear()){
            for(int i = 0; i < distSlot.getLength(); i++){
                distSlot.push(0);
            }

            this.originX = currentX;
            this.originY = currentY;
        }else {
            dist = (float) Math.sqrt(
                    (currentX - originX) * (currentX - originX)
                            + (currentY - originY) * (currentY - originY)
            );
            distSlot.push(dist);
        }

        // Var for portion
        float pSpeed;
        float deltaDist = targetDist - dist;

        // var for differential
        float dSpeed;
        float posDif = distSlot.get(0) - distSlot.get(1); // differential of position
        float speed;


        /* *****************
         * Calculate Speed *
         *******************/

        // Calculate the Magnitude of speed gain in portion element
        pSpeed = deltaDist * DIST_KP;
        pSpeed = Calc.clamp(MAX_LINEAR_P_SPEED, MIN_LINEAR_P_SPEED, pSpeed);



//        // Calculate the Magnitude of speed gain in differential element
//        dSpeed = - posDif * DIST_KD;
//        dSpeed = Calc.clamp(MAX_LINEAR_I_SPEED,  dSpeed);

        speed = pSpeed;

        if(Math.abs(deltaDist) < LINEAR_BRAKE_THRE){
            speed = pSpeed;
        }

        /* *********************
         * Terminate condition *
         ***********************/
        if (Math.abs(distSlot.getAvg() - targetDist) < LINEAR_ARR_DELT && distSlot.getVariance() < LINEAR_ARR_VARIANCE) {
            Log.d(TAG, "moveToDist Linear Position Arrived, Readings in Average: " + (distSlot.getAvg() - targetDist));
            Log.d(TAG, "moveToDist Linear Position Arrived, Readings in Variance: " + (distSlot.getVariance()));
            this.isFinish = true;
            return 0;
        } else {
          return speed;
        }
    }
}


