package com.example.loomo.basic.position.source;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @date 28.12.2021
 * @author Zhouyao
 *
 * @Description:
 * This is a class to encapsulate MQTT method for getting position info through sensors in corridor
 * This position info will act as one of the sources of Loomo's position system,
 * and thus this class will be a member of PositionUnit
 */
public class MQTTClient {
    // Local Var for android
    Context mContext;
    String TAG = "MQTT: ";

    //mqtt settings
    private static String ServerURI = "ssl://iot.th-luebeck.de:8883";
    private static String SubscrineTopic = "thl/cosa/localization/groundtruth";
    private static String PublishTopic = "thl/cosa/localization/loomo";
    private static String UserName = "schmidts";
    private static String Password = "sciwo_2021";


    private int MQTT_QOS = 1;
    MqttAndroidClient client;

    public MQTTClient(Context context){
        this.mContext = context;

    }

    /**
     * Implemented in MainActivity, Called by initMqtt()
     * Used to print MQTT message on screen
     */
    public interface DataListener {
        /**
         * Function to print coordination on screen
         * @param x is the x-coordinate
         * @param y is the y-coordinate
         */
        void onMsgReturned(String x, String y);
    }


    public void initMqtt(DataListener listener){
        //String clientId = MqttClient.generateClientId();
        String clientId="Loomo";
        client =
                new MqttAndroidClient(mContext, ServerURI,
                        clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //message format example:
                //"vls start forward setX:4; setY:0"
                // this will set vls mode on, set the current pattern to rectangle and will set the X,Y coordinates.
                //another example to set parameter but not start the movement
                //"forward setX:3"

                String mqttMessage=new String(message.getPayload());

                //here the message can be interpreted
                Log.d(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));

                int x_index = mqttMessage.indexOf("X");
                int y_index = mqttMessage.indexOf("Y");
                // "+2" is used to skip ":" in String
                // "mqttMessage.length() - 1" used to skip "," at the end
                String posX = mqttMessage.substring(x_index + 2, y_index);
                String posY = mqttMessage.substring(y_index + 2, mqttMessage.length());

                listener.onMsgReturned(posX, posY);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(UserName);
        options.setPassword(Password.toCharArray());


        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "Success MQTT");

                    //subscribe to topic
                    try {
                        IMqttToken subToken = client.subscribe(SubscrineTopic, MQTT_QOS );
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // Subscription succeed
                                Log.i(TAG, "subscribed succeed");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Log.i(TAG, "subscribed failed");

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "Failure MQTT");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMQTT(){
        String message= "Test Message";
        try {
            client.publish(PublishTopic, message.getBytes(),MQTT_QOS,false);
            Log.i(TAG, "COORDINATES SEND");
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    public void OnStop() throws MqttException {
        IMqttToken disconToken = client.disconnect();
    }
}
