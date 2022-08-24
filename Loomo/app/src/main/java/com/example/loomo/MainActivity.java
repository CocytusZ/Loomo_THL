package com.example.loomo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loomo.basic.UltrasonicUnit;
import com.example.loomo.basic.communication.bluetooth.BluetoothUnit;
import com.example.loomo.basic.communication.udp.UdpUnit;
import com.example.loomo.basic.movement.MovementUnit;
import com.example.loomo.basic.position.PositionUnit;
import com.example.loomo.basic.sensorReg.SensorRegister;
import com.example.loomo.message.HandlerTag;
import com.example.loomo.message.LoomOrder;
import com.example.loomo.slam.pos.InParam;
import com.example.loomo.slam.realSense.MediaScanner;
import com.example.loomo.slam.realSense.RealSenseCamera;

public class MainActivity extends AppCompatActivity {
    /* ********************** *
     * Fields for UI   *
     * ********************** */
    SurfaceView colorPreview;
    SurfaceView depthPreview;
    ImageView filterPreview;

    Switch startSwitch;

    /* ********************** *
     * Fields for Components  *
     * ********************** */
    BluetoothUnit btUnit;
    PositionUnit posUnit;
    MovementUnit moveUnit;
    UltrasonicUnit ultraUnit;
    UdpUnit udpUnit;

    /* ********************** *
     *     Fields for SLAM    *
     * ********************** */
    RealSenseCamera realSenseCamera;
    /**
     * Auto created onCreate method gets called every time the app gets started
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        new SensorRegister(getApplicationContext()).registerAll();

        ultraUnit = new UltrasonicUnit(getApplicationContext());
        moveUnit = new MovementUnit(getApplicationContext(), ultraUnit);
        btUnit = new BluetoothUnit(this, moveUnit.getMoveHandler());
        posUnit = new PositionUnit(getApplicationContext(), moveUnit.getMoveHandler(), btUnit.getBTServerHandler());
        udpUnit = new UdpUnit(moveUnit.getMoveHandler());


        realSenseCamera = new RealSenseCamera(getApplicationContext(), visionHandler);

    }

    /**
     *  Auto created onResume method binding objects to the server
     */

    @Override
    protected void onResume(){
        super.onResume();
    }


    /*------------------------ View Initialization start-----------------------------*/
    private void initView(){
        colorPreview = findViewById(R.id.colorPreview);
        depthPreview = findViewById(R.id.depthPreview);
        filterPreview = findViewById(R.id.filterView);

        startSwitch = findViewById(R.id.startSwitch);
        initStartSwitch();

    }
    /* ********************** *
     *  Fields for Switch    *
     * ********************** */

    /*============= Start Switch =============*/
    public void initStartSwitch(){
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
//                    realSenseCamera.setPreview(colorPreview, depthPreview);
//                    realSenseCamera.startSimplecapture();
//                    // Code for capture and filter image for slam
//                    realSenseCamera.setFilterHandler(filterHandler);
//                    realSenseCamera.startCapture();

                    udpUnit.setRealSenseCamera(realSenseCamera);
                    // start position service
                    posUnit.start();
                    // start movement service
                    moveUnit.start();
                    // start bluetooth service
                    btUnit.initListener();
                }else{
                    realSenseCamera.suspend();
                }
            }
        });

        startSwitch.setActivated(false);
    }

    /* ********************** *
     *  Fields for Buttons    *
     * ********************** */
    double angle = 0;
    public void sendBtn(View v){
        angle += 1.57;
        LoomOrder order = LoomOrder.createPolarOrder(5,angle);

        Message msg = new Message();
        msg.what = HandlerTag.MSG_FROM_UDP;
        msg.obj = order;
        moveUnit.getMoveHandler().handleMessage(msg);
    }

    public void enableBT(View view){
        InParam in = new InParam();
        in.showIntrinsic();
    }

    public void reset(View view){
        new MediaScanner(this).scanCaptureDir();
    }

    /* --------------------------- *
     *    Fields for UI Handler    *
     * --------------------------- */
    private Handler filterHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap =(Bitmap) msg.obj;
            filterPreview.setImageBitmap(bitmap);
        }
    };

    private Handler visionHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Toast.makeText(getApplicationContext(), (String) msg.obj,Toast.LENGTH_LONG).show();
            startSwitch.setActivated(true);
        }
    };


    /*------------------------ View Initialization Finish -----------------------------*/

}
