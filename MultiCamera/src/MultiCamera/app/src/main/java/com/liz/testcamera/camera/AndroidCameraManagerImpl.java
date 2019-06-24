/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liz.testcamera.camera;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.liz.testcamera.utils.LogUtils;

import java.lang.reflect.Method;

/**
 * A class to implement {@link CameraManager} of the Android camera framework.
 */
class AndroidCameraManagerImpl implements CameraManager {

    /* Messages used in CameraHandler. */
    // Camera initialization/finalization
    private static final int OPEN_CAMERA = 1;
    private static final int RELEASE =     2;
    private static final int RECONNECT =   3;
    private static final int UNLOCK =      4;
    private static final int LOCK =        5;

    //HAL1 version code
    private static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;

    private CameraHandler mCameraHandler;
    private Camera mCamera;

    // Used to retain a copy of Parameters for setting parameters.
    private Camera.Parameters mParamsToSet;

    AndroidCameraManagerImpl() {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
    }

    @Override
    public CameraProxy cameraOpen(Handler handler, int cameraId, CameraOpenErrorCallback callback) {
        mCameraHandler.obtainMessage(OPEN_CAMERA, cameraId, 0, null).sendToTarget();
        mCameraHandler.waitDone();
        if (mCamera != null) {
            return new AndroidCameraProxyImpl();
        } else {
            return null;
        }
    }

    /**
     * A helper class to forward ShutterCallback to to another thread.
     */
    private static class ShutterCallbackForward implements ShutterCallback {
        private final Handler mHandler;
        private final CameraShutterCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link ShutterCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link ShutterCallbackForward},
         *                or null if any parameter is null.
         */
        public static ShutterCallbackForward getNewInstance(
                Handler handler, CameraProxy camera, CameraShutterCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new ShutterCallbackForward(handler, camera, cb);
        }

