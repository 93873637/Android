package com.liz.androidutils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * StorageUtils:
 * add by liz 2020/04/16
 */
public class StorageUtils {

    /**
     * Since its hidden, we copy from system/vold/binder/android/os/IVold.aidl
     */
    public static final int VOLUME_TYPE_PUBLIC = 0;
    public static final int VOLUME_TYPE_PRIVATE = 1;
    public static final int VOLUME_TYPE_EMULATED = 2;
    public static final int VOLUME_TYPE_ASEC = 3;
    public static final int VOLUME_TYPE_OBB = 4;

    @TargetApi(Build.VERSION_CODES.M)
    public static List<Object> getVolumeInfoList(Context context) {
        StorageManager storageManager = context.getSystemService(StorageManager.class);
        if (storageManager == null) {
            LogUtils.te2("storage manager null");
            return null;
        }
        try {
            Method method_getVolumes = storageManager.getClass().getMethod("getVolumes");
            return (List<Object>) method_getVolumes.invoke(storageManager);
        } catch (Exception e) {
            LogUtils.te2("get volume list failed with ex = " + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * getVolumeList: get devices volume info list by reflect
     *
     * @param context:
     * @return string list of volume info
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static List<String> getVolumeList(Context context) {
        StorageManager storageManager = context.getSystemService(StorageManager.class);
        if (storageManager == null) {
            LogUtils.te2("storage manager null");
            return null;
        }

        try {
            Method method_getVolumes = storageManager.getClass().getMethod("getVolumes");
            List<Object> volumeInfoList = (List<Object>) method_getVolumes.invoke(storageManager);
            if (volumeInfoList == null) {
                LogUtils.te2("volume info list null");
                return null;
            }

            Class<?> clsVolumeInfo = Class.forName("android.os.storage.VolumeInfo");
            Method method_getType = clsVolumeInfo.getMethod("getType");
            Method method_isMountedReadable = clsVolumeInfo.getMethod("isMountedReadable");
            Method method_getPath = clsVolumeInfo.getMethod("getPath");

            ArrayList<String> volumeList = new ArrayList<>();
            for (int i = 0; i < volumeInfoList.size(); i++) {
                int type = (Integer) method_getType.invoke(volumeInfoList.get(i));
                boolean isMountedReadable = (Boolean) method_isMountedReadable.invoke(volumeInfoList.get(i));
                File f = (File) method_getPath.invoke(volumeInfoList.get(i));
                String info = "# " + (i + 1)
                        + ": type=" + type
                        + ", readable=" + isMountedReadable
                        + ", path=" + ((f == null) ? "" : f.getAbsolutePath());
                volumeList.add(info);
            }

            return volumeList;
        } catch (Exception e) {
            LogUtils.te2("get volume list failed with ex = " + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * getSdCardPath: get first mounted and readable storage volume by reflect
     *
     * @param context:
     * @return path of volume
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static String getSdCardPath(Context context) {
        StorageManager storageManager = context.getSystemService(StorageManager.class);
        if (storageManager == null) {
            LogUtils.te2("storage manager null");
            return "";
        }

        try {
            Class<?> clsVolumeInfo = Class.forName("android.os.storage.VolumeInfo");
            Method method_getVolumes = storageManager.getClass().getMethod("getVolumes");
            Method method_getType = clsVolumeInfo.getMethod("getType");
            Method method_isMountedReadable = clsVolumeInfo.getMethod("isMountedReadable");
            Method method_getPath = clsVolumeInfo.getMethod("getPath");

            List<Object> volumeInfoList = (List<Object>) method_getVolumes.invoke(storageManager);
            if (volumeInfoList == null) {
                LogUtils.te2("volume info list null");
                return "";
            }

            //LogUtils.d("get volume info list, size = "+ volumeInfoList.size());
            for (int i = 0; i < volumeInfoList.size(); i++) {
                int type = (Integer) method_getType.invoke(volumeInfoList.get(i));
                //LogUtils.d("volume type = " + type);
                if (type == VOLUME_TYPE_PUBLIC) {
                    boolean isMountedReadable = (Boolean) method_isMountedReadable.invoke(volumeInfoList.get(i));
                    //LogUtils.d("isMountedReadable = " + isMountedReadable);
                    if (isMountedReadable) {
                        File file = (File) method_getPath.invoke(volumeInfoList.get(i));
                        if (file != null) {
                            String path = file.getAbsolutePath();
                            //LogUtils.d("path = " + path);
                            return path;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.te2("get sd card path failed, e = " + e.toString());
            e.printStackTrace();
        }
        return "";
    }

    /**
     * getUsbDiskPathList: get usb disk path list by reflect
     *
     * @param context:
     * @return usb disk path list
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static List<String> getUsbDiskPathList(Context context) {
        StorageManager storageManager = context.getSystemService(StorageManager.class);
        if (storageManager == null) {
            LogUtils.te2("storage manager null");
            return null;
        }

        try {
            Method method_getVolumes = storageManager.getClass().getMethod("getVolumes");
            List<Object> volumeInfoList = (List<Object>) method_getVolumes.invoke(storageManager);
            if (volumeInfoList == null) {
                LogUtils.te2("volume info list null");
                return null;
            }

            Class<?> clsVolumeInfo = Class.forName("android.os.storage.VolumeInfo");
            Method method_getType = clsVolumeInfo.getMethod("getType");
            Method method_isMountedReadable = clsVolumeInfo.getMethod("isMountedReadable");
            Method method_getPath = clsVolumeInfo.getMethod("getPath");

            ArrayList<String> pathList = new ArrayList<>();
            for (int i = 0; i < volumeInfoList.size(); i++) {
                int type = (Integer) method_getType.invoke(volumeInfoList.get(i));
                if (type == VOLUME_TYPE_PUBLIC) {
                    File f = (File) method_getPath.invoke(volumeInfoList.get(i));
                    if (f != null) {
                        pathList.add(f.getAbsolutePath());
                    }
                }
            }

            return pathList;
        } catch (Exception e) {
            LogUtils.te2("get volume list failed with ex = " + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isMountSD(StorageManager storageManager) {

        //####@:
//        final List<VolumeInfo> volumes = storageManager.getVolumes();
//        //Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//
//        for (VolumeInfo vol : volumes) {
//            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
//                DiskInfo disk = vol.getDisk();
//                if (disk != null && disk.isSd()) {
//                    // sdcard dir
//                    int status = vol.getState();
//                    JLog.d("isMountSD()--status-->" + status);
//                    if (status == VolumeInfo.STATE_MOUNTED) {
//                        return true;
//                    }
//                    //continue;
//
//                }
//            }
//        }
        return false;
    }

    public static String getSDcardDir(StorageManager storageManager) {
        //####@:
//
//        final List<VolumeInfo> volumes = storageManager.getVolumes();
//        //Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//        for (VolumeInfo vol : volumes) {
//            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
//                DiskInfo disk = vol.getDisk();
//                if (disk != null) {
//                    if (disk.isSd()) {
//                        // usb dir
////                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(),
////                                false);
//                        mSDcardDir = vol.path;
//                        JLog.d("hjc getSDcardDir()--mSDcardDir-->" + mSDcardDir);
//                    }
//                }
//            }
//        }
//
//		if (null != mSDcardDir) {
//            /*int end = mSDcardDir.lastIndexOf('/');
//            if (end > 0)    // case mSDcardDir = /xxx/xxx
//                return mSDcardDir.substring(0, end);
//            else            // case mSDcardDir = /xxx*/
//                return mSDcardDir;
//			//return mSDcardDir;
//        } else {
//            return null;
//        }
        return null;
    }

    //###@:
//    /**
//     * 获取所有SD卡路径
//     * @param storageManager
//     * @return
//     */
//    public static List<String> getSdCardPaths(StorageManager storageManager){
//    	List<String> sdPaths = new ArrayList<String>();
//        final List<VolumeInfo> volumes = storageManager.getVolumes();
//        //Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//        for (VolumeInfo vol : volumes) {
//            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
//                DiskInfo disk = vol.getDisk();
//                if (disk != null) {
//                    if (disk.isSd()) {
//                        sdPaths.add(vol.path);
//                    }
//                }
//            }
//        }
//
//		return sdPaths;
//
//    }

    public static boolean isMountUSB(StorageManager storageManager) {

        //###@:
//        final List<VolumeInfo> volumes = storageManager.getVolumes();
//        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//
//        for (VolumeInfo vol : volumes) {
//            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
//                DiskInfo disk = vol.getDisk();
//                if (disk != null) {
//                    if (disk.isUsb()) {
//                        // usb dir
//                        int status = vol.getState();
//                        JLog.d("isMountUSB()--status-->" + status);
//                        if (status == VolumeInfo.STATE_MOUNTED) {
//                            return true;
//                        }
//                        return false;
//                    }
//                }
//            }
//        }
        return false;
    }

    public static String getUsbDir(StorageManager storageManager) {
        return null;
//
//        final List<VolumeInfo> volumes = storageManager.getVolumes();
//        //Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
//        for (VolumeInfo vol : volumes) {
//            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
//                Log.d("YHX", "VolumeInfo.TYPE_PUBLIC");
//                Log.d("YHX", "Volume path:" + vol.getPath());
//                DiskInfo disk = vol.getDisk();
//                if (disk != null) {
//                    if (disk.isUsb()) {
//                        // usb dir
////                        StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(),
////                                false);
//                        mUsbDirs = vol.path;
//                    }
//                }
//            }
//        }
//
//        if (null != mUsbDirs) {
//          int end = mUsbDirs.lastIndexOf('/');
//            if (end > 0)    // case mUsbDirs = /xxx/xxx
//                return mUsbDirs.substring(0, end);
//            else            // case mUsbDirs = /xxx
//                return mUsbDirs;
//        	//return mUsbDirs;
//        } else {
//            return null;
//        }
    }

    public static String getFlashDir() {
        //###@:
//        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
//            JLog.d("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
//            return ((File)invokeStaticMethod("android.os.Environment","getFlashStorageDirectory",null)).getPath();
//        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
//            JLog.d("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
//            return ((File)invokeStaticMethod("android.os.Environment","getExternalStorageDirectory",null)).getPath();
//        }
        return null;
    }

    public static String getFlashState() {
        //####@:
//        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
//            JLog.d("%%%%%%%%%%-------- using at gingerbread2.3.1 ----------!!!");
//            return (String)invokeStaticMethod("android.os.Environment","getFlashStorageState",null);
//        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
//            JLog.d("%%%%%%%%%%-------- using at ics4.0.3 ----------!!!");
//            return (String)invokeStaticMethod("android.os.Environment","getExternalStorageState",null);
//        }
        return Environment.MEDIA_REMOVED;
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... arguments) {
        try {
            Class<?>[] parameterTypes = null;
            if (arguments != null) {
                parameterTypes = new Class<?>[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    parameterTypes[i] = arguments[i].getClass();
                }
            }
            Method method = cls.getMethod(methodName, parameterTypes);
            return method.invoke(null, arguments);
        } catch (Exception ex) {
            JLog.d("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Object... arguments) {
        try {
            Class<?> cls = Class.forName(className);
            return invokeStaticMethod(cls, methodName, arguments);
        } catch (Exception ex) {
            JLog.d("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

    public static Object invokeStaticMethod(String className, String methodName, Class<?>[] types, Object... arguments) {
        try {
            Class<?> cls = Class.forName(className);
            return invokeStaticMethod(cls, methodName, types, arguments);
        } catch (Exception ex) {
            JLog.d("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Class<?>[] types, Object... arguments) {
        try {
            Method method = cls.getMethod(methodName, types);
            return method.invoke(null, arguments);
        } catch (Exception ex) {
            JLog.d("Invoke method error. " + ex.getMessage());
            //handleReflectionException(ex);
            return null;
        }
    }
}
