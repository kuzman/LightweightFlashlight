package com.daresoft.lightweightflashlight;
import android.hardware.Camera;
import android.os.IBinder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by darko on 12.12.2014.
 */
public class Flash {
    private Object svc = null;
    private Method getFlashlightEnabled = null;
    private Method setFlashlightEnabled = null;

    @SuppressWarnings("unchecked")
    public Flash() throws Exception {
        try {
            // call ServiceManager.getService("hardware") to get an IBinder for the service.
            // this appears to be totally undocumented and not exposed in the SDK whatsoever.
            Class sm = Class.forName("android.os.ServiceManager");
            Object hwBinder = sm.getMethod("getService", String.class).invoke(null, "hardware");

            // get the hardware service stub. this seems to just get us one step closer to the proxy
            Class hwsstub = Class.forName("android.os.IHardwareService$Stub");
            Method asInterface = hwsstub.getMethod("asInterface", android.os.IBinder.class);
            svc = asInterface.invoke(null, (IBinder) hwBinder);

            // grab the class (android.os.IHardwareService$Stub$Proxy) so we can reflect on its methods
            Class proxy = svc.getClass();

            // save methods
            getFlashlightEnabled = proxy.getMethod("getFlashlightEnabled");
            setFlashlightEnabled = proxy.getMethod("setFlashlightEnabled", boolean.class);
        }
        catch(Exception e) {
            throw new Exception("LED could not be initialized");
        }
    }

    public boolean isEnabled() {
        try {
            return getFlashlightEnabled.invoke(svc).equals(true);
        }
        catch(Exception e) {
            return false;
        }
    }

    public void enable(boolean tf) {
        try {
            setFlashlightEnabled.invoke(svc, tf);
        }
        catch(Exception e) {}
    }

}
