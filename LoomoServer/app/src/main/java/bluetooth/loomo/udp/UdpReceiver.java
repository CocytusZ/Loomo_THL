package bluetooth.loomo.udp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;//导入IOException类
import java.net.DatagramPacket;//导入DatagramPacket类
import java.net.DatagramSocket;//导入DatagramSocket类
import java.net.InetAddress;

import bluetooth.loomo.bluetooth.connect.BTConst;
import bluetooth.loomo.config.Message.HandlerTag;


public class UdpReceiver extends Thread{
	private String TAG = "UDP UNIT";

	private boolean killFlag = false;
	private Handler dataHandler;

	/********************************
	 *	 	Field for UDP Receiver	*
	 ********************************/
	DatagramSocket socket;
	DatagramPacket recPacket;
	byte[] recData;

	private InetAddress senderAddress;
	private int senderPort;
	private UdpSender.ReplySenderHandler replyHandler;
	
	/**
	 * Create a UDP Server which can receive and send message
	 * @param socket
	 */
	public UdpReceiver(DatagramSocket socket) {
		try {
			// initialize receiver
			this.socket = socket;
			this.recData = new byte[1024];
			this.recPacket = new DatagramPacket(recData, recData.length);

		} catch (Exception e) {
			Log.e(TAG, "UdpReceiver: Create receiver socket failed", e);
		}
	}

	@Override
	public void run() {
		super.run();

		while (true) {
			//Receive
			try {
				socket.receive(recPacket);
				senderAddress = recPacket.getAddress();
				senderPort = recPacket.getPort();

				if(replyHandler != null){
					replyHandler.setupReplySender(senderAddress, senderPort);
				}
				/********************************************
				 * 											*
				 * 		The data is stored in recData	  	*
				 * 											*
				 * 		Add code below to Handle message	*
				 *  										*
				 ********************************************/
				String info = new String(recData, 0, recData.length);
				Log.i(TAG, "Message received: " + info);


				Message msg = new Message();
				msg.what = HandlerTag.MSG_FROM_UDP;
				msg.obj =(byte[]) recData;
				if(recData.length != 0){
					msg.obj = BitmapFactory.decodeByteArray(recData, 0, recData.length);
				}

				dataHandler.sendMessage(msg);

			} catch (IOException e) {
				Log.e(TAG, "Error when receiver listen" , e);
			}

		}
	}

	public void setDataHandler(Handler dataHandler){
		this.dataHandler = dataHandler;
	}

	/**
	 * Get the ip address of the device which send the message to this one
	 * @return
	 */
	public InetAddress getClientAddress() {
		return senderAddress;
	}

	/**
	 * Get the ip port of the device which send the message to this one
	 * @return
	 */
	public int getSenderPort() {
		return senderPort;
	}

	/**
	 * To stop this thread
	 */
	public void kill() {
		killFlag = true;
	}
	
	/**
	 * Set up a handler from a sender to help it get address and port of remote device
	 * @param handler
	 */
	public void setReplyHandler(UdpSender.ReplySenderHandler handler) {
		this.replyHandler = handler;
	}

}
