package com.example.loomo.basic.communication.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loomo.basic.communication.bluetooth.connect.AcceptThread;
import com.example.loomo.basic.communication.bluetooth.connect.Constant;
import com.example.loomo.basic.communication.bluetooth.connect.SendThread;
import com.example.loomo.basic.communication.bluetooth.controller.BlueToothController;
import com.example.loomo.basic.movement.MovementUnit;
import com.example.loomo.message.HandlerTag;
import com.example.loomo.message.LoomOrder;
import com.example.loomo.message.LoomState;

import java.io.UnsupportedEncodingException;

/**
 * @Description:
 * Top Class for bluetooth
 *
 * @date: 2021/12/28
 * @author : Zhouyao
 */
public class BluetoothUnit{
    private static final String TAG = "Bluetooth unit";
    /* ******************************
     *   Fields for external class  *
     ********************************/
    private AppCompatActivity main;


    /* ******************************
     *      Fields for bluetooth    *
     ********************************/
    private BlueToothController controller;
    private AcceptThread acceptThread;
    private SendThread sendThread;

    private Toast mToast;


    /**
     * Constructor to init bluetooth unit
     * @param activity is the activity where the bluetooth is initialized
     */
    public BluetoothUnit(AppCompatActivity activity, MovementUnit.MoveHandler moveHandler){
        this.main = activity;
        this.moveHandler = moveHandler;
    }

    /**
     * Method to initialize bluetooth listener
     */
    public void initListener(){
        controller = new BlueToothController();
        if(acceptThread != null){
            acceptThread.cancel();
        }
        acceptThread = new AcceptThread(controller.getAdapter(), btHandler);
        acceptThread.start();

        showToast("Listener is set");
    }

    /**
     * Method used to encapsulate message
     * @param text
     */
    private void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(main, text,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            mToast.setText(text);
            mToast.show();
        }

    }

    /**
     * Send Loomo State to upper computer
     * @param msg
     */
    public void sendLoomoState(LoomState msg){
        if (acceptThread != null) {
            acceptThread.sendData(msg.toJsonBytes());
        }
    }

    public void sendData(String word) {
        if (acceptThread != null) {
            try {
                acceptThread.sendData(word.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }


    /* ************************************************************
     *                                                            *
     *                 Fields for inner Handler                   *
     *     used to handle incoming message from Upper computer    *
     *                                                            *
     **************************************************************/
    MovementUnit.MoveHandler moveHandler;

    private BluetoothHandler btHandler = new BluetoothHandler();

    public BluetoothHandler getBTServerHandler(){
        return btHandler;
    }

    /**
     *  For handling incoming message from Server
     */
    public class BluetoothHandler extends Handler{
        /* **************************************************
        *                                                   *
        *       Field for message exchange with move unit   *
        *                                                   *
        *****************************************************/
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case Constant.MSG_GOT_LOOMORDER:{
                    showToast("Order:" + String.valueOf(message.obj));
                    LoomOrder order = new LoomOrder(String.valueOf(message.obj));

                    // Send to Move unit
                    Message msg = moveHandler.obtainMessage(HandlerTag.MSG_FROM_BLUETOOTH);
                    msg.obj = order;
                    moveHandler.sendMessage(msg);
                }break;

                case Constant.MSG_GOT_LOOMOSTATE:{
                    showToast("State:" + String.valueOf(message.obj));

                }break;

                case Constant.MSG_ERROR:{
                    showToast("error:" + String.valueOf(message.obj));
                }break;

                case Constant.MSG_CONNECTED_TO_SERVER:{
                    showToast("Server connected");
                }break;

                case Constant.MSG_GOT_A_CLINET:{
                    showToast("Client connected");
                }break;
            }
        }
    }


    /* *************************************
     *                                     *
     *      Fields for external Handler    *
     *                                     *
     ***************************************/

    /**
     *  For handling data from other unit on Loomo
     *  Will be used to send info of Loomo to Upper computer
     */
    public class BTServerHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case HandlerTag.MSG_FROM_POSITION:{

                }break;

                default:
                    Log.e(TAG, "handleMessage: Receive Unexpected message");
            }
        }
    }
}
