package com.liz.calllogreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Permissions Required
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        findViewById(R.id.btnReadCallLog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchCallLogs();
            }
        });
    }

    protected void fetchCallLogs() {
        ArrayList<String> callList = getCallHistoryList(MainActivity.this);
        TextView tvInfo = findViewById(R.id.textInfo);
        if (callList == null || callList.isEmpty()) {
            tvInfo.setText("No call logs.");
        }
        else {
            tvInfo.setText("");
            for (int i = 0; i < callList.size(); i++) {
                String callInfo = "#" + (i + 1) + ": " + callList.get(i) + "\n";
                tvInfo.append(callInfo);
            }
        }
    }

    protected void checkPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions,
                    REQUEST_CODE_PERMISSIONS);
        }
    }

    protected String[] getQueryString() {
//        return new String[]{
//                CallLog.Calls.CACHED_NAME,  //姓名
//                CallLog.Calls.NUMBER,    //号码
//                CallLog.Calls.TYPE,  //呼入/呼出(2)/未接
//                CallLog.Calls.DATE,  //拨打时间
//                CallLog.Calls.DURATION,   //通话时长
//        };
        return new String[]{
                CallLog.Calls.CACHED_FORMATTED_NUMBER,
                CallLog.Calls.CACHED_LOOKUP_URI,
                CallLog.Calls.CACHED_MATCHED_NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NORMALIZED_NUMBER,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.COUNTRY_ISO,
                CallLog.Calls.DATA_USAGE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.FEATURES,
                CallLog.Calls.GEOCODED_LOCATION,
                CallLog.Calls.NEW,
                CallLog.Calls.NUMBER,
                CallLog.Calls.NUMBER_PRESENTATION,
                CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.POST_DIAL_DIGITS,
                CallLog.Calls.TRANSCRIPTION,
                CallLog.Calls.TYPE,
                CallLog.Calls.VIA_NUMBER,
                CallLog.Calls.VOICEMAIL_URI,
        };
    }

    public ArrayList<String> getCallHistoryList(Activity activity) {
        ArrayList<String> callLogList = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CALL_LOG}, 1000);
        }

        Cursor cs = activity.getContentResolver().query(CallLog.Calls.CONTENT_URI, //系统方式获取通讯录存储地址
                getQueryString(), null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

        if (cs != null && cs.getCount() > 0) {
            for (cs.moveToFirst(); (!cs.isAfterLast()); cs.moveToNext()) {
                String callInfo = "";
                for (int i=0; i<cs.getColumnCount(); i++) {
                    callInfo += cs.getString(i) + " | ";
                    callLogList.add(callInfo);
                }
            }
        }

        return callLogList;

//        if (cs != null && cs.getCount() > 0) {
//            Date date = new Date(System.currentTimeMillis());
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            String date_today = simpleDateFormat.format(date);
//            for (cs.moveToFirst(); (!cs.isAfterLast()); cs.moveToNext(), i++) {
//
//                String callName = cs.getString(0);  //名称
//                String callNumber = cs.getString(1);  //号码
//
//                //如果名字为空，在通讯录查询一次有没有对应联系人
//                if (callName == null || callName.equals("")) {
//                    String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME};
//                    //设置查询条件
//                    String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + "='" + callNumber + "'";
//                    Cursor cursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            cols, selection, null, null);
//                    int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
//                    if (cursor.getCount() > 0) {
//                        cursor.moveToFirst();
//                        callName = cursor.getString(nameFieldColumnIndex);
//                    }
//                    cursor.close();
//                }
//
//                //通话类型
//                int callType = Integer.parseInt(cs.getString(2));
//                String callTypeStr;
//                switch (callType) {
//                    case CallLog.Calls.INCOMING_TYPE:
//                        callTypeStr = "INCOMING";
//                        break;
//                    case CallLog.Calls.OUTGOING_TYPE:
//                        callTypeStr = "OUTGOING";
//                        break;
//                    case CallLog.Calls.MISSED_TYPE:
//                        callTypeStr = "MISSED";
//                        break;
//                    default:
//                        //其他类型的，例如新增号码等记录不算进通话记录里，直接跳过
//                        callTypeStr = "TYPE" + callType;
//                        break;
//                }
//
//                //拨打时间
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                Date callDate = new Date(Long.parseLong(cs.getString(3)));
//                String callDateStr = sdf.format(callDate);
//                if (callDateStr.equals(date_today)) { //判断是否为今天
//                    sdf = new SimpleDateFormat("HH:mm");
//                    callDateStr = sdf.format(callDate);
//                } else if (date_today.contains(callDateStr.substring(0, 7))) { //判断是否为当月
//                    sdf = new SimpleDateFormat("dd");
//                    int callDay = Integer.valueOf(sdf.format(callDate));
//
//                    int day = Integer.valueOf(sdf.format(date));
//                    if (day - callDay == 1) {
//                        callDateStr = "昨天";
//                    } else {
//                        sdf = new SimpleDateFormat("MM-dd");
//                        callDateStr = sdf.format(callDate);
//                    }
//                } else if (date_today.contains(callDateStr.substring(0, 4))) { //判断是否为当年
//                    sdf = new SimpleDateFormat("MM-dd");
//                    callDateStr = sdf.format(callDate);
//                }
//
//                //通话时长
//                int callDuration = Integer.parseInt(cs.getString(4));
//                int min = callDuration / 60;
//                int sec = callDuration % 60;
//                String callDurationStr = "";
//                if (sec > 0) {
//                    if (min > 0) {
//                        callDurationStr = min + "分" + sec + "秒";
//                    } else {
//                        callDurationStr = sec + "秒";
//                    }
//                }
//
//                /**
//                 * callName 名字
//                 * callNumber 号码
//                 * callTypeStr 通话类型
//                 * callDateStr 通话日期
//                 * callDurationStr 通话时长
//                 * 请在此处执行相关UI或存储操作，之后会查询下一条通话记录
//                 */
//                Log.i("Msg", "callName" + callName);
//                Log.i("Msg", "callNumber" + callNumber);
//                Log.i("Msg", "callTypeStr" + callTypeStr);
//                Log.i("Msg", "callDateStr" + callDateStr);
//                Log.i("Msg", "callDurationStr" + callDurationStr);
//            }
//        }
    }
}
