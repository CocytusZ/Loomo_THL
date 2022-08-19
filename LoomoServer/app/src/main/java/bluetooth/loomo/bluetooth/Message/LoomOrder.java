package bluetooth.loomo.bluetooth.Message;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to encapsulate Order for Loomo
 * The message can be converted to JSON format
 * Also a formatted JSON can be converted to object of this class
 */
public class LoomOrder {
    private static final String TAG = "LOOMO ORDER";

    private static final String POSX_KEY = "order_pos_x";
    private static final String POSY_KEY = "order_pos_y";
    private static final String DIR_KEY = "order_dir";
    private static final String DIST_KEY = "order_dist";
    private static final String ANGLE_KEY = "order_angle";
    private static final String MODE_KEY = "order_mode";
    private static final String RAWX_KEY = "order_raw_x";
    private static final String RAWY_KEY = "order_raw_y";

    private static final int STOP_MODE = 0;
    private static final int POLAR_MODE = 1;
    private static final int COORD_MODE = 2;
    private static final int RAW_MODE = 3;

    public double posX, posY, dir;
    public double dist, ang;
    public int mode;
    public double rawX, rawY;


    private LoomOrder(){
    }

    /**
     * Create stop oder for Loomo
     */
    public static LoomOrder createStopOrder(){
        LoomOrder order = new LoomOrder();
        order.mode = STOP_MODE;

        return order;
    }

    /**
     * Create raw moving order for Loomo
     * through polar coordination
     * @param dist
     * @param ang
     */
    public static LoomOrder createPolarOrder(double dist, double ang){
        LoomOrder order = new LoomOrder();
        order.mode = POLAR_MODE;

        order.dist = dist;
        order.ang = ang;

        return  order;
    }

    /**
     * Create coordinate moving order for Loomo
     * through cartesian coordination
     * @param posX coordinate in X axis
     * @param posY coordinate in Y axis
     * @param dir the heading direction after movement
     */
    public static LoomOrder createCoordOrder(double posX, double posY, double dir){
        LoomOrder order = new LoomOrder();
        order.mode = COORD_MODE;

        order.posX = posX;
        order.posY = posY;
        order.dir = dir;

        return order;
    }

    /**
     * Create Raw moving order for Loomo
     * @param rawX is the X coordinate on Server touch pad
     * @param rawY is the Y coordinate on Server touch pad
     * @return
     */
    public static LoomOrder createRawOrder(double rawX, double rawY){
        LoomOrder order = new LoomOrder();
        order.mode = RAW_MODE;

        order.rawX = rawX;
        order.rawY = rawY;

        return order;
    }

    /**
     * Create Loomo order from Json String
     * @param jsonStr
     */
    public LoomOrder(String jsonStr){
        try {
            JSONObject json = new JSONObject(jsonStr);
            this.mode = json.getInt(MODE_KEY);

            switch (this.mode){
                case STOP_MODE: break;

                case POLAR_MODE:{
                    this.dist = json.getDouble(DIST_KEY);
                    this.ang = json.getDouble(ANGLE_KEY);
                }break;

                case COORD_MODE:{
                    this.posX = json.getDouble(POSX_KEY);
                    this.posY = json.getDouble(POSY_KEY);
                    this.dir = json.getDouble(DIR_KEY);
                }break;

                case RAW_MODE:{
                    this.rawX = json.getDouble(RAWX_KEY);
                    this.rawY = json.getDouble(RAWY_KEY);
                }
            }
        } catch (JSONException e){
            Log.e(TAG, "LoomOrder: Analyzing Order failed");
        }
    }

    /**
     *
     * @return
     */
    public byte[] toJsonBytes(){
        JSONObject json = new JSONObject();
        try {
            json.put(MODE_KEY, this.mode);
            switch (this.mode){
                case STOP_MODE: break;

                case POLAR_MODE:{
                    json.put(DIST_KEY, this.dist);
                    json.put(ANGLE_KEY, this.ang);
                }break;

                case COORD_MODE:{
                    json.put(POSX_KEY, this.posX);
                    json.put(POSY_KEY, this.posY);
                    json.put(DIR_KEY, this.dir);
                }break;

                case RAW_MODE:{
                    json.put(RAWX_KEY, this.rawX);
                    json.put(RAWY_KEY, this.rawY);
                }break;

                default: throw new Exception();
            }

        } catch (JSONException e) {
            Log.e(TAG, "toJsonBytes: Packing Json failed");
        } catch (Exception e){
            Log.e(TAG, "toJsonBytes: Order Error");
        }

        return json.toString().getBytes();
    }
}
