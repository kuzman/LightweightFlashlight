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
    private Flash flash = new Flash();
    SurfaceView preview;
    SurfaceHolder mHolder;
    public Camera camera = null;
    private Camera.Parameters cameraParameters;
    List<String> flashModes;
    private String previousFlashMode = null;

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
        String manuName = android.os.Build.MANUFACTURER.toLowerCase();
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

    public synchronized void close() {
        if (camera != null) {
            cameraParameters.setFlashMode(previousFlashMode);
            camera.setParameters(cameraParameters);
            camera.release();
            camera = null;
        }
    }

    public synchronized void on() {
        if (camera != null) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH))
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            else
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            camera.setParameters(cameraParameters);
            camera.startPreview();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                }
            });
        }
    }

    public synchronized void off() {
        if (camera != null) {
            camera.stopPreview();
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(cameraParameters);
        }
    }}
