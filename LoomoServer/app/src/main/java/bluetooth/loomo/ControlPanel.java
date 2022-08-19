package bluetooth.loomo;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import bluetooth.loomo.bluetooth.BlueToothController;
import bluetooth.loomo.bluetooth.connect.AcceptThread;
import bluetooth.loomo.bluetooth.connect.ConnectThread;
import bluetooth.loomo.bluetooth.connect.BTConstant;
import bluetooth.loomo.bluetooth.Message.LoomOrder;
import bluetooth.loomo.bluetooth.Message.LoomoMsg;

/**
 * Activity for control panel
 * This activity will be called by Main activity after connection established
 * Used as an upper computer for Loomo
 */
public class ControlPanel extends AppCompatActivity {
    public static final String DEVICE_ID_KEY = "device_id";

    private static final String TAG = "BT_CONTROL_PANEL";

    private BlueToothController btController = new BlueToothController();

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private Handler panelHandler = new PanelHandler();

    /* **************************************
     *         Fields for UI Components     *
     ****************************************/

    // UI for state display
    TextView textCoord_x;
    TextView textCoord_y;
    TextView textSpeed;
    TextView textAngular;
    TextView textComment;

    // UI Components for Order input
    EditText inPosX;
    EditText inPosY;
    EditText inDir;
    EditText inDist;
    EditText inAng;

    // UI for Direct control mode
    Button forward;
    Button back;
    Button left;
    Button right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        initBluetooth();
        initUI();
        setDirectControlButtonListener();
    }

    private void initUI(){
        textCoord_x = findViewById(R.id.coordText_x);
        textCoord_y = findViewById(R.id.coordText_y);
        textSpeed = findViewById(R.id.speedText);
        textAngular = findViewById(R.id.angularText);
        textComment = findViewById(R.id.panel_comment);

        inDist = findViewById(R.id.inputDist);
        inAng = findViewById(R.id.inputAngle);
        inPosX = findViewById(R.id.inputX);
        inPosY = findViewById(R.id.inputY);
        inDir = findViewById(R.id.inputDir);

        forward = findViewById(R.id.panel_forwardBtn);
        back = findViewById(R.id.panel_backBtn);
        left = findViewById(R.id.panel_leftBtn);
        right = findViewById(R.id.panel_rightBtn);
    }

    /**
     * Method called when the activity is created
     * Establish connection to the target device according to the index from intent
     */
    private void initBluetooth(){
        int deviceID = getIntent().getIntExtra(DEVICE_ID_KEY, -1);
        if(deviceID != -1){
            Toast.makeText(this, "Device ID: " + deviceID,Toast.LENGTH_SHORT).show();
            // Get device according to index
            BluetoothDevice device = btController.getBoundDevice(deviceID);

            if (connectThread != null) {
                connectThread.cancel();
            }
            connectThread = new ConnectThread(device, btController.getAdapter(), panelHandler);
            connectThread.start();
        } else {
            Log.e(TAG, "Target Device ID Missing");
        }
    }


    /* *************************
     *     Message Handler     *
     ***************************/

    /**
     * Handler to process Message from Loomo
     */
    private class PanelHandler extends Handler {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case BTConstant.MSG_GOT_DATA:
//                    showToast("Data:" + String.valueOf(message.obj));
                    showLoomoState(message);
                    break;
                case BTConstant.MSG_ERROR:
                    showToast("error:" + String.valueOf(message.obj));
                    break;
                case BTConstant.MSG_CONNECTED_TO_SERVER:
                    // Connection established
                    showToast("连接到服务端");
                    break;
                case BTConstant.MSG_GOT_A_CLINET:
                    // Being connected
                    showToast("找到服务端");
                    break;
            }
        }
    }

    /**
     * Method to show data through Toast
     * @param text
     */
    private void showToast(String text){
        if(text != null){
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No Message", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to show loomo message on the control panel
     * @param message
     */
    private void showLoomoState(Message message){
        String data = String.valueOf(message.obj);
        LoomoMsg msg = new LoomoMsg(data);

        textCoord_x.setText(("X: " + msg.posX).toCharArray(), 0, 10);
        textCoord_y.setText(("Y: " + msg.posY).toCharArray(), 0, 11);
        textAngular.setText(msg.angular + " rad/s");
        textSpeed.setText(msg.speed + " m/s");
        textComment.setText(msg.comment);
    }


    /* **************************************************
     *      Methods to response Components on view      *
     ****************************************************/

    // -------------------- Fields to handle Loomo Moving order -------------- //

    private void setDirectControlButtonListener(){
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == forward.getId()){
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                       LoomOrder order = LoomOrder.createRawOrder(0, 1);
                       connectThread.setOrder(order);
                       connectThread.startSend(50);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        connectThread.finishSend();
                        connectThread.sendOrder(LoomOrder.createStopOrder());
                    }
                }
                return false;
            }
        });

        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == back.getId()){
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        LoomOrder order = LoomOrder.createRawOrder(0, -1);
                        connectThread.setOrder(order);
                        connectThread.startSend(50);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        connectThread.finishSend();
                        connectThread.sendOrder(LoomOrder.createStopOrder());
                    }
                }
                return false;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == left.getId()){
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        LoomOrder order = LoomOrder.createRawOrder(1, 0);
                        connectThread.setOrder(order);
                        connectThread.startSend(50);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        connectThread.finishSend();
                        connectThread.sendOrder(LoomOrder.createStopOrder());
                    }
                }
                return false;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == right.getId()){
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        LoomOrder order = LoomOrder.createRawOrder(-1, 0);
                        connectThread.setOrder(order);
                        connectThread.startSend(50);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        connectThread.finishSend();
                        connectThread.sendOrder(LoomOrder.createStopOrder());
                    }
                }
                return false;
            }
        });

    }


    // ---------------- Fields to handle Loomo State Message ------------------ //

    /**
     * Called when the "Start" button on control panel is clicked
     * Will send movement order to Loomo
     * @param view
     */
    public void panelStart(View view){
        LoomOrder order;

        if(inDist.getText() != null && inAng.getText() != null){
            double dist = Double.valueOf(inDist.getText().toString());
            double ang = Double.valueOf(inAng.getText().toString());

            order = LoomOrder.createPolarOrder(dist, ang);
            sendOrder(order);
            return;
        }

        if(inPosX.getText() != null && inPosY.getText() != null && inDir.getText() != null){
            double posX = Double.valueOf(inPosX.getText().toString());
            double posY = Double.valueOf(inPosY.getText().toString());
            double dir = Double.valueOf(inDir.getText().toString());

            order = LoomOrder.createCoordOrder(posX, posY, dir);
            sendOrder(order);
            return;
        }
    }

    /**
     * Stop Loomo at onece
     * @param view
     */
    public void panelStop(View view){
        LoomOrder order = LoomOrder.createStopOrder();
        sendOrder(order);
    }

    /**
     * Method when the right "Test" button is clicked
     * Will keep send test message to Loomo
     * @param view
     */
    public void panelRightTest(View view){

    }

    /**
     * Method used to send message to Loomo
     */
    private void sendOrder(LoomOrder order){
        if (connectThread != null) {
            connectThread.sendOrder(order);
        }
    }
}