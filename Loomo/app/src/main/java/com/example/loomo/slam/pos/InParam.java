package com.example.loomo.slam.pos;

import android.util.Log;

import com.segway.robot.sdk.vision.basic.Matrix3F;
import com.segway.robot.sdk.vision.calibration.Distortion;
import com.segway.robot.sdk.vision.calibration.Intrinsic2;

/**
 * @author Zhouyao
 * @Date: 2022/4/29
 * @Description:
 *  Class to read intrinsic of depth camera,
 *  Failed because the shit company did not finish this function
 */
public class InParam {
    private String TAG = "Intrinsic";
    Intrinsic2 inMat;

    public InParam(){
        if(inMat == null)
            inMat = new Intrinsic2();

    }

    public void showIntrinsic(){
        Matrix3F kf = inMat.kf;
        Distortion dis = inMat.distortion;
        Log.i(TAG, "Intrinsic2 Matrix: " + kf.toString());
        Log.i(TAG, "Intrinsic2 distortion: " + dis.toString());


//        Point2DF f;
//        Point2DF p;
//        Distortion d;
//        f = inMat.focalLength;
//        p = inMat.principal;
//        d = inMat.distortion;
//        Log.i(TAG, "focal length: " + f.toString());
//        Log.i(TAG, "principle: " + p.toString());
//        Log.i(TAG, "distortion: " + d.toString());
    }
}
