package com.liz.cmdtool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.cmdtool.app.ComDef;
import com.liz.cmdtool.app.ThisApp;
import com.liz.cmdtool.utils.LogUtils;

public class MainActivity extends AppCompatActivity {

    final String CMD_PROMPT = "# ";

    private TextView mTextInfo;
    ScrollView mScrollView;
    private EditText mEditCmd;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextInfo = findViewById(R.id.textInfo);
        mScrollView = findViewById(R.id.scrollInfo);

        mEditCmd = findViewById(R.id.editCmd);
        mEditCmd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    onCommand();
                }
                return true;
            }
        });

        ImageButton mBtnSend = findViewById(R.id.btnSend);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                onCommand();
            }
        });

        ImageButton mBtnPrev = findViewById(R.id.btnPrev);
        mBtnPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                onCommandPrev();
            }
        });

        ImageButton mBtnNext = findViewById(R.id.btnNext);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                onCommandNext();
            }
        });

        ImageButton mBtnList = findViewById(R.id.btnList);
        mBtnList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                onCommandList();
            }
        });

        showPrompt();
        setInstallPermission();

        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(mEditCmd, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v != null && shouldHideInputMethod(v, ev)) {
                LogUtils.d("input method should hide");
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null && imm.isActive()) {
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean shouldHideInputMethod(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            //之前一直不成功的原因是,getX获取的是相对父视图的坐标,getRawX获取的才是相对屏幕原点的坐标！！！
            //Log.v("leftTop[]", "zz--left:" + left + "--top:" + top + "--bottom:" + bottom + "--right:" + right);
            //Log.v("event", "zz--getX():" + event.getRawX() + "--getY():" + event.getRawY());
            if ((event.getRawX() > left) && (event.getRawX() < right)
                    && (event.getRawY() > top) && (event.getRawY() < bottom)) {
                LogUtils.d("click on edit area");
                return false;
            } else {
                LogUtils.d("click out of edit area");
                return true;
            }
        }
        return false;
    }

    protected void onCommand() {
        String cmdStr = mEditCmd.getText().toString();
        mEditCmd.setText("");
        onCommand(cmdStr);
    }

    protected void onCommand(String cmdStr) {
        LogUtils.d("onCommand: cmdStr=\"" + cmdStr + "\"");
        cmdStr = cmdStr.replace("\n", "");
        cmdStr = cmdStr.trim();
        LogUtils.d("onCommand: pure cmdStr=\"" + cmdStr + "\"");

        if (TextUtils.isEmpty(cmdStr)) {
            LogUtils.d("command empty");

            //turn to new line for empty command
            showInfo("", true);
        }
        else {
            //first show cmd line
            showInfo(cmdStr);

            if (ThisApp.isSpecialCmd(cmdStr)) {
                showInfo(ThisApp.getCmdInfo());
            }
            else if (ThisApp.isNumeric(cmdStr)) {
                String realCmd = ThisApp.getCmd(Integer.parseInt(cmdStr)-1);
                if (TextUtils.isEmpty(realCmd)) {
                    showInfo("ERROR: no cmd by index " + cmdStr);
                }
                else {
                    showInfo(realCmd);
                    execCmd(realCmd);
                }
            }
            else {
                ThisApp.addToCmdList(cmdStr);
                execCmd(cmdStr);
            }
        }

        //show prompt for next cmd
        showPrompt();
    }

    private void execCmd(String cmdStr) {
        //run command and show result
        String cmdResult =  CmdTool.runCmd(cmdStr);
        if (TextUtils.isEmpty(cmdResult)) {
            LogUtils.d("empty result by command \"" + cmdStr + "\"");
        }
        else {
            showInfo(cmdResult);
        }
    }

    protected void onCommandPrev() {
        String cmdStr = ThisApp.getPrevCmd();
        if (TextUtils.isEmpty(cmdStr)) {
            LogUtils.d("No prev command.");
            mEditCmd.setText("");
        }
        else {
            mEditCmd.setText(cmdStr);
        }
    }

    protected void onCommandNext() {
        String cmdStr = ThisApp.getNextCmd();
        if (TextUtils.isEmpty(cmdStr)) {
            LogUtils.d("No next command.");
            mEditCmd.setText("");
        }
        else {
            mEditCmd.setText(cmdStr);
        }
    }

    protected void onCommandList() {
        onCommand(ComDef.SPECIAL_CMD_STRING_LIST);
    }

    protected void showPrompt() {
        showInfo(CMD_PROMPT, false);
    }

    protected void showInfo(final String msg) {
        showInfo(msg, !msg.endsWith("\n"));
    }

    protected void showInfo(final String msg, final boolean newLine) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (newLine) {
                    mTextInfo.append(msg + "\n");
                }
                else {
                    mTextInfo.append(msg);
                }
                mTextInfo.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.smoothScrollTo(0, mTextInfo.getBottom());
                    }
                });
            }
        });
    }

    /**
     * 8.0以上系统设置安装未知来源权限
     */
    public void setInstallPermission(){
        boolean haveInstallPermission = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //先判断是否有安装未知来源应用的权限
            haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if(!haveInstallPermission){
                //弹框提示用户手动打开
                MainActivity.this.showAlert(this, "安装权限", "需要打开允许来自此来源，请去设置中开启此权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //此方法需要API>=26才能使用
                            toInstallPermissionSettingIntent();
                        }
                    }
                });
                return;
            }
        }
    }

    final int INSTALL_PERMISS_CODE = 0x101;

    /**
     * 开启安装未知来源权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void toInstallPermissionSettingIntent() {
        Uri packageURI = Uri.parse("package:"+getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        startActivityForResult(intent, INSTALL_PERMISS_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == INSTALL_PERMISS_CODE) {
            Toast.makeText(this,"安装应用",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * alert 消息提示框显示
     * @param context   上下文
     * @param title     标题
     * @param message   消息
     * @param listener  监听器
     */
    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", listener);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_configuration);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
