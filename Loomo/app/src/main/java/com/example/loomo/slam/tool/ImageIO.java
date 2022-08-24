package com.example.loomo.slam.tool;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.loomo.slam.pos.Extrinsic;
import com.segway.robot.sdk.vision.frame.Frame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * @date: 24.04.2022
 * @author Zhouyao
 * This class is used to save color image, depth image and pos info to disk.
 * It will create a new folder called capture in the root dir of MTP folder
 */
public class ImageIO{
    private String TAG = "Image IO";

    /* Fields for Image format */
    public static int IMAGE_WIDTH = ImageInfo.DEPTH_WIDTH;
    public static int IMAGE_HEIGHT = ImageInfo.DEPTH_HEIGHT;

    /* fields for IO operation */
    private int index;
    private String colorPath = "/color/";
    private String depthPath = "/depth/";
    private String filterPath = "/filterDepth/";
    private String pgmPath = "/pgmDepth/";
    private String posFilePath = "/pose.txt";


    /* Fields for buffer and processing */
    private Bitmap colorMap;
    private Bitmap depthMap;
    private Extrinsic.Pos pos;


    /* Fields for state machine and Thread */
    private boolean hasColor;
    private boolean hasDepth;
    private boolean hasPos;


    public ImageIO(){
        initDir();
        this.index = 0;

        hasColor = false;
        hasDepth = false;
        hasPos = false;
    }

    /* -------------------------------  Storage path generate start ----------------------------  *
     *
     *  This part has two method:
     *      1. initDir(): called once to generate the dir to store file
     *
     *
     * -----------------------------------------------------------------------------------------  */
    /**
     * create dir to store images
     * Run once
     */
    private void initDir(){
        /* set up root dir  */
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR) + 1;
        int min = calendar.get(Calendar.MINUTE);

        String prefix = "0" +String.valueOf(month) + "_" + String.valueOf(day) + "_HR" + String.valueOf(hour) + "_" + String.valueOf(min);
        prefix = Environment.getExternalStorageDirectory().getPath() + "/Capture/" + prefix;

        /* --------------- set up the dir to store each picture ---------------------- */
        colorPath = prefix + colorPath;
        depthPath = prefix + depthPath;
        filterPath = prefix + filterPath;
        pgmPath = prefix + pgmPath;
        posFilePath = prefix + posFilePath;

        File cDir = new File(colorPath);
        File dDir = new File(depthPath);
        File fDir = new File(filterPath);
        File pDir = new File(pgmPath);

        if(!cDir.exists()) cDir.mkdirs();
        if(!dDir.exists()) dDir.mkdirs();
        if(!fDir.exists()) fDir.mkdirs();
        if(!pDir.exists()) pDir.mkdirs();
    }

    /* ======================= Storage path generate Finish  ========================== */


    /* ----------------------------- Image Handler Start  ---------------------------------------------  *
     *  This part has one field:
     *      1. realsenseHandler: used to inform realsense of save is complete
     *
     * ------------------------------------------------------------------------------------------------  */
    /**
     * This handler is used to inform realsense of save is complete
     */
    private Handler realsenseHandler;
    public void setSenseHandler(Handler handler){
        this.realsenseHandler = handler;
    }


    /* ----------------------------- Image Handler Start  ---------------------------------------------  *
     *  This part has one field:
     *      1. ioHandler:  used to receive the output of filter which is a bitmap
     *                      When message received, all images and pos will be stored to designated path
     *
     * ------------------------------------------------------------------------------------------------  */
    public Handler ioHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case ImageInfo.FILTERED_DEPTH:{
                    index++;

                    saveBitmap( colorPath + index + ".png", colorMap);
                    saveBitmap( depthPath + index + ".png", depthMap);
                    saveBitmap( filterPath + index + ".png",(Bitmap) msg.obj);
                    savePGM(pgmPath + index + ".pgm", (Bitmap) msg.obj);
                    savePoseFile( posFilePath, pos );

                    hasColor = false;
                    hasDepth = false;
                    hasPos = false;

                    Message msg2vision = Message.obtain();
                    msg2vision.what = ImageInfo.SAVE_COMPLETE;
                    realsenseHandler.sendMessage(msg2vision);
                }break;
            }
            return false;
        }
    });
    /* ========================== Image Handler Finished =================================== */


    /**
     * Set the color frame need to save
     * @param frame {the data type offered by segway}
     */
    public void addColorFrame(Frame frame){
        if(!hasColor) {
            // If there is no bitmap, then add
            this.colorMap = Bitmap.createBitmap(ImageInfo.COLOR_WIDTH, ImageInfo.COLOR_HEIGHT, Bitmap.Config.ARGB_8888);
            this.colorMap.copyPixelsFromBuffer(frame.getByteBuffer());
            hasColor = true;
        }
    }

    /**
     * Set the depth frame need to save
     * @param frame {the data type offered by segway}
     */
    public void addDepthFrame(Frame frame){
        if(!hasDepth) {
            this.depthMap = Bitmap.createBitmap(ImageInfo.DEPTH_WIDTH, ImageInfo.DEPTH_HEIGHT, Bitmap.Config.RGB_565);
            this.depthMap.copyPixelsFromBuffer(frame.getByteBuffer());
            hasDepth = true;
        }
    }

    /**
     * Set the pose
     * @param pos
     */
    public void addPos(Extrinsic.Pos pos){
        if(!hasPos){
            this.pos = pos;
            hasPos = true;
        }
    }

    /**
     * Method to save all info for slam to disk
     * @param path
     * @param pos
     */
    private void savePoseFile(String path, Extrinsic.Pos pos){
        FileWriter fw = null;
        try {
            File file = new File(path);

            fw = new FileWriter(file,true);
            fw.write(pos.toString() + "\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fw!=null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save image in pgm file
     * @param path the image path
     * @param bitmap the target Bitmap need to be saved
     */
    private void savePGM(String path, Bitmap bitmap){
        PGM16 pgm = new PGM16(path, IMAGE_WIDTH, IMAGE_HEIGHT);
        pgm.setImageContent(bitmap);
    }


    /**
     * Save frame as a bitmap
     * @param path image file path
     * @param bitmap target bitmap need to be saved
     */
    private void saveBitmap(String path, Bitmap bitmap){
        Bitmap colorMap = resize(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT);

        try {
            File colorFile = new File(path);
            FileOutputStream cOut = new FileOutputStream(colorFile);
            colorMap.compress(Bitmap.CompressFormat.PNG, 100, cOut);

            cOut.flush();
            cOut.close();
        }catch (Exception exception){
            exception.printStackTrace();
            Log.e(TAG, "saveBitmap: ERROR");
        }
    }

    /**
     * Method to resize map to a satisfied scale
     * @param input
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private Bitmap resize(Bitmap input, float targetWidth, float targetHeight){
        float scaleW = targetWidth / input.getWidth();
        float scaleH = targetHeight / input.getHeight();

        Matrix scaleMat = new Matrix();
        scaleMat.postScale(scaleW, scaleH);
        return Bitmap.createBitmap(input, 0, 0, input.getWidth(), input.getHeight(), scaleMat, true);
    }
}
