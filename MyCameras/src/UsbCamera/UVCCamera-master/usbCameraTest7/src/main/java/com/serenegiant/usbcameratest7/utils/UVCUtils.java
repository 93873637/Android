package com.serenegiant.usbcameratest7.utils;

import android.app.Activity;
import android.hardware.usb.UsbDevice;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameratest7.R;

import java.util.List;

public class UVCUtils {

    public static List<UsbDevice> getUsbDeviceList(Activity activity, USBMonitor usbMonitor) {
        List<DeviceFilter> deviceFilters = DeviceFilter
                .getDeviceFilters(activity.getApplicationContext(), R.xml.device_filter);
        if (usbMonitor == null || deviceFilters == null) {
            return null;
        }
        return usbMonitor.getDeviceList(deviceFilters.get(0));
    }

    public static int getUsbDeviceCount(Activity activity, USBMonitor usbMonitor) {
        List<UsbDevice> devList = getUsbDeviceList(activity, usbMonitor);
        if (devList == null) {
            return 0;
        }
        return devList.size();
    }
}
