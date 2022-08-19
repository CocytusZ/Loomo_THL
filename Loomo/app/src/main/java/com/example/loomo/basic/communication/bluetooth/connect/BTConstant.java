package com.example.loomo.basic.communication.bluetooth.connect;

/**
 * @Description:
 * Constants for Bluetooth communication
 * @author : Zhouyao
 * Date: 2021/12/26
 */
public class BTConstant {
    public static final String CONNECTTION_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * Start Listening
     */
    public static final int MSG_START_LISTENING = 1;

    /**
     * Finish Listening
     */
    public static final int MSG_FINISH_LISTENING = 2;

    /**
     * Client connected
     */
    public static final int MSG_GOT_A_CLINET = 3;

    /**
     * Server connected
     */
    public static final int MSG_CONNECTED_TO_SERVER = 4;

    /**
     * Get message of loomo order
     */
    public static final int MSG_GOT_LOOMORDER = 5;

    /**
     * Get message of loomo state
     */
    public static final int MSG_GOT_LOOMOSTATE = 6;
    /**
     * error
     */
    public static final int MSG_ERROR = -1;
}
