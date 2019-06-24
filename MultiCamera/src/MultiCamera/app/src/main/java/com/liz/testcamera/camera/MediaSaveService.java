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

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.liz.testcamera.exif.ExifInterface;
import com.liz.testcamera.utils.LogUtils;

/*
 * Service for saving images in the background thread.
 */
public class MediaSaveService extends Service {
    public static final String VIDEO_BASE_URI = "content://media/external/video/media";

    // The memory limit for unsaved image is 50MB.
    private static final int SAVE_TASK_MEMORY_LIMIT_IN_MB = 50;
                                   //PersistUtil.getSaveTaskMemoryLimitInMb();

    private static final int SAVE_TASK_MEMORY_LIMIT = SAVE_TASK_MEMORY_LIMIT_IN_MB * 1024 * 1024;
    private static final String TAG = "CAM_" + MediaSaveService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private Listener mListener;
    // Memory used by the total queued save request, in bytes.
    private long mMemoryUse;

    public interface Listener {
        public void onQueueStatus(boolean full);
    }

    public interface OnMediaSavedListener {
        public void onMediaSaved(Uri uri);
    }

    public class LocalBinder extends Binder {
        public MediaSaveService getService() {
            return MediaSaveService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onCreate() {
        mMemoryUse = 0;
    }

    public boolean isQueueFull() {
        return (mMemoryUse >= SAVE_TASK_MEMORY_LIMIT);
    }

    public void addImage(final byte[] data, String title, long date, Location loc,
            int width, int height, int orientation, ExifInterface exif,
            OnMediaSavedListener l, ContentResolver resolver, String pictureFormat) {
        LogUtils.d("MediaSaveService.addImage");

        if (isQueueFull()) {
            LogUtils.e("Cannot add image when the queue is full");
            return;
        }
        ImageSaveTask t = new ImageSaveTask(data, title, date,
                (loc == null) ? null : new Location(loc),
                width, height, orientation, exif, resolver, l, pictureFormat);

        mMemoryUse += data.length;
        if (isQueueFull()) {
            onQueueFull();
        }
        t.execute();
    }

    public void setListener(Listener l) {
        mListener = l;
        if (l == null) return;
        l.onQueueStatus(isQueueFull());
    }

    private void onQueueFull() {
        if (mListener != null) mListener.onQueueStatus(true);
    }

    private void onQueueAvailable() {
        if (mListener != null) mListener.onQueueStatus(false);
    }

    private class ImageSaveTask extends AsyncTask <Void, Void, Uri> {
        private byte[] data;
        private String title;
        private long date;
        private Location loc;
        private int width, height;
        private int orientation;
        private ExifInterface exif;
        private ContentResolver resolver;
        private OnMediaSavedListener listener;
        private String pictureFormat;

        public ImageSaveTask(byte[] data, String title, long date, Location loc,
                             int width, int height, int orientation, ExifInterface exif,
                             ContentResolver resolver, OnMediaSavedListener listener, String pictureFormat) {
            this.data = data;
            this.title = title;
            this.date = date;
            this.loc = loc;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
            this.exif = exif;
            this.resolver = resolver;
            this.listener = listener;
            this.pictureFormat = pictureFormat;
        }

        @Override
        protected void onPreExecute() {
            // do nothing.
        }

        @Override
        protected Uri doInBackground(Void... v) {
            if (width == 0 || height == 0) {
                // Decode bounds
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                width = options.outWidth;
                height = options.outHeight;
            }
            return Storage.addImage(
                    resolver, title, date, loc, orientation, exif, data, width, height, pictureFormat);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (listener != null) listener.onMediaSaved(uri);
            boolean previouslyFull = isQueueFull();
            mMemoryUse -= data.length;
            if (isQueueFull() != previouslyFull) onQueueAvailable();
        }
    }
}
