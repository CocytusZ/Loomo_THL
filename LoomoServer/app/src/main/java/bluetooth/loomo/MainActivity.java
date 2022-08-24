package bluetooth.loomo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import bluetooth.loomo.bluetooth.BlueToothController;
import bluetooth.loomo.bluetooth.DeviceAdapter;
import bluetooth.loomo.bluetooth.connect.AcceptThread;
import bluetooth.loomo.bluetooth.connect.ConnectThread;
import bluetooth.loomo.bluetooth.connect.BTConst;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 0;
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mBoundDeviceList = new ArrayList<>();

    private BlueToothController mController = new BlueToothController();
    private Handler mUIHandler = new MyHandler();

    private ListView mListView;
    private DeviceAdapter mAdapter;
    private Toast mToast;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        initActionBar();
        initUI();

        registerBluetoothReceiver();

        //软件运行时直接申请打开蓝牙
        mController.turnOnBlueTooth(this,REQUEST_CODE);
    }

    private void registerBluetoothReceiver(){
        IntentFilter filter = new IntentFilter();
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(receiver, filter);
    }

    //注册广播监听搜索结果
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                //setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                //setProgressBarIndeterminateVisibility(false);
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个添加一个
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();

            } else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {  //此处作用待细查
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if(scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    setProgressBarIndeterminateVisibility(true);
                } else {
                    setProgressBarIndeterminateVisibility(false);
                }

            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(remoteDevice == null) {
                    showToast("无设备");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if(status == BluetoothDevice.BOND_BONDED) {
                    showToast("已绑定" + remoteDevice.getName());
                } else if(status == BluetoothDevice.BOND_BONDING) {
                    showToast("正在绑定" + remoteDevice.getName());
                } else if(status == BluetoothDevice.BOND_NONE) {
                    showToast("未绑定" + remoteDevice.getName());
                }
            }
        }
    };

    //初始化用户界面
    private void initUI() {
        mListView = findViewById(R.id.device_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(bindDeviceClick);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()){
            case R.id.control_panel:{
                Intent intent = new Intent(this, ControlPanel.class);
                startActivity(intent);
            }break;

            case R.id.enable_visibility:{
                mController.enableVisibily(this);
            }break;

            // Find device and bind if click
            case R.id.find_device:{
                mAdapter.refresh(mDeviceList);
                mController.findDevice();
                mListView.setOnItemClickListener(bindDeviceClick);
            }break;

            // Check bound device and establish connection if click
            case R.id.bonded_device:{
                mBoundDeviceList = mController.getBoundDeviceList();
                mAdapter.refresh(mBoundDeviceList);
                mListView.setOnItemClickListener(connectDeviceClick);
            }break;

            case R.id.disconnect:{
                if( mConnectThread != null) {
                    mConnectThread.cancel();
                }
            }

            // Listening
            case R.id.listening:{
                if(mAcceptThread != null){
                    mAcceptThread.cancel();
                }
                mAcceptThread = new AcceptThread(mController.getAdapter(), mUIHandler);
                mAcceptThread.start();
            }break;

            case R.id.stop_listening:{
                if(mAcceptThread != null){
                    mAcceptThread.cancel();
                }
            }break;

            case R.id.say_hello:{
                say("Hello");
            }break;

            case R.id.say_hi:{
                say("Hi");
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void say(String word) {
        if (mAcceptThread != null) {
            try {
                mAcceptThread.sendData(word.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        else if( mConnectThread != null) {
            try {
                mConnectThread.sendData(word.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * Listener for Items in listview that show discovered devices
     */
    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
        /**
         * Called when item is clicked
         * Will bind the device showed by item to this device
         * @param adapterView
         * @param view
         * @param i
         * @param l
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mDeviceList.get(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };

    /**
     * Listener for Items in listview that show bound devices
     */
    private AdapterView.OnItemClickListener connectDeviceClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            toControlPanel(i);
        }
    };

    /**
     * Method to turn to control panel by intent
     * Pass the index of target Bluetooth device as param
     */
    private void toControlPanel(int deviceID){
        Intent intent = new Intent(this, ControlPanel.class);
        intent.putExtra(ControlPanel.DEVICE_ID_KEY, deviceID);
        startActivity(intent);
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case BTConst.MSG_GOT_DATA:
                    showToast("data:" + String.valueOf(message.obj));
                break;
                case BTConst.MSG_ERROR:
                showToast("error:" + String.valueOf(message.obj));
                break;
                case BTConst.MSG_CONNECTED_TO_SERVER:
                    showToast("连接到服务端");
                break;
                case BTConst.MSG_GOT_A_CLINET:
                    showToast("找到服务端");
                break;
            }
        }
    }

    //设置toast的标准格式
    private void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(this, text,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            mToast.setText(text);
            mToast.show();
        }

    }

    /**
     * 退出时注销广播、注销连接过程、注销等待连接的监听
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }

        unregisterReceiver(receiver);
    }
}
