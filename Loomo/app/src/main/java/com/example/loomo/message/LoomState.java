package com.example.loomo.message;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

/**
 * @Description:
 * Class to encapsulate message from Loomo, which contains: coordinate, heading angle, speed and self-defined comment
 * The message can be converted to JSON format
 * Also a formatted JSON can be converted to object of this class
 *
 * @date: 2021/12/26
 * @author : Zhouyao
 */
public class LoomState {
    private static final String TAG = "LOOMO MESSAGE";

    private static final String POSX_KEY = "posx";
    private static final String POSY_KEY = "posy";
    private static final String THETA_KEY = "theta";
    private static final String COMMENT_KEY = "comment";
    private static final String IMAGE_KEY = "image";

    /* ****************************
     *      Fields of message     *
     ******************************/
    public double posX, posY;
    public double theta;
    public String comment;

    public LoomState(double x, double y, double theta, String comment){
        this.posX = x;
        this.posY = y;
        this.theta = theta;
        this.comment = comment;
    }

    public LoomState(String jsonStr){
        try {
            JSONObject json = new JSONObject(jsonStr);
            this.posX = json.getDouble(POSX_KEY);
            this.posY = json.getDouble(POSY_KEY);
            this.theta = json.getDouble(THETA_KEY);
            this.comment = json.getString(COMMENT_KEY);
        } catch (JSONException e) {
            Log.e(TAG, "Create LoomoMsg: Initialize from JSON object failed");
            e.printStackTrace();
        }
    }

    /**
     * Pack this message into bytes
     * First to JSON format, then to String and byte[]
     * Used to send message through bluetooth
     * @return
     */
    public byte[] toJsonBytes(){
        JSONObject json = new JSONObject();

        try {
            json.put(POSX_KEY, posX);
            json.put(POSY_KEY, posY);
            json.put(THETA_KEY, theta);
            json.put(COMMENT_KEY, comment);
        } catch (Exception e) {
            Log.e(TAG, "toJSON: Packing failed");
        }
        return json.toString().getBytes();
    }
}
