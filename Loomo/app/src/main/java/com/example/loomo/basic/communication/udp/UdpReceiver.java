package com.example.loomo.basic.communication.udp;

import android.os.Message;
import android.util.Log;

import com.example.loomo.basic.communication.bluetooth.connect.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UdpReceiver extends Thread{
	private String TAG = "UDP UNIT";

	private boolean killFlag = false;

	/********************************
	 *	 	Field for UDP Receiver	*
	 ********************************/
	DatagramSocket socket;
	DatagramPacket recPacket;
	byte[] recData;

	private InetAddress senderAddress;
	private int senderPort;
	private UdpSender.ReplySenderHandler replyHandler;
	private UdpUnit.ReceiveHandler recHandler;
	
	/**
	 * Create a UDP Server which can receive and send message
	 * @param socket
	 */
	public UdpReceiver(DatagramSocket socket, UdpUnit.ReceiveHandler recHandler) {
		try {
			// initialize receiver
			this.socket = socket;
			this.recData = new byte[1024];
			this.recPacket = new DatagramPacket(recData, recData.length);
			this.recHandler = recHandler;

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
				Log.i(TAG, "UDP received: " + info);
				Message msg = new Message();
				msg.what = Constant.MSG_GOT_LOOMORDER;
				msg.obj = info;
				recHandler.sendMessage(msg);

			} catch (IOException e) {
				Log.e(TAG, "Error when receiver listen" , e);
			}

		}
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
