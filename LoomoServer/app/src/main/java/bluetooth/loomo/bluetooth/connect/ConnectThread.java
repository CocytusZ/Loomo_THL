package bluetooth.loomo.bluetooth.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

import bluetooth.loomo.config.Message.LoomOrder;

/**
 * Class to encapsulate data transmission methods
 */
public class ConnectThread extends Thread {
    private static final UUID MY_UUID = UUID.fromString(BTConst.CONNECTTION_UUID);
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;
    private SendThread sendThread;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        // U将一个临时对象分配给mmSocket，因为mmSocket是最终的
        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = adapter;
        mHandler = handler;
        // 用BluetoothSocket连接到给定的蓝牙设备
        try {
            // MY_UUID是应用程序的UUID，客户端代码使用相同的UUID
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        // 搜索占用资源大，关掉提高速度
        mBluetoothAdapter.cancelDiscovery();

        try {
            // 通过socket连接设备，阻塞运行直到成功或抛出异常时
            mmSocket.connect();
        } catch (Exception connectException) {
            mHandler.sendMessage(mHandler.obtainMessage(BTConst.MSG_ERROR, connectException));
            // 如果无法连接则关闭socket并退出
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        // 在单独的线程中完成管理连接的工作
        manageConnectedSocket(mmSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        mHandler.sendEmptyMessage(BTConst.MSG_CONNECTED_TO_SERVER);
        mConnectedThread = new ConnectedThread(mmSocket, mHandler);
        mConnectedThread.start();

        sendThread = new SendThread(mmSocket);
        sendThread.start();
    }

    /**
     * 取消正在进行的连接并关闭socket
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    /**
     * Expired, only for test
     * @param data
     */
    public void sendData(byte[] data){
        if(mConnectedThread != null){
            mConnectedThread.write(data);
        }
    }

    /**
     * Make sending Thread to send message for once
     * Auto send
     * @param order
     */
    public void sendOrder(LoomOrder order){
        sendThread.send(order);
    }

    /**
     * Set order send to Loomo
     * First step of manual continuous sending
     * @param order
     */
    public void setOrder(LoomOrder order){
        sendThread.setData(order);
    }

    /**
     * Start sending data continuously
     */
    public void startSend(){
        sendThread.startSend(0);
    }

    /**
     * Start sending data with some interval between two transmission
     * @param interval interval between transmission, unit: ms
     */
    public void startSend(long interval){
        sendThread.startSend(interval);
    }

    /**
     * Finish data sending
     */
    public void finishSend(){
        sendThread.finishSend();
    }
}