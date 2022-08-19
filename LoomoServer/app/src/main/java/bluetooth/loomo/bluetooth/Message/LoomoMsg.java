package bluetooth.loomo.bluetooth.Message;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to encapsulate message from Loomo
 * The message can be converted to JSON format
 * Also a formatted JSON can be converted to object of this class
 */
public class LoomoMsg {
    private static final String TAG = "LOOMO MESSAGE";

    private static final String POSX_KEY = "posx";
    private static final String POSY_KEY = "posy";
    private static final String SPEED_KEY = "speed";
    private static final String ANGULAR_KEY = "ang";
    private static final String COMMENT_KEY = "comment";

    /* ****************************
     *      Fields of message     *
     ******************************/
    public double posX, posY;
    public double speed,angular;
    public String comment;

    public LoomoMsg(double x, double y, double speed, double angular, String comment){
        this.posX = x;
        this.posY = y;
        this.speed = speed;
        this.angular = angular;
        this.comment = comment;
    }

    public LoomoMsg(String jsonStr){
        try {
            JSONObject json = new JSONObject(jsonStr);
            this.posX = json.getDouble(POSX_KEY);
            this.posY = json.getDouble(POSY_KEY);
            this.speed = json.getDouble(SPEED_KEY);
            this.angular = json.getDouble(ANGULAR_KEY);
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
            json.put(SPEED_KEY, speed);
            json.put(ANGULAR_KEY, angular);
            json.put(COMMENT_KEY, comment);
        } catch (Exception e) {
            Log.e(TAG, "toJSON: Packing failed");
        }
        return json.toString().getBytes();
    }
}
