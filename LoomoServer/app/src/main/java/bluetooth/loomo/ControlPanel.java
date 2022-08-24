package bluetooth.loomo;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import bluetooth.loomo.bluetooth.BlueToothController;
import bluetooth.loomo.bluetooth.connect.ConnectThread;
import bluetooth.loomo.bluetooth.connect.BTConst;
import bluetooth.loomo.config.ConnMode;
import bluetooth.loomo.config.Message.HandlerTag;
import bluetooth.loomo.config.Message.LoomOrder;
import bluetooth.loomo.config.Message.LoomState;
import bluetooth.loomo.udp.UdpUnit;

/**
 * Activity for control panel
 * This activity will be called by Main activity after connection established
 * Used as an upper computer for Loomo
 */
public class ControlPanel extends AppCompatActivity {
    public static final String DEVICE_ID_KEY = "device_id";

    private static final String TAG = "CONTROL_PANEL";

    /* **************************************
     *         Fields for Communicatoin     *
     ****************************************/
    ConnMode connMode = ConnMode.BLUETOOTH;

    /* ---------------- Bluetooth --------------- */
    private BlueToothController btController = new BlueToothController();
    private ConnectThread connectThread;

    /* ---------------- UDP --------------- */
    UdpUnit udpUnit;

    private Handler panelHandler = new PanelHandler();

    /* **************************************
     *         Fields for UI Components     *
     ****************************************/

    // UI for state display
    TextView textCoord_x;
    TextView textCoord_y;
    TextView textTheta;
    TextView textComment;
    ImageView imageView;

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
    Button imageBtn;

    //UI for udp connection
    EditText ipText;
    EditText portText;
    EditText udpMsgText;
    Button udpBtn;


    Switch conSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        initBluetooth();
        initUI();
        initUDP();

