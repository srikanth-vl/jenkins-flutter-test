package com.vassar.unifiedapp.newcamera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/** A basic Camera preview class */
public class CameraPreview
        extends SurfaceView
        implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getName();
    private SurfaceHolder mHolder;
    private static Camera mCamera;
    private Context mContext;

    public CameraPreview(Context context) {
        super(context);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public  Camera getCameraInitialized() {
        return mCamera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.

        try {
            // Create an instance of Camera
            //mCamera = getCameraInstance();
            newOpenCamera();
            Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            Camera.Parameters params = mCamera.getParameters();
            if (display != null) {
                if (display.getRotation() == Surface.ROTATION_0) {
                    mCamera.setDisplayOrientation(90);
                    params.setRotation(90);
                }

                if (display.getRotation() == Surface.ROTATION_270) {
                    mCamera.setDisplayOrientation(180);
                    params.setRotation(180);
                }
            }

            params.setJpegQuality(100);
            params.setPictureFormat(ImageFormat.JPEG);

            int w = 0, h = 0;
            for (Camera.Size size : params.getSupportedPictureSizes()) {
                if (size.width > w || size.height > h) {
                    w = size.width;
                    h = size.height;
                }

            }

            params.setPictureSize(w, h);
            mCamera.setParameters(params);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            mCamera.release();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** A safe way to get an instance of the Camera object. */
    private static void oldOpenCamera(){
        //Camera c = null;
        try {
          mCamera = Camera.open(); // attempt to get a Camera instance
        }
        catch (RuntimeException  e){
            // Camera is not available (in use or does not exist)
            Log.e("CAMERATEST1", "camera not available -- camera.open --");
            e.printStackTrace();
        }
        //return c; // returns null if camera is unavailable
    }

    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }

    }

    private CameraHandlerThread mThread = null;

    private static class CameraHandlerThread
            extends HandlerThread {

        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldOpenCamera();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            }
            catch (InterruptedException e) {
                Log.w("CAMERA", "wait was interrupted");
            }
        }
    }
}
