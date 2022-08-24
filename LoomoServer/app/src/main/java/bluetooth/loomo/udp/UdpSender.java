package bluetooth.loomo.udp;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import bluetooth.loomo.config.Message.LoomOrder;

/**
 * A Udp sender thread used to send message to remote device
 * Will be a member in Object of UdpUnit
 *
 * There is three process to start sending:
 * 	1. create the instance of this class
 * 	2. set ip address and port of target devices
 *  3. send message
 *
 * There is two sending mode:
 *  1. Send data for once, use method: send(LoomOrder)
 *  2. Keep sending data, use following method:
 *      2.1 setOrder(LoomOrder)
 *      2.2 startSend(msgInterval)
 *      2.3 finishSend()
 *
 * @author Zhouyao
 *
 */
public class UdpSender extends Thread{
	private static final String TAG = "UDP Receiver";

	/* ******************************
	 *	 	Field for Thread		*
	 ********************************/
	boolean killFlag = false;

	int msgInterval = 50;
	boolean sendFlag = false;
	LoomOrder order;


	/* ******************************
	 *	 	Field for UDP 			*
	 ********************************/
	private DatagramSocket socket;
	InetAddress address;
	int port;
	
	public UdpSender(DatagramSocket socket) {
		try {
			this.socket = socket;
		} catch (Exception e) {
			Log.e(TAG, "UdpSender: Init failed",e );
		}
		
		address = null;
		port = -1;
	}
	
	/**
	 * Set param of remote device
	 * @param address 
	 * @param port 
	 */
	public void setTargetDevice(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public void setTargetDevice(String address, int port) {
		try {
			this.address = InetAddress.getByName(address);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.port = port;
	}
	
	
	public void send(String msg) {
        byte[] sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);

        try {
	        socket.send(sendPacket);
		} catch (Exception e) {
			Log.e(TAG, "send: Failed", e);
		}
	}

	/**
	 * Method to send Loomo order to Loomo
	 * @param order LoomOrder
	 */
	public void send(LoomOrder order){
		byte[] data = order.toJsonBytes();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);

		try {
			socket.send(sendPacket);
		} catch (Exception e) {
			Log.e(TAG, "send: Failed", e);
		}
	}



	/* ******************************************************
	 *	 	Handler called by receiver to set up sender		*
	 ********************************************************/
	class ReplySenderHandler{
		public void setupReplySender(InetAddress address, int port) {
			UdpSender.this.address = address;
			UdpSender.this.port = port;
		}
	}

	private ReplySenderHandler replyHandler = new ReplySenderHandler();
	public ReplySenderHandler getReplyHandler() {
		return this.replyHandler;
	}

	/* ******************************************************
	 *		 	Methods for continuous sending 				*
	 ********************************************************/

	@Override
	public void run() {
		super.run();

		while (!killFlag){
			if(sendFlag){
				try {
					send(order);
					sleep(msgInterval);
				}catch (Exception e){
					Log.e(TAG, "Continuously sending failed: ", e );
				}
			}
		}
	}

	public void killThread(){
		killFlag = true;
		socket.disconnect();
		socket.close();
	}

	public void finishSend(){
		this.sendFlag = false;
	}

	/**
	 * Set the interval between two messages when continuously sending message
	 * And then start sending
	 * @param interval
	 */
	public void startSend(int interval){
		this.msgInterval = interval;
		this.sendFlag = true;
	}

	public void setOrder(LoomOrder order){
		this.order = order;
	}
}