        private ShutterCallbackForward(
                Handler h, CameraProxy camera, CameraShutterCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onShutter() {
            final android.hardware.Camera currentCamera = mCamera.getCamera();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if ((currentCamera != null) && currentCamera.equals(mCamera.getCamera())) {
                        mCallback.onShutter(mCamera);
                    }
                }
            });
        }
    }

    /**
     * A helper class to forward PictureCallback to another thread.
     */
    private static class PictureCallbackForward implements PictureCallback {
        private final Handler mHandler;
        private final CameraPictureCallback mCallback;
        private final CameraProxy mCamera;

        /**
         * Returns a new instance of {@link PictureCallbackForward}.
         *
         * @param handler The handler in which the callback will be invoked in.
         * @param camera  The {@link CameraProxy} which the callback is from.
         * @param cb      The callback to be invoked.
         * @return        The instance of the {@link PictureCallbackForward},
         *                or null if any parameters is null.
         */
        public static PictureCallbackForward getNewInstance(
                Handler handler, CameraProxy camera, CameraPictureCallback cb) {
            if (handler == null || camera == null || cb == null) return null;
            return new PictureCallbackForward(handler, camera, cb);
        }

        private PictureCallbackForward(
                Handler h, CameraProxy camera, CameraPictureCallback cb) {
            mHandler = h;
            mCamera = camera;
            mCallback = cb;
        }

        @Override
        public void onPictureTaken(
                final byte[] data, android.hardware.Camera camera) {
            final android.hardware.Camera currentCamera = mCamera.getCamera();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (currentCamera != null && currentCamera.equals(mCamera.getCamera())) {
                        mCallback.onPictureTaken(data, mCamera);
                    }
                }
            });
        }
    }

    /**
     * A class which implements {@link CameraManager.CameraProxy} and
     * camera handler thread.
     * TODO: Save the handler for the callback here to avoid passing the same
     * handler multiple times.
     */
    public class AndroidCameraProxyImpl implements CameraManager.CameraProxy {

        private AndroidCameraProxyImpl() {
            Assert(mCamera != null);
        }

        @Override
        public android.hardware.Camera getCamera() {
            return mCamera;
        }

        @Override
        public void takePicture(
                Handler handler,
                CameraShutterCallback shutter,
                CameraPictureCallback raw,
                CameraPictureCallback post,
                CameraPictureCallback jpeg) {
            mCameraHandler.requestTakePicture(
                    ShutterCallbackForward.getNewInstance(handler, this, shutter),
                    PictureCallbackForward.getNewInstance(handler, this, raw),
                    PictureCallbackForward.getNewInstance(handler, this, post),
                    PictureCallbackForward.getNewInstance(handler, this, jpeg));
        }
    }

    public static void Assert(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    private class CameraHandler extends Handler {
        CameraHandler(Looper looper) {
            super(looper);
        }

        public void requestTakePicture(
                final ShutterCallback shutter,
                final PictureCallback raw,
                final PictureCallback postView,
                final PictureCallback jpeg) {
            if (mCamera == null) {
                LogUtils.e("AndroidCameraManagerImpl.CameraHandler.takePicture: no camera device to take picture...");
                return;
            }

            post(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtils.d("AndroidCameraManagerImpl.CameraHandler.takePicture: run...");
                        mCamera.takePicture(shutter, raw, postView, jpeg);
                    } catch (RuntimeException e) {
                        // TODO: output camera state and focus state for debugging.
                        LogUtils.e("ERROR: take picture failed with Exception: " + e.toString());
                        //throw e;  //not throw out keep app running
                    }
                }
            });
        }

        /**
         * This method does not deal with the API level check.  Everyone should
         * check first for supported operations before sending message to this handler.
         */
        @Override
        public void handleMessage(final Message msg) {
            try {
                switch (msg.what) {
                    case OPEN_CAMERA:
                        LogUtils.d("AndroidCameraManagerImpl.CameraHandler.handleMessage: OPEN_CAMERA");
                        try {
                            Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                                    "openLegacy", int.class, int.class);
                            mCamera = (android.hardware.Camera) openMethod.invoke(
                                    null, msg.arg1, CAMERA_HAL_API_VERSION_1_0);
                        } catch (Exception e) {
                            /* Retry with open if openLegacy doesn't exist/fails */
                            LogUtils.w("WARNING: openLegacy failed due to " + e.getMessage() + ", using open instead");
                            mCamera = android.hardware.Camera.open(msg.arg1);
                        }

                        if (mCamera != null) {
                            //mParametersIsDirty = true;

                            // Get a instance of Camera.Parameters for later use.
                            if (mParamsToSet == null) {
                                mParamsToSet = mCamera.getParameters();
                            }
                        } else {
                            LogUtils.e("ERROR: open camera failed.");
                            //if (msg.obj != null) {
                            //((CameraOpenErrorCallback) msg.obj).onDeviceOpenFailure(msg.arg1);
                            //}
                        }
                        return;

                    default:
                        LogUtils.d("AndroidCameraManagerImpl.CameraHandler.handleMessage: unhandled msg " + msg.what);
                        break;
                }
            }
            catch (RuntimeException e) {
                LogUtils.e("ERROR: AndroidCameraManagerImpl.CameraHandler.handleMessage: exception=" + e.toString());
            }
        }

        /**
         * Waits for all the {@code Message} and {@code Runnable} currently in the queue
         * are processed.
         *
         * @return {@code false} if the wait was interrupted, {@code true} otherwise.
         */
        public boolean waitDone() {
            final Object waitDoneLock = new Object();
            final Runnable unlockRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (waitDoneLock) {
                        waitDoneLock.notifyAll();
                    }
                }
            };

            synchronized (waitDoneLock) {
                mCameraHandler.post(unlockRunnable);
                try {
                    waitDoneLock.wait();
                } catch (InterruptedException ex) {
                    LogUtils.e("ERROR: AndroidCameraManagerImpl.CameraHandler.waitDone: exception=" + ex.toString());
                    return false;
                }
            }
            return true;
        }

    }  //class CameraHandler
}