        setDirectControlButtonListener();
    }

    private void initUI(){
        textCoord_x = findViewById(R.id.coordText_x);
        textCoord_y = findViewById(R.id.coordText_y);
        textTheta = findViewById(R.id.thetaText);
        textComment = findViewById(R.id.panel_comment);
        imageView = findViewById(R.id.camera_image);

        inDist = findViewById(R.id.inputDist);
        inAng = findViewById(R.id.inputAngle);
        inPosX = findViewById(R.id.inputX);
        inPosY = findViewById(R.id.inputY);
        inDir = findViewById(R.id.inputDir);

        forward = findViewById(R.id.panel_forwardBtn);
        back = findViewById(R.id.panel_backBtn);
        left = findViewById(R.id.panel_leftBtn);
        right = findViewById(R.id.panel_rightBtn);
        imageBtn = findViewById(R.id.reqImg);

        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);
        udpMsgText = findViewById(R.id.udpMsgText);
        udpBtn = findViewById(R.id.udpSend);

        conSwitch = findViewById(R.id.conSwitch);
        conSwitch.setOnCheckedChangeListener(switchListener);
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

    private void initUDP(){
        udpUnit = new UdpUnit();
        udpUnit.setDataHandler(panelHandler);
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
                case BTConst.MSG_GOT_DATA:
                    showToast("Data:" + String.valueOf(message.obj));
//                    showLoomoState(message);
                    break;
                case BTConst.MSG_ERROR:
                    showToast("error:" + String.valueOf(message.obj));
                    break;
                case BTConst.MSG_CONNECTED_TO_SERVER:
                    // Connection established
                    showToast("连接到服务端");
                    break;
                case BTConst.MSG_GOT_A_CLINET:
                    // Being connected
                    showToast("找到服务端");
                    break;

                case HandlerTag
                        .MSG_FROM_UDP:
                    showToast("Data:" + String.valueOf(message.obj));
                    showLoomoState(message);
//                    Bitmap bitmap =

//                    imageView.setImageBitmap(bitmap);
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
        LoomState msg = new LoomState(data);

//        textCoord_x.setText(("X: " + msg.posX).toCharArray(), 0, 10);
//        textCoord_y.setText(("Y: " + msg.posY).toCharArray(), 0, 11);
//        textTheta.setText(msg.angular + " rad/s");
//
//        textComment.setText(msg.comment);
    }


    /* **************************************************
     *      Methods to response Components on view      *
     ****************************************************/
    CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(isChecked){
                connMode = ConnMode.UDP;
            }else{
                connMode = ConnMode.BLUETOOTH;
            }
        }
    };

    // --------------------  UDP Connection -------------------------- //
    public void udpConn(View view){
        String ip;
        int port;
        String msg;

        ip = ("InetAddress".equals(ipText.getText().toString())) ? "192.168.146.94" :  ipText.getText().toString();
        port = portText.getText().toString().equals("Port") ? 1122 : Integer.parseInt(portText.getText().toString());
        msg = udpMsgText.getText().toString();

        udpUnit.setRemoteDevice(ip, port);
        udpUnit.send(msg);
        Log.i(TAG, "udpSend: address=[" + ip + "], port=[" + port +"], msg=[" + msg + "]");
    }

    // -------------------- Methods for sending order -------------- //

    /**
     * Used to send order for once
     * @param order
     */
    private void sendOrderOnce(LoomOrder order){
        switch (this.connMode){
            case BLUETOOTH:
                connectThread.sendOrder(order);
                break;
            case UDP:
                udpUnit.send(order);
                break;

            default: break;
        }
    }

    /**
     * Start continuously sending message
     * @param order the order need to send
     * @param interval the interval between two messages
     */
    private void startSend(LoomOrder order, int interval){
        switch (this.connMode){
            case BLUETOOTH:
                connectThread.setOrder(order);
                connectThread.startSend(interval);
                break;

            case UDP:
                udpUnit.setOrder(order);
                udpUnit.startSend(interval);
                break;

            default: break;
        }
    }

    /**
     * Finish sending order
     */
    private void finishSend(){
        switch (this.connMode){
            case BLUETOOTH:
                connectThread.finishSend();
                break;

            case UDP:
                udpUnit.finishSend();
                break;

            default: break;
        }
    }

    /* **************************************************
     *         Methods to send order to Loomo           *
     ****************************************************/

    // -------------------- Fields to send Loomo Moving direct order -------------- //

    private void setDirectControlButtonListener(){
        final int interval = 100;

        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(view.getId() == forward.getId()){
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                       LoomOrder order = LoomOrder.createRawOrder(0, 1);
                       startSend(order, interval);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        finishSend();
                        sendOrderOnce(LoomOrder.createStopOrder());
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
                        startSend(order, interval);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        finishSend();
                        sendOrderOnce(LoomOrder.createStopOrder());
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
                        startSend(order, interval);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        finishSend();
                        sendOrderOnce(LoomOrder.createStopOrder());
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
                        startSend(order, interval);
                    }

                    if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                        finishSend();
                        sendOrderOnce(LoomOrder.createStopOrder());
                    }
                }
                return false;
            }
        });
    }

    public void onRecImg(View view){
        LoomOrder order = new LoomOrder();
        order.mode = LoomOrder.REPORT_IMG;
        udpUnit.send(order);
    }


    // ---------------- Method to send Loomo coordinate order ------------------ //

    /**
     * Called when the "Start" button on control panel is clicked
     * Will send movement order to Loomo
     * @param view
     */
    public void panelStart(View view){
        LoomOrder order;

        if(!inDist.getText().toString().equals("")
                && !inAng.getText().toString().equals("")){
            double dist = Double.valueOf(inDist.getText().toString());
            double ang = Double.valueOf(inAng.getText().toString());

            order = LoomOrder.createPolarOrder(dist, ang);
            sendOrderOnce(order);
            return;
        }

        if(!inPosX.getText().toString().equals("")
                && !inPosY.getText().toString().equals("")
                && !inDir.getText().toString().equals("")){
            double posX = Double.valueOf(inPosX.getText().toString());
            double posY = Double.valueOf(inPosY.getText().toString());
            double dir = Double.valueOf(inDir.getText().toString());

            order = LoomOrder.createCoordOrder(posX, posY, dir);
            sendOrderOnce(order);
            return;
        }

        order = LoomOrder.createStopOrder();
        sendOrderOnce(order);
    }

    // ---------------- Fields to handle Loomo State Message ------------------ //

    /**
     * Stop Loomo at onece
     * @param view
     */
    public void panelStop(View view){
        LoomOrder order = LoomOrder.createStopOrder();
        sendOrderOnce(order);
    }


}