package com.example.loomo.basic.communication.udp;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.loomo.basic.movement.MovementUnit;
import com.example.loomo.message.HandlerTag;
import com.example.loomo.message.LoomOrder;
import com.example.loomo.message.LoomState;
import com.example.loomo.slam.realSense.RealSenseCamera;

import java.net.DatagramSocket;

/**
 * @author Zhouyao
 * @Date: 2022/6/16
 * @Description:
 */
public class UdpUnit {
    private static String TAG = "UDP Unit";
    private static final int SOCKET_PORT = 1122;

    private DatagramSocket socket;
    private UdpReceiver receiver;
    private UdpSender sender;

    private MovementUnit.MoveHandler moveHandler;
    private RealSenseCamera camera;

    public UdpUnit(MovementUnit.MoveHandler moveHandler){
        try {
            this.moveHandler = moveHandler;

            socket = new DatagramSocket(SOCKET_PORT);
            sender = new UdpSender(socket);
            receiver = new UdpReceiver(socket, recHandler);
            receiver.setReplyHandler(sender.getReplyHandler());

            receiver.start();
        }catch (Exception e){
            Log.e(TAG, "UdpUnit: ", e);
        }
        Log.e(TAG,"UdpUnit: init");
    }

    public void setRealSenseCamera(RealSenseCamera camera){
        this.camera = camera;
    }

    public void setRemoteDevice(String inetAddress, int port){
        sender.setTargetDevice(inetAddress, port);
        sender.start();
    }

    public void send(String msg){
        final String message = msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                sender.send(message);
            }
        }).start();
    }

    public void send(LoomOrder order){
        final LoomOrder message = order;
        new Thread(new Runnable() {
            @Override
            public void run() {
                sender.send(message);
            }
        }).start();
    }

    public void send(Bitmap bitmap){
        final Bitmap message = bitmap;
        new Thread(new Runnable() {
            @Override
            public void run() {
                sender.send(message);
            }
        }).start();
    }

    public void send(LoomState state){
        final LoomState message = state;
        new Thread(new Runnable() {
            @Override
            public void run() {
                sender.send(message);
            }
        }).start();
    }

    /**
     * Method used in continuously sending
     * Used to set order to send
     * @param order
     */
    public void setOrder(LoomOrder order){
        sender.setOrder(order);
    }

    /**
     * Method used in continuously sending
     * Used to set interval of two messages and start sending
     */
    public void startSend(int interval){
        sender.startSend(interval);
    }

    /**
     * Method used in continuously sending
     * Finish sending
     */
    public void finishSend(){
        sender.finishSend();
    }

    public void kill(){
        receiver.kill();
        socket.disconnect();
        socket.close();
    }


    /* ***********************************************************
     *                                                           *
     *           Fields and Methods for Handle Massage           *
     *                                                           *
     *         Messages will come from UDP receiver              *
     *                                                           *
     *************************************************************/

    private ReceiveHandler recHandler = new ReceiveHandler();

    /**
     * The class to handle received message of receiver
     */
    protected class ReceiveHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            LoomOrder order = new LoomOrder(String.valueOf(msg.obj));

            Message orderMsg = new Message();
            orderMsg.what = HandlerTag.MSG_FROM_UDP;
            orderMsg.obj = order;

            switch (order.mode){
                case LoomOrder.REPORT_IMG:
                    Bitmap bitmap = camera.captureSingleBitmap();
                    LoomState loomState = new LoomState(0, 0, 0, "Test");
                    send(loomState);
//                    send(bitmap);
                    Log.w(TAG, "UDP handleMessage: Report" );
                    break;

                default:moveHandler.sendMessage(orderMsg);break;
            }



        }
    }

}
