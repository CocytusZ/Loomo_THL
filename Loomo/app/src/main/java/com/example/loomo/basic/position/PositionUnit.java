package com.example.loomo.basic.position;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.example.loomo.basic.communication.bluetooth.BluetoothUnit;
import com.example.loomo.basic.movement.MovementUnit;
import com.example.loomo.basic.position.source.MQTTClient;
import com.example.loomo.basic.position.source.Odometry;
import com.example.loomo.message.HandlerTag;
import com.example.loomo.message.LoomState;

/**
 * @Description:
 * Class for operating all position sensors
 * It is also the only interface for these sensors
 *
 * @date: 2021/12/26
 * @author : Zhouyao
 */
public class PositionUnit extends Thread{

    /* ******************************************
     *          Handlers for Other Units        *
     ********************************************/
    MovementUnit.MoveHandler moveHandler;
    BluetoothUnit.BluetoothHandler btHandler;

    /* ******************************************
     *      Fields for UWB Positioning system   *
     ********************************************/
    private boolean dataFlag;

    // Data from external position system is transmit through MQTT protocol
    private MQTTClient posMqtt;
    private boolean posFlag;
    private float uwb_x;
    private float uwb_y;
    private MQTTClient.DataListener posListener = new MQTTClient.DataListener() {
        @Override
        public void onMsgReturned(String x, String y) {
            uwb_x = Float.parseFloat(x);
            uwb_y = Float.parseFloat(y);

            posFlag = true;
        }
    };

    // Data from inner odometry sensor, which are coming every 50 ms
    private Odometry odometry;
    private boolean odoFlag;
    private float odometry_x;
    private float odometry_y;
    private float odometry_theta;
    private Odometry.DataListener odoListener = new Odometry.DataListener() {
        @Override
        public void onDataReturned(float posX, float posY, float theta) {
            odometry_x = posX;
            odometry_y = posY;
            odometry_theta = theta;

            odoFlag = true;
        }
    };

    /* ******************************************
     *         Fields for Position Unit         *
     ********************************************/
    private float x;
    private float y;
    private float theta;

    Context context;

    public PositionUnit(Context context, MovementUnit.MoveHandler moveHandler, BluetoothUnit.BluetoothHandler btHandler){
        this.context = context;
        this.odometry = new Odometry(context, odoListener);
        this.moveHandler = moveHandler;
        this.btHandler = btHandler;
    }

    @Override
    public void run() {
        super.run();

        /* Start Odometry */
        odometry.start();

        /* Start reciving data from external Position system */
        posMqtt = new MQTTClient(context);
        posMqtt.initMqtt(posListener);

        while (true){
            dataFlag = posFlag & odoFlag;

            if(posFlag){
                posFlag = false;
                // Function to handle Data from position system
            }

            if (odoFlag){
                odoFlag = false;
                LoomState state = new LoomState(odometry_x, odometry_y, odometry_theta, "");
//                Log.i("PosUnit", "State Message : Theta=" + odometry_theta
//                        + ", PosX=" + state.posX
//                        + ", PosY=" + state.posY);
                Message moveMsg = moveHandler.obtainMessage(HandlerTag.MSG_FROM_POSITION);
                moveMsg.obj = state;
                moveHandler.sendMessage(moveMsg);

                Message btMsg = btHandler.obtainMessage(HandlerTag.MSG_FROM_BLUETOOTH);
                btMsg.obj = state;
                btHandler.sendMessage(btMsg);
            }

            if(dataFlag){
                // Function to handle Data from both sensor
            }

        }
    }


}
