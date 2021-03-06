package com.daresoft.lightweightflashlight;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
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
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
        {
            Log.e("PreviewDemo-surfaceCallback",
                    "Exception in setPreviewDisplay()", e);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
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
            if (!hasFlash()) {
                Toast.makeText(getApplicationContext(), "LED Not Available", Toast.LENGTH_LONG).show();
                return;
            }
            on();
            v.setKeepScreenOn(true);
        } else {
            off();
            close();
            v.setKeepScreenOn(false);
        }
    }

    public synchronized void open() {
        if (!manuName.contains("motorola")) {
            camera = Camera.open();
            if (camera != null) {
                cameraParameters = camera.getParameters();
                flashModes = cameraParameters.getSupportedFlashModes();
                if (flashModes == null) {
                    Toast.makeText(getApplicationContext(), "LED Not Available", Toast.LENGTH_LONG).show();
                    return;
                }
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
                open();
            if (camera != null) {
                flashModes = cameraParameters.getSupportedFlashModes();
                if (flashModes == null) {
                    Toast.makeText(getApplicationContext(), "LED Not Available",Toast.LENGTH_LONG).show();
                    return;
                }
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
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        count = 1;
                    }
                });
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(cameraParameters);
                }
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    camera.stopPreview();
                }
                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(cameraParameters);
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    camera.stopPreview();
                }

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                    camera.release();
//                    camera = null;
                }
            }
        }
    }
    public boolean hasFlash() {
        if (camera == null) {
           open();
        }

        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }
}
