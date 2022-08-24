package bluetooth.loomo.udp;

import android.os.Handler;
import android.util.Log;

import java.net.DatagramSocket;


import bluetooth.loomo.config.Message.LoomOrder;

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

    public UdpUnit(){
        try {
            socket = new DatagramSocket(SOCKET_PORT);
            sender = new UdpSender(socket);
            receiver = new UdpReceiver(socket);

            receiver.start();
        }catch (Exception e){
            Log.e(TAG, "UdpUnit: ", e);
        }
        Log.e(TAG,"UdpUnit: init");

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

    /**
     * Set the handler which is used to handler the data received by Receiver
     * @param dataHandler
     */
    public void setDataHandler(Handler dataHandler){
        receiver.setDataHandler(dataHandler);
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

}
