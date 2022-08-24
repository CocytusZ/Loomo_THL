package com.example.loomo.slam.tool;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.segway.robot.sdk.vision.frame.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhouyao
 * @Date: 2022/5/18
 * @Description:
 *  A filter to handle the low resolitoin issue of depth image
 */
public class FrameFilter extends Thread{
    private static String TAG = "FRAME_FILTER";
    /* Fields of filter */
    int width;
    int height;
    Bitmap.Config format;
    List<Bitmap> buffer;
    Bitmap output;

    int MAX_PIXEL_VAL = 65535;
    int MIN_PIXEL_VAL = 100;

    /* Fields of Thread */
    private int frameCounter;
    private int MAX_FRAME_NUM = 10; // default value is 10
    private boolean suspendFlag;
    private boolean killThread;
    private State state;
    private enum State{
        INIT, PROCESS, END
    }

    /* fields for UI */
    Handler imageHandler;
    Handler ioHandler;

    /**
     *  use 10 frame in filter
     * @param width
     * @param height
     * @param format
     */
    public FrameFilter(int width, int height, Bitmap.Config format){
        this.width = width;
        this.height = height;
        this.format = format;

        state = State.INIT;
        buffer = new ArrayList<>();
    }

    /**
     *
     * @param width
     * @param height
     * @param format format of bitmap
     * @param maxFrame the maximum num of frame which used in filter
     */
    public FrameFilter(int width, int height, Bitmap.Config format, int maxFrame ){
        this.width = width;
        this.height = height;
        this.format = format;
        this.MAX_FRAME_NUM = maxFrame;

        state = State.INIT;
        buffer = new ArrayList<>();
    }

    /**
     * Set image Handler for preview the result
     * @param handler Image handler
     */
    public void setPreviewHandler(Handler handler){
        this.imageHandler = handler;
    }

    /**
     * Set IO handler to save the output image
     * @param handler
     */
    public void setIOHandler(Handler handler){
        this.ioHandler = handler;
    }


    @Override
    public void run() {
        super.run();
        killThread = false;

        while (true){
            /* This thread is terminated */
            if(killThread) break;
            /* This thread is suspend */
            if(suspendFlag) continue;

             switch (state){
                 case INIT:{
                     if(!buffer.isEmpty()){
                         output = buffer.remove(0);
                     }

                     if(output != null){
                         state = State.PROCESS;
                     }
                 }break;

                 case PROCESS:{
                     if(!buffer.isEmpty()) {
                         Bitmap raw = buffer.remove(0);

                         // iterate content
                         for(int col = 0; col < width; col++){
                             for(int row = 0; row < height; row++){
                                 try {
                                     int pre = output.getPixel(col, row);
                                     int post = raw.getPixel(col, row);

                                     if (pre < post){
                                         output.setPixel(col, row, post);
                                     }
                                 }catch (Exception ex){
                                     ex.printStackTrace();

                                 }
                             }
                         }
                         frameCounter++;
                         if(frameCounter > MAX_FRAME_NUM){
                             state = State.END;
                         }
                     }
                 }break;

                 case END:{
                     // Send Bitmap for preview
                     Message msgPre = new Message();
                     msgPre.what = ImageInfo.FILTERED_DEPTH;
                     msgPre.obj = output;
                     imageHandler.sendMessage(msgPre);

                     // Send bitmap for save
                     Message msgIO = new Message();
                     msgIO.what = ImageInfo.FILTERED_DEPTH;
                     msgIO.obj = output;
                     ioHandler.sendMessage(msgIO);

                     // Clear buffer
                     suspendFlag = true;
                 }break;

                 default: break;
             }// Switch End
        }
    }

    /**
     * Reset and start frame filter
     */
    public void restart(){
        output = null;
        buffer.clear();
        frameCounter = 1;
        state = State.INIT;

        suspendFlag = false;
    }

    /**
     * Add frame to buffer, waiting for proceed
     * @param frame
     */
    public void addFrame(Frame frame){
        Bitmap bitmap = Bitmap.createBitmap(width, height, format);
        bitmap.copyPixelsFromBuffer(frame.getByteBuffer());
        buffer.add(bitmap);
    }

    /**
     * Add bitmap to buffer, waiting for proceed
     * @param bitmap
     */
    public void addBitmap(Bitmap bitmap){
        buffer.add(bitmap);
    }


}
