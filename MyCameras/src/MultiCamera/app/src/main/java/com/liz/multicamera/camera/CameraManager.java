/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.liz.multicamera.camera;

import android.hardware.Camera;
import android.os.Handler;

/**
 * An interface which provides possible camera device operations.
 *
 * The client should call {@code CameraManager.cameraOpen} to get an instance
 * of {@link CameraManager.CameraProxy} to control the camera. Classes
 * implementing this interface should have its own one unique {@code Thread}
 * other than the main thread for camera operations. Camera device callbacks
 * are wrapped since the client should not deal with
 * {@code android.hardware.Camera} directly.
 *
 * TODO: provide callback interfaces for:
 * {@code android.hardware.Camera.ErrorCallback},
 * {@code android.hardware.Camera.OnZoomChangeListener}, and
 * {@code android.hardware.Camera.Parameters}.
 */
public interface CameraManager {

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.AutoFocusCallback}.
     */
    public interface CameraAFCallback {
        public void onAutoFocus(boolean focused, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.AutoFocusMoveCallback}.
     */
    public interface CameraAFMoveCallback {
        public void onAutoFocusMoving(boolean moving, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.ShutterCallback}.
     */
    public interface CameraShutterCallback {
        public void onShutter(CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.PictureCallback}.
     */
    public interface CameraPictureCallback {
        public void onPictureTaken(byte[] data, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.PreviewCallback}.
     */
    public interface CameraPreviewDataCallback {
        public void onPreviewFrame(byte[] data, CameraProxy camera);
    }

    /**
     * An interface which wraps
     * {@link android.hardware.Camera.FaceDetectionListener}.
     */
    public interface CameraFaceDetectionCallback {
        /**
         * Callback for face detection.
         *
         * @param faces   Recognized face in the preview.
         * @param camera  The camera which the preview image comes from.
         */
        public void onFaceDetection(Camera.Face[] faces, CameraProxy camera);
    }

    /**
     * An interface to be called for any exception caught when opening the
     * camera device. This error callback is different from the one defined
     * in the framework, {@link android.hardware.Camera.ErrorCallback}, which
     * is used after the camera is opened.
     */
    public interface CameraOpenErrorCallback {
    }

    /**
     * Opens the camera of the specified ID synchronously.
     *
     * @param handler The {@link android.os.Handler} in which the callback
     *                was handled.
     * @param callback The callback when any error happens.
     * @param cameraId The camera ID to open.
     * @return   An instance of {@link CameraProxy} on success. null on failure.
     */
    public CameraProxy cameraOpen(
            Handler handler, int cameraId, CameraOpenErrorCallback callback);

    /**
     * An interface that takes camera operation requests and post messages to the
     * camera handler thread. All camera operations made through this interface is
     * asynchronous by default except those mentioned specifically.
     */
    public interface CameraProxy {

        /**
         * Returns the underlying {@link android.hardware.Camera} object used
         * by this proxy. This method should only be used when handing the
         * camera device over to {@link android.media.MediaRecorder} for
         * recording.
         */
        public android.hardware.Camera getCamera();
//
//        /**
//         * Releases the camera device synchronously.
//         * This function must be synchronous so the caller knows exactly when the camera
//         * is released and can continue on.
//         */
//        public void release();
//
//        /**
//         * Reconnects to the camera device.
//         * @see android.hardware.Camera#reconnect()
//         *
//         * @param handler The {@link android.os.Handler} in which the callback
//         *                was handled.
//         * @param cb The callback when any error happens.
//         * @return {@code false} on errors.
//         */
//        public boolean reconnect(Handler handler, CameraOpenErrorCallback cb);
//
//        /**
//         * Unlocks the camera device.
//         *
//         * @see android.hardware.Camera#unlock()
//         */
//        public void unlock();
//
//        /**
//         * Locks the camera device.
//         * @see android.hardware.Camera#lock()
//         */
//        public void lock();
//
//        /**
//         * Sets the {@link android.graphics.SurfaceTexture} for preview.
//         *
//         * @param surfaceTexture The {@link SurfaceTexture} for preview.
//         */
//        public void setPreviewTexture(final SurfaceTexture surfaceTexture);
//
//        /**
//         * Sets the {@link android.view.SurfaceHolder} for preview.
//         *
//         * @param surfaceHolder The {@link SurfaceHolder} for preview.
//         */
//        public void setPreviewDisplay(final SurfaceHolder surfaceHolder);
//
//        /**
//         * Starts the camera preview.
//         */
//        public void startPreview();
//
//        /**
//         * Stops the camera preview synchronously.
//         * {@code stopPreview()} must be synchronous to ensure that the caller can
//         * continues to release resources related to camera preview.
//         */
//        public void stopPreview();
//
//        /**
//         * Sets the callback for preview data.
//         *
//         * @param handler    The {@link android.os.Handler} in which the callback was handled.
//         * @param cb         The callback to be invoked when the preview data is available.
//         * @see  android.hardware.Camera#setPreviewCallback(android.hardware.Camera.PreviewCallback)
//         */
//        public void setPreviewDataCallback(Handler handler, CameraPreviewDataCallback cb);
//
//        /**
//         * Sets the callback for preview data.
//         *
//         * @param handler The handler in which the callback will be invoked.
//         * @param cb      The callback to be invoked when the preview data is available.
//         * @see android.hardware.Camera#setPreviewCallbackWithBuffer(android.hardware.Camera.PreviewCallback)
//         */
//        public void setPreviewDataCallbackWithBuffer(Handler handler, CameraPreviewDataCallback cb);
//
//        public void setOneShotPreviewCallback(Handler handler, CameraPreviewDataCallback cb);
//        /**
//         * Adds buffer for the preview callback.
//         *
//         * @param callbackBuffer The buffer allocated for the preview data.
//         */
//        public void addCallbackBuffer(byte[] callbackBuffer);
//
//        /**
//         * Starts the auto-focus process. The result will be returned through the callback.
//         *
//         * @param handler The handler in which the callback will be invoked.
//         * @param cb      The auto-focus callback.
//         */
//        public void autoFocus(Handler handler, CameraAFCallback cb);
//
//        /**
//         * Cancels the auto-focus process.
//         */
//        public void cancelAutoFocus();
//
//
        /**
         * Instrument the camera to take a picture.
         *
         * @param handler   The handler in which the callback will be invoked.
         * @param shutter   The callback for shutter action, may be null.
         * @param raw       The callback for uncompressed data, may be null.
         * @param postview  The callback for postview image data, may be null.
         * @param jpeg      The callback for jpeg image data, may be null.
         * @see android.hardware.Camera#takePicture(
         *         android.hardware.Camera.ShutterCallback,
         *         android.hardware.Camera.PictureCallback,
         *         android.hardware.Camera.PictureCallback)
         */
        public void takePicture(
                Handler handler,
                CameraShutterCallback shutter,
                CameraPictureCallback raw,
                CameraPictureCallback postview,
                CameraPictureCallback jpeg);

//        /**
//         * Sets the face detection listener.
//         *
//         * @param handler  The handler in which the callback will be invoked.
//         * @param callback The callback for face detection results.
//         */
//        public void setFaceDetectionCallback(Handler handler, CameraFaceDetectionCallback callback);
//
//        /**
//         * Starts the face detection.
//         */
//        public void startFaceDetection();
//
//        /**
//         * Sets the camera parameters.
//         *
//         * @param params The camera parameters to use.
//         */
//        public void setParameters(Camera.Parameters params);
//
//        /**
//         * Gets the current camera parameters synchronously. This method is
//         * synchronous since the caller has to wait for the camera to return
//         * the parameters. If the parameters are already cached, it returns
//         * immediately.
//         */
//        public Camera.Parameters getParameters();
//
//        /**
//         * Forces {@code CameraProxy} to update the cached version of the camera
//         * parameters regardless of the dirty bit.
//         */
//        public void refreshParameters();
//
//        /**
//         * Enables/Disables the camera shutter sound.
//         *
//         * @param enable   {@code true} to enable the shutter sound,
//         *                 {@code false} to disable it.
//         */
//        public void enableShutterSound(boolean enable);
//
//        /**
//         * Send the Histogram Data.
//         *
//         */
//        public void sendHistogramData();
//        /**
//         * Enables/Disables longshot mode.
//         *
//         * @param enable   {@code true} to enable longshot mode,
//         *                 {@code false} to disable it.
//         */
//        public void setLongshot(boolean enable);
    }
}
