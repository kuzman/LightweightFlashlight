package com.daresoft.lightweightflashlight;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ToggleButton;

import java.util.List;


public class MainActivity extends Activity implements SurfaceHolder.Callback{
    //private Flash flash = new Flash();
    SurfaceView preview;
    SurfaceHolder mHolder;
    public Camera camera = null;
    private Camera.Parameters cameraParameters;
    List<String> flashModes;
    private String previousFlashMode = null;
    String manuName = android.os.Build.MANUFACTURER.toLowerCase();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preview = (SurfaceView) findViewById(R.id.PREVIEW);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {}

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        try {
            camera.setPreviewDisplay(mHolder);
        }
        catch (Exception e)
        {}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
        }
        mHolder = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        open();
        ToggleButton the_button = (ToggleButton) findViewById(R.id.flashlightButton);
        if (the_button.isChecked()) {
            on();
            the_button.setKeepScreenOn(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        close();
    }

    public void onToggleClicked(View v) {
        if (((ToggleButton) v).isChecked()) {
            on();
            v.setKeepScreenOn(true);
        } else {
            off();
            v.setKeepScreenOn(false);
        }
    }

    public synchronized void open() {
        if (!manuName.contains("motorola")) {
            camera = Camera.open();
            if (camera != null) {
                cameraParameters = camera.getParameters();
                flashModes = cameraParameters.getSupportedFlashModes();
                if (flashModes == null)
                    return;
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH))
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                else
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                previousFlashMode = cameraParameters.getFlashMode();
            }
            if (previousFlashMode == null) {
                // could be null if no flash, i.e. emulator
                previousFlashMode = Camera.Parameters.FLASH_MODE_OFF;
            }
        }
    }

    public synchronized void close() {
        if (camera != null) {
            cameraParameters.setFlashMode(previousFlashMode);
            camera.setParameters(cameraParameters);
            camera.release();
            camera = null;
        }
    }

    public synchronized void on() {
        if (manuName.contains("motorola")) {
            Flash led;
            try {
                led = new Flash();
                led.enable(true);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            if (camera == null)
                camera = Camera.open();
            if (camera != null) {
                flashModes = cameraParameters.getSupportedFlashModes();
                if (flashModes == null)
                    return;
                if (count == 0) {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(cameraParameters);
                    camera.startPreview();
                }
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH))
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                else
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                camera.setParameters(cameraParameters);
//                camera.startPreview();
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        count = 1;
                    }
                });
            }
        }
    }

    public synchronized void off() {
        if (manuName.contains("motorola")) {
            Flash led;
            try {
                led = new Flash();
                led.enable(false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (camera != null) {
                count = 0;
                camera.stopPreview();
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(cameraParameters);
            }
        }
    }}
