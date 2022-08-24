package com.example.loomo.slam.realSense;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.loomo.slam.pos.Extrinsic;
import com.example.loomo.slam.tool.FrameFilter;
import com.example.loomo.slam.tool.ImageIO;
import com.example.loomo.slam.tool.ImageInfo;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamInfo;
import com.segway.robot.sdk.vision.stream.StreamType;

/**
 * @date 24.04.2022
 * @author Zhouyao
 * This is the class to encapsulate Realsense Camera
 * The camera has two output: color image and depth image
 * The color image is 640 by 480 pixel in ARGB8888 format
 * The depth image is 320 by 240 pixel in Z16 format
 *
 */
public class RealSenseCamera{
    private static final String TAG = "PreviewPresenter";

    private Vision mVision;
    private StreamInfo[] streamInfos;
    private Extrinsic extrinsic = new Extrinsic();

    public RealSenseCamera(Context context, Handler reminder) {
        initImageOperator();

        this.mVision = Vision.getInstance();
        this.mVision.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Message msg = Message.obtain();
                msg.obj = "Vision component Ready";
                reminder.sendMessage(msg);
            }

            @Override
            public void onUnbind(String reason) {
            }
        });
    }

    /* ============================ Field for thread ============================= */
    private boolean killThread = false;
    // Use as a counter to skip some frame
    private int OMIT_FRAME = 1;
    private int frameCounter = 0;
    /* ---------------------------------------------------------------------------- */

    /* =========================== Field for preview =============================== *
    *
    *   Two field for preview view
    *   Methods:
    *       1. setPreview : set surface view for preview
    *       2. startPreview:
    *       3. stopPreview:
    *
    * ============================================================================== */
    SurfaceView colorPreview;
    SurfaceView depthPreview;

    /**
     * Start preview of realsense camera on main activity
     */
    public void startPreview(){
        if(colorPreview != null) mVision.startPreview(StreamType.COLOR, colorPreview.getHolder().getSurface());
        if(depthPreview != null) mVision.startPreview(StreamType.DEPTH, depthPreview.getHolder().getSurface());
    }

    /**
     * Start preview of realsense camera on main activity
     */
    public void stopPreview(){
        if(colorPreview != null) mVision.stopPreview(StreamType.COLOR);
        if(depthPreview != null) mVision.stopPreview(StreamType.DEPTH);
    }

    /**
     * This method is to set preview of camera image
     * You can bind it to any surface view
     * Once the view is bound, the preview will automatically start
     * @param mColorSurfaceView
     * @param mDepthSurfaceView
     */
    public void setPreview(SurfaceView mColorSurfaceView, SurfaceView mDepthSurfaceView){
        this.colorPreview = mColorSurfaceView;
        this.depthPreview = mDepthSurfaceView;
        this.streamInfos = mVision.getActivatedStreamInfo();
        for (StreamInfo info : streamInfos) {
            // Adjust image ratio for display
            float ratio = (float) info.getWidth() / info.getHeight();
            ViewGroup.LayoutParams layout;
            switch (info.getStreamType()) {
                case StreamType.COLOR:
                    // Adjust color surface view
                    mColorSurfaceView.getHolder().setFixedSize(info.getWidth(), info.getHeight());
                    layout = mColorSurfaceView.getLayoutParams();
                    layout.width = (int) (mColorSurfaceView.getHeight() * ratio);
                    mColorSurfaceView.setLayoutParams(layout);
                    break;

                case StreamType.DEPTH:
                    // Adjust depth surface view
                    mDepthSurfaceView.getHolder().setFixedSize(info.getWidth(), info.getHeight());
                    layout = mDepthSurfaceView.getLayoutParams();
                    layout.width = (int) (mDepthSurfaceView.getHeight() * ratio);
                    mDepthSurfaceView.setLayoutParams(layout);

                    break;
            }
        }

        startPreview();
    }

    /* -------------------------------------- Preview End --------------------------------------- */




    /* ====================== Fields and methods for Image filter =================================== *
    *
    *   There are objects to operate image:
    *       1. ImageIO io: it is the object to save image to the memory
    *
    *       2. FrameFilter filter: this is used to process depth image
    *                               It will send result to io for storage
    *
    *       3. senseHandler: Use to get following sign from ImageIO:
    *                       1. Save complete
    *
    * ------------------------------------------------------------------------------------------------ */
    private ImageIO io;
    private FrameFilter filter;

    public void setFilterHandler(Handler filterHandler){
        filter.setPreviewHandler(filterHandler);
    }

    /**
     * Called in constructor for initialization
     */
    private void initImageOperator(){
        io = new ImageIO();
        io.setSenseHandler(senseHandler);

        filter = new FrameFilter(ImageInfo.DEPTH_WIDTH, ImageInfo.DEPTH_HEIGHT, Bitmap.Config.RGB_565, 20);
        filter.setIOHandler(io.ioHandler);
        filter.start();
    }

    private Handler senseHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ImageInfo.SAVE_COMPLETE:{
                    filter.restart();
                    setUpCaptureListener();
                }break;
            }
        }
    };
    /*-------------------------------------------------------------------------------*/




    /* ==============================  Capture Start ================================= *

     * The run() method will start listener once every 2 seconds and then shutdown
     * In listener the frame image will be captured and save to disk

     * =============================================================================== */
    StreamInfo colorInfo;
    StreamInfo depthInfo;

    public void startCapture(){
        killThread = false;
        this.streamInfos = mVision.getActivatedStreamInfo();
        startPreview();
        setUpCaptureListener();
    }

    /**
     * Listener used to capture the key frame, include following info:
     *      1. rgb    2. depth   3. position
     */
    Vision.FrameListener keyFramelistener = new Vision.FrameListener() {
        @Override
        public void onNewFrame(int streamType, Frame frame) {
            // Omit initial several frame
            frameCounter++;
            if(frameCounter < OMIT_FRAME){return;}

            io.addPos(extrinsic.getPos());
            // put image to save buffer
            switch (streamType) {
                case StreamType.COLOR:
                    io.addColorFrame(frame);
                    mVision.stopListenFrame(StreamType.COLOR);
                break;

                case StreamType.DEPTH:
                    io.addDepthFrame(frame);
                    mVision.stopListenFrame(StreamType.DEPTH);
                    mVision.startListenFrame(StreamType.DEPTH, trivialFrameListener);
                break;
            }
        }
    };

    Vision.FrameListener trivialFrameListener = new Vision.FrameListener() {
        @Override
        public void onNewFrame(int streamType, Frame frame) {
            if(streamType == StreamType.DEPTH){
                filter.addFrame(frame);
            }
        }
    };

    private void setUpCaptureListener(){
        mVision.stopListenFrame(StreamType.DEPTH);
        mVision.stopListenFrame(StreamType.COLOR);

        for (StreamInfo info : streamInfos) {
            switch (info.getStreamType()) {
                case StreamType.COLOR:
                    colorInfo = info;
                    mVision.startListenFrame(StreamType.COLOR, keyFramelistener);
                    break;
                case StreamType.DEPTH:
                    depthInfo = info;
                    mVision.startListenFrame(StreamType.DEPTH, keyFramelistener);
                    break;
            }
        }
    }
    /*--------------------------------------------------------------------------------*/

    public synchronized void suspend() {
        stopPreview();
    }

    /* ==============================  Capture Single ================================= *

     * Use captureBitmap() to capture a single image
     * In listener the frame image will be captured and save to a cache

     * =============================================================================== */
    public void startSimplecapture(){
        killThread = false;
        this.streamInfos = mVision.getActivatedStreamInfo();

        mVision.stopListenFrame(StreamType.COLOR);
        mVision.startListenFrame(StreamType.COLOR, simpleCaptureListener);
    }


    /**
     * A method to get a single capture of camera
     * @return
     */
    public Bitmap captureSingleBitmap(){
        if(bitmapCache != null){
            return bitmapCache;
        }

        return null;
    }

    private Bitmap bitmapCache;

    private Vision.FrameListener simpleCaptureListener = new Vision.FrameListener() {
        @Override
        public void onNewFrame(int streamType, Frame frame) {
            if(streamType == StreamType.COLOR){
                bitmapCache = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
                bitmapCache.copyPixelsFromBuffer(frame.getByteBuffer());

            }

        }
    };
}
