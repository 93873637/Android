package com.cloudminds.feedback.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cloudminds.feedback.net.IFUserFeedback.ImageBody;
import com.cloudminds.feedback.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "task.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TASK_TABLE = "task";

    public static class TaskColumns {
        public static final String _ID = "_id";
        public static final String REPORT_ID = "report_id";
        public static final String TASK_FLAG = "task_flag"; //0 for pending, 1 for history
        public static final String CONTENT = "content";
        public static final String MODULE = "module";
        public static final String APP_VERSION = "app_version";
        public static final String ERROR_TYPE = "error_type";
        public static final String ERROR_TIME = "error_time";
        public static final String DEVICE_MODEL = "device_model";
        public static final String IMEI1 = "imei1";
        public static final String BUILD_VERSION = "build_version";
        public static final String HARDWARE_VERSION = "hardware_version";
        public static final String ANDROID_VERSION = "android_version";
        public static final String STORAGE_USAGE = "storage_usage";
        public static final String MEMORY_USAGE = "memory_usage";
        public static final String CPU_USAGE = "cpu_usage";
        public static final String NETWORK_TYPE = "network_type";
        public static final String BATTERY_LEVEL = "battery_level";
        public static final String ATTACHMENT_NAME = "attachment_name";
        public static final String ATTACHMENT_SIZE = "attachment_size";
        public static final String IS_MONKEY = "is_monkey";
    }

    private static final String CREATE_TASK_TABLE = "CREATE TABLE " + TASK_TABLE + " (" +
            TaskColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            TaskColumns.REPORT_ID + " INTEGER DEFAULT 0," +
            TaskColumns.TASK_FLAG + " INTEGER DEFAULT 0," +
            TaskColumns.CONTENT + " TEXT," +
            TaskColumns.MODULE + " TEXT," +
            TaskColumns.APP_VERSION + " TEXT," +
            TaskColumns.ERROR_TYPE + " TEXT," +
            TaskColumns.ERROR_TIME + " TEXT," +
            TaskColumns.DEVICE_MODEL + " TEXT," +
            TaskColumns.IMEI1 + " TEXT," +
            TaskColumns.BUILD_VERSION + " TEXT," +
            TaskColumns.HARDWARE_VERSION + " TEXT," +
            TaskColumns.ANDROID_VERSION + " TEXT," +
            TaskColumns.STORAGE_USAGE + " TEXT," +
            TaskColumns.MEMORY_USAGE + " TEXT," +
            TaskColumns.CPU_USAGE + " TEXT," +
            TaskColumns.NETWORK_TYPE + " TEXT," +
            TaskColumns.BATTERY_LEVEL + " TEXT," +
            TaskColumns.ATTACHMENT_NAME + " TEXT," +
            TaskColumns.ATTACHMENT_SIZE + " TEXT," +
            TaskColumns.IS_MONKEY + " INTEGER DEFAULT 0" +
            ");";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static TaskDatabaseHelper sInstance;

    public static synchronized TaskDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TaskDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            dropTables(db);
            createTables(db);
        }
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(CREATE_TASK_TABLE);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
    }

    public List<ImageBody> queryTaskData(String selection) {
        List<ImageBody> taskList = new ArrayList<>();
        try (Cursor cursor = getWritableDatabase().query(TASK_TABLE, null, selection, null, null, null, null)) {
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int _id = cursor.getInt(cursor.getColumnIndex(TaskColumns._ID));
                    int report_id = cursor.getInt(cursor.getColumnIndex(TaskColumns.REPORT_ID));
                    int task_flag = cursor.getInt(cursor.getColumnIndex(TaskColumns.TASK_FLAG));
                    String content = cursor.getString(cursor.getColumnIndex(TaskColumns.CONTENT));
                    String module = cursor.getString(cursor.getColumnIndex(TaskColumns.MODULE));
                    String app_version = cursor.getString(cursor.getColumnIndex(TaskColumns.APP_VERSION));
                    String error_type = cursor.getString(cursor.getColumnIndex(TaskColumns.ERROR_TYPE));
                    String error_time = cursor.getString(cursor.getColumnIndex(TaskColumns.ERROR_TIME));
                    String device_model = cursor.getString(cursor.getColumnIndex(TaskColumns.DEVICE_MODEL));
                    String imei1 = cursor.getString(cursor.getColumnIndex(TaskColumns.IMEI1));
                    String build_version = cursor.getString(cursor.getColumnIndex(TaskColumns.BUILD_VERSION));
                    String hardware_version = cursor.getString(cursor.getColumnIndex(TaskColumns.HARDWARE_VERSION));
                    String android_version = cursor.getString(cursor.getColumnIndex(TaskColumns.ANDROID_VERSION));
                    String storage_usage = cursor.getString(cursor.getColumnIndex(TaskColumns.STORAGE_USAGE));
                    String memory_usage = cursor.getString(cursor.getColumnIndex(TaskColumns.MEMORY_USAGE));
                    String cpu_usage = cursor.getString(cursor.getColumnIndex(TaskColumns.CPU_USAGE));
                    String network_type = cursor.getString(cursor.getColumnIndex(TaskColumns.NETWORK_TYPE));
                    String battery_level = cursor.getString(cursor.getColumnIndex(TaskColumns.BATTERY_LEVEL));
                    String attachment_name = cursor.getString(cursor.getColumnIndex(TaskColumns.ATTACHMENT_NAME));
                    String attachment_size = cursor.getString(cursor.getColumnIndex(TaskColumns.ATTACHMENT_SIZE));
                    int is_monkey = cursor.getInt(cursor.getColumnIndex(TaskColumns.IS_MONKEY));
                    taskList.add(new ImageBody(_id, report_id, task_flag, content, module, app_version,
                            error_type, error_time, device_model, imei1, build_version,
                            hardware_version, android_version, storage_usage, memory_usage, cpu_usage,
                            network_type, battery_level, attachment_name, attachment_size, is_monkey));
                }
            }
        } catch (Exception e) {
            LogUtils.e("Failed to query task data.");
            e.printStackTrace();
        }
        return taskList;
    }

    public void insertTaskData(ImageBody imageBody) {
        ContentValues values = new ContentValues();
        values.put(TaskColumns.REPORT_ID, imageBody.report_id);
        values.put(TaskColumns.TASK_FLAG, imageBody.task_flag);
        values.put(TaskColumns.CONTENT, imageBody.content);
        values.put(TaskColumns.MODULE, imageBody.module);
        values.put(TaskColumns.APP_VERSION, imageBody.app_version);
        values.put(TaskColumns.ERROR_TYPE, imageBody.error_type);
        values.put(TaskColumns.ERROR_TIME, imageBody.error_time);
        values.put(TaskColumns.DEVICE_MODEL, imageBody.device_model);
        values.put(TaskColumns.IMEI1, imageBody.imei1);
        values.put(TaskColumns.BUILD_VERSION, imageBody.build_version);
        values.put(TaskColumns.HARDWARE_VERSION, imageBody.hardware_version);
        values.put(TaskColumns.ANDROID_VERSION, imageBody.android_version);
        values.put(TaskColumns.STORAGE_USAGE, imageBody.storage_usage);
        values.put(TaskColumns.MEMORY_USAGE, imageBody.memory_usage);
        values.put(TaskColumns.CPU_USAGE, imageBody.cpu_usage);
        values.put(TaskColumns.NETWORK_TYPE, imageBody.network_type);
        values.put(TaskColumns.BATTERY_LEVEL, imageBody.battery_level);
        values.put(TaskColumns.ATTACHMENT_NAME, imageBody.attachment_name);
        values.put(TaskColumns.ATTACHMENT_SIZE, imageBody.attachment_size);
        values.put(TaskColumns.IS_MONKEY, imageBody.is_monkey);

        try {
            getWritableDatabase().insert(TASK_TABLE, null, values);
            LogUtils.i("Insert a row of data: " + imageBody);
        } catch (Exception e) {
            LogUtils.e("Failed to insert task data.");
            e.printStackTrace();
        }
    }

    public void deleteTaskData(int id) {
        String selection = TaskColumns._ID + "=" + id;
        try {
            getWritableDatabase().delete(TASK_TABLE, selection, null);
            LogUtils.i("Delete a row of data: " + selection);
        } catch (Exception e) {
            LogUtils.e("Failed to delete task data.");
            e.printStackTrace();
        }
    }

    public void updateTaskData(int id, int report_id) {
        String selection = TaskColumns._ID + "=" + id;
        ContentValues values = new ContentValues(2);
        values.put(TaskColumns.REPORT_ID, report_id);
        values.put(TaskColumns.TASK_FLAG, 1);
        try {
            getWritableDatabase().update(TASK_TABLE, values, selection, null);
            LogUtils.i("Update a row of data from pending to history: " + selection);
        } catch (Exception e) {
            LogUtils.e("Failed to update task data.");
            e.printStackTrace();
        }
    }
}
