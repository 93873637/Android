/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.liz.androidutils;


import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UsbUtils {

    public static void showList(Context context) {
        List<UsbDevice> list = UsbUtils.getUsbDeviceList(context);
        if (list == null) {
            LogUtils.td("list null");
        } else {
            LogUtils.td("get list, size = " + list.size());
            LogUtils.d("-----------------------------------------------");
            for (int i=0; i<list.size(); i++) {
                LogUtils.d("#" + (i+1));
                LogUtils.d(list.get(i).toString());
            }
            LogUtils.d("-----------------------------------------------");
        }
    }

    /**
     * getUsbDeviceList: get all usb device list, without filter
     *
     * @return usb device list
     */
    public static List<UsbDevice> getUsbDeviceList(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            LogUtils.te2("usb manager null");
            return null;
        }
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (deviceList == null) {
            LogUtils.td("no device list");
            return null;
        }
        return new ArrayList<>(deviceList.values());
    }

    /**
     * getUsbDevices: get all usb devices, without filter
     *
     * @return list iterator
     */
    public static Iterator<UsbDevice> getUsbDevices(Context context) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            LogUtils.te2("usb manager null");
            return null;
        }
        Iterator<UsbDevice> iterator = null;
        final HashMap<String, UsbDevice> list = usbManager.getDeviceList();
        if (list != null) {
            iterator = list.values().iterator();
        }
        return iterator;
    }

    /**
     * getUsbDeviceList: get device list by filter
     *
     * @param filters:
     * @return device list by filter
     *   empty list if no device matched
     *   all devices if filter null or empty
     */
    public List<UsbDevice> getUsbDeviceList(Context context, final List<DeviceFilter> filters) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            LogUtils.te2("usb manager null");
            return null;
        }
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (deviceList == null) {
            LogUtils.td("no device list");
            return null;
        }
        final List<UsbDevice> result = new ArrayList<>();
        if ((filters == null) || filters.isEmpty()) {
            result.addAll(deviceList.values());
        } else {
            for (final UsbDevice device : deviceList.values()) {
                for (final DeviceFilter filter : filters) {
                    if ((filter != null) && filter.matches(device)) {
                        // when filter matches
                        if (!filter.isExclude) {
                            result.add(device);
                        }
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * An example for usb camera:
     * UsbDevice[
     *   mName=/dev/bus/usb/003/010,
     *   mVendorId=1133,
     *   mProductId=2085,
     *   mClass=239,
     *   mSubclass=2,
     *   mProtocol=1,
     *   mManufacturerName=null,
     *   mProductName=null,
     *   mVersion=0.12,
     *   mSerialNumber=791FE4D0,
     *   mConfigurations=[
     *     UsbConfiguration[mId=1,mName=null,mAttributes=128,mMaxPower=250,mInterfaces=[
     *     UsbInterface[mId=0,mAlternateSetting=0,mName=null,mClass=14,mSubclass=1,mProtocol=0,mEndpoints=[
     *     UsbEndpoint[mAddress=135,mAttributes=3,mMaxPacketSize=16,mInterval=8]]
     *     UsbInterface[mId=1,mAlternateSetting=0,mName=null,mClass=14,mSubclass=2,mProtocol=0,mEndpoints=[]
     *     UsbInterface[mId=1,mAlternateSetting=1,mName=null,mClass=14,mSubclass=2,mProtocol=0,mEndpoints=[
     *     UsbEndpoint[mAddress=129,mAttributes=5,mMaxPacketSize=192,mInterval=1]]
     *     UsbInterface[mId=1,mAlternateSetting=2,mName=null,mClass=14,mSubclass=2,mProtocol=0,mEndpoints=[
     *     UsbEndpoint[mAddress=129,mAttributes=5,mMaxPacketSize=384,mInterval=1]]
     *     ...
     *     UsbEndpoint[mAddress=134,mAttributes=5,mMaxPacketSize=196,mInterval=4]]
     *   ]
     * ]
     *
     * An example for usb mouse:
     * UsbDevice[
     *   mName=/dev/bus/usb/003/009,
     *   mVendorId=1226,
     *   mProductId=97,
     *   mClass=0,
     *   mSubclass=0,
     *   mProtocol=0,
     *   mManufacturerName=PixArt,
     *   mProductName=USB Optical Mouse,
     *   mVersion=1.00,
     *   mSerialNumber=null,
     *   mConfigurations=[
     *     UsbConfiguration[mId=1,mName=null,mAttributes=160,mMaxPower=50,mInterfaces=[
     *     UsbInterface[mId=0,mAlternateSetting=0,mName=null,mClass=3,mSubclass=1,mProtocol=2,mEndpoints=[
     *     UsbEndpoint[mAddress=129,mAttributes=3,mMaxPacketSize=4,mInterval=10]]
     *   ]
     * ]
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getUsbDeviceInfo(UsbDevice device) {
        return device.getDeviceName()
                + "|" + device.getVendorId() + ":" + device.getProductId()
                + "|" + device.getProductName()
                + "|" + device.getManufacturerName()
                + "|" + device.getDeviceClass() + "/" + device.getDeviceSubclass()
                ;
    }
}
