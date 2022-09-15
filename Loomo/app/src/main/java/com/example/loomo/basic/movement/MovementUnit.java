package com.example.loomo.basic.movement;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.loomo.basic.UltrasonicUnit;
import com.example.loomo.basic.movement.pid.AngularCtrl;
import com.example.loomo.basic.movement.pid.LinearCtrl;
import com.example.loomo.message.HandlerTag;
import com.example.loomo.message.LoomOrder;
import com.example.loomo.message.LoomState;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;

/**
 * @Description:
 * Class for Movement unit
 * This is the top class for movement control
 *
 * @author : Zhouyao
 * Date: 2021/12/26
 */
public class MovementUnit extends Thread {
    private static Base base;

    String TAG = "Movement";

    /* ****************************************
     *        Fields for Ultrasonic sensor    *
     ******************************************/
    UltrasonicUnit ultrasonicUnit;
    float dist;
    private final float MIN_DIST = 0;

    /* ****************************************
     *        Fields for assign order         *
     ******************************************/

    public MovementUnit(Context context, UltrasonicUnit ultrasonicUnit) {
        if (base == null) {
            base = Base.getInstance();
            base.bindService(context, new ServiceBinder.BindStateListener() {
                @Override
                public void onBind() {

                }

                @Override
                public void onUnbind(String reason) {

                }
            });
        }

        this.ultrasonicUnit = ultrasonicUnit;
    }

    /**
     * Entry of this thread
     */
    @Override
    public void run() {
        super.run();
        while (true){
//            Log.i(TAG, "run: Wait new order");
            
            dist = ultrasonicUnit.getDist();
            Log.v(TAG, "Ultrasonic: " + dist );
            // Avoid collision
            if( dist< MIN_DIST && base.getLinearVelocity().getSpeed() > 0){
                base.setLinearVelocity(0);
                continue;
            }

            if(order != null){
                try {
                    switch (order.mode){
                        case LoomOrder.RAW_MODE:{
                            // Avoid collision
//                            if( dist< MIN_DIST && order.rawY > 0){
//                                base.setLinearVelocity(0);
//                                continue;
//                            }

                            base.setAngularVelocity((float)order.rawX);
                            base.setLinearVelocity((float)order.rawY);
                        }break;

                        case LoomOrder.POLAR_MODE:{
                            Log.i(TAG, "POLAR MODE: Move to angle="
                                    + order.ang + ", Move to dist=" + order.dist);
                            moveToAngle((float) order.ang);
                            moveToDist((float) order.dist);
                        }break;

                        case LoomOrder.COORD_MODE:{

                        }break;

                        case LoomOrder.STOP_MODE:{
                            halt();
                        }break;
                        default: throw new Exception();
                    }
                }catch (Exception e){
                    Log.e(TAG, "Movement order error");
                    break;
                }
            }else{
                halt();
            }
        }

        Log.e(TAG, "Move stop! ");
    }

    /* ******************************************************************
     *                                                                  *
     *             Fields and Methods for Communication:                *
     *                 1. Receive Loomo Order                           *
     *                                                                  *
     ********************************************************************/
    LoomOrder order;
    public void setLoomOrder(LoomOrder order){
        this.order = order;
    }


    /* ******************************************************************
     *                                                                  *
     *                       Methods for Moving                         *
     *                                                                  *
     *     !!! Coordination and Controlling part should be added !!!    *
     *                                                                  *
     ********************************************************************/

    /**
     * Method to reset the base origin to (0,0) at current position
     */
    public void baseOriginReset() {
        base.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        base.clearCheckPointsAndStop();
        base.cleanOriginalPoint();
        Pose2D newOriginPoint = base.getOdometryPose(-1);
        base.setOriginalPoint(newOriginPoint);
    }

    /**
     *  Method to Let loomo stop
     */
    public void halt(){
        base.setAngularVelocity(0);
        base.setLinearVelocity(0);
    }

    /* ***********************************************************
     *                                                           *
     *           Fields and Methods for Precise movement         *
     *                                                           *
     *              PD control is applied to Linear              *
     *              PID control is applied to Angular            *
     *                                                           *
     *************************************************************/
    private boolean thetaFlag;
    private boolean posFlag;

    private float theta;
    private float posx;
    private float posy;

    private AngularCtrl angularCtrl = new AngularCtrl();
    private LinearCtrl linearCtrl = new LinearCtrl();

    /**
     * Method for moving and rotating
     * Before moving detect whether there is enough space
     *
     * @param x     the x coordination to the origin point
     * @param y     the y coordination to the origin point
     * @param angle the pointed direction of robot
     */
    public void moveTo(float x, float y, float angle) {

    }

    /**
     * Rotate the robot to a designated angle
     *
     * @param target can be any number
     */
    public void moveToAngle(float target) {
        /* **************
         * Initializing *
         ****************/
        base.setControlMode(Base.CONTROL_MODE_RAW);
        angularCtrl.initCtrl(target);

        while (true){
            /* Set speed to base */
            if (thetaFlag){
                Log.i(TAG, "moveToAngle: ");
                thetaFlag = false;
                base.setAngularVelocity(angularCtrl.getVelocity(theta));
            }
            /* Terminate condition */
            // Movement finished
            if(angularCtrl.isFinished()){
                Log.i(TAG, "moveToAngle: Finished");
                break;
            }

            // Stop by order
            if(order.mode == LoomOrder.STOP_MODE){
                break;
            }
        }
    }

    public void moveToDist(float target){
        base.setControlMode(Base.CONTROL_MODE_RAW);
        linearCtrl.initCtrl(target);

        while (true){
            /* Set speed to base */
            if(posFlag){
                Log.i(TAG, "moveToDist: ");
                posFlag = false;
//                if(UltrasonicUnit.getDist() < 1000) break;
                base.setLinearVelocity(linearCtrl.getVelocity(posx, posy));
            }


            /* Terminate condition */
            // Movement finished
            if(linearCtrl.isFinished()){
                break;
            }

            // Stop by order
            if(order.mode == LoomOrder.STOP_MODE){
                break;
            }
        }

    }


    /* ***********************************************************
     *                                                           *
     *           Fields and Methods for Handle Massage           *
     *                                                           *
     *         Messages will come from following units:          *
     *              1. Position unit                             *
     *              2. Bluetooth Unit                            *
     *                                                           *
     *************************************************************/
    private MoveHandler moveHandler = new MoveHandler();

    public MoveHandler getMoveHandler(){
        return this.moveHandler;
    }

    public class MoveHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case HandlerTag.MSG_FROM_POSITION:{
                    LoomState state = (LoomState) msg.obj;
                    theta = (float) state.theta;
                    posx = (float) state.posX;
                    posy = (float) state.posY;

                    thetaFlag = true;
                    posFlag = true;
                    Log.v(TAG, "State Message : Theta=" + theta
                            + ", PosX=" + posx
                            + ", PosY=" + posy);
                }break;

                case HandlerTag.MSG_FROM_BLUETOOTH:{
                    order = (LoomOrder) msg.obj;
                }break;

                case HandlerTag.MSG_FROM_UDP:{
                    order = (LoomOrder) msg.obj;
                }break;
            }
        }
    }


}

