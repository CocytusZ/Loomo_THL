package com.example.loomo.basic;

import android.content.Context;
import android.widget.TextView;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Recognizer;

/**
 * @Description:
 * Class to encapsulate initialization of Recognizer, which include:
 * 1. Instance of Recognizer
 * 2. Initialization of Grammar
 *
 * @author : Zhouyao
 * Date: 2021/12/26
 */
public class RecognizerUnit {

    private static Recognizer recognizer;

    /**
     * Used to show the result of Recognition
     */
    public static String resultMsg = "Recognizer: ";
    public static TextView textView;

    /**
     * Initialize Sensor only, without service listener
     * To Bind service listener
     */
    public RecognizerUnit(){
        if(recognizer == null) {
            recognizer = Recognizer.getInstance();
        }
    }

    /**
     * Method to get the instance of Recognizer  itself
     * @return {Recognizer} recognizer
     */
    public Recognizer getUnit(){
       return recognizer;
    }

    public static void bindService(Context context, ServiceBinder.BindStateListener listener){
        recognizer.bindService(context, listener);
    }



    public static void clearResult(){
        resultMsg = "Recognizer: ";
    }

    /**
     * Used to Debug, show the result of Recognition
     */
    public static void showRecogResult(String str){
        resultMsg = "Recognizor: " + str;
        textView.setText(resultMsg);
    }
}
