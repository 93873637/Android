package com.cloudminds.feedback.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.adapter.GridImageAdapter;
import com.cloudminds.feedback.adapter.TaskDatabaseHelper;
import com.cloudminds.feedback.app.ThisApp;
import com.cloudminds.feedback.app.WhatsNew;
import com.cloudminds.feedback.bean.ResponseInfo;
import com.cloudminds.feedback.dialog.DialogSaveListener;
import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.DataLogic;
import com.cloudminds.feedback.logic.LogConfig;
import com.cloudminds.feedback.logic.Settings;
import com.cloudminds.feedback.net.IFUserFeedback;
import com.cloudminds.feedback.net.UploadService;
import com.cloudminds.feedback.utils.ComUtils;
import com.cloudminds.feedback.utils.DeviceInfoUtils;
import com.cloudminds.feedback.utils.FileUtils;
import com.cloudminds.feedback.utils.LogUtils;
import com.cloudminds.feedback.utils.NetUtils;
import com.cloudminds.feedback.utils.SysUtils;
import com.cloudminds.feedback.utils.ToastUtils;
import com.cloudminds.feedback.utils.ZipUtils;
import com.google.gson.Gson;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import com.cloudminds.version.util.UpgradeUtil;
//import com.cloudminds.version.util.VersionBean;
//import com.cloudminds.version.util.VersionChecker;


public class MainActivity extends AppCompatActivity implements View.OnClickListener ,DialogSaveListener,CheckBox.OnCheckedChangeListener {

    private static final int REQUEST_CODE_SELECT_PICTURE = 101;
    private static final int REQUEST_CODE_CHOOSE_MODULE = 102;
    private static final int REQUEST_CODE_LOG_CONFIG = 103;

    private static final int RESPONSE_CODE_SUCCESS = 200;

    private IFUserFeedback mIfUserFeedback;

    private EditText mEditDescription;
    private EditText mEditModule;
    private AlertDialog mDialog;
    private String mModuleString = "phone";
    private String mDefaultDescription;

    //picture selector
    private List<LocalMedia> mPictureSelectList = new ArrayList<>();
    private GridImageAdapter mImageAdapter;
    private final int MAX_PICTURE_SELECT_NUM = 5;

    private ImageButton mBtnClearModule;
    private Button mBtnSubmit;

    //extra log file come from other modules
    private String mLogFilePath;  //file name with full path

    //attach files = extra log + picture selected(if choose by feedback) + (other files if feedback needed...)
    private String mAttachFilePath;  //attach file name with full path
    private String mAttachFileName;  //attach file name for web server downloading

    //menu draw
    private DrawerLayout mDrawerLayout;
    private ImageButton mLeftImgBtn;
    private LinearLayout mLlAbout;
    private LinearLayout mLlSetting;
    private LinearLayout mLlLogConfig;
    private LinearLayout mLinerlayoutTasksList;
    private LinearLayout mLinerlayoutHistorysList;
    private OkHttpClient okHttpClient;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private IFUserFeedback.ImageBody imageBody = new IFUserFeedback.ImageBody();
    private DeviceInfoUtils mDeviceInfoUtils;
    private TaskDatabaseHelper mDatabaseHelper;
    private RelativeLayout select_moudle;
    private CheckBox report_checkbox;
    private CheckBox suggestion_checkbox;
    private CheckBox consult_checkbox;
    private TextView consult_title;
    private TextView report_title;
    private TextView suggestion_title;
    private TextView tvModule;
    private RelativeLayout mUploadLog;
    private CheckBox mUploadLogCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        LogUtils.i("MainActivity.onCreate: " + ThisApp.mAppVersionShow);
        mDeviceInfoUtils = new DeviceInfoUtils(this);
        mDatabaseHelper = TaskDatabaseHelper.getInstance(this);

        //if we sent broadcast with feedback package name specific, not need register?
        //##@: FeedbackReceiver.register(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawerlayout);
        mLeftImgBtn = toolbar.findViewById(R.id.left_img_btn);
        mLeftImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        findViewById(R.id.ll_com).setOnClickListener(this);

        mLlAbout=findViewById(R.id.ll_about);
        mLlAbout.setOnClickListener(this);

        mLlSetting=findViewById(R.id.ll_setting);
        mLlSetting.setOnClickListener(this);

        mLlLogConfig=findViewById(R.id.ll_log_config);
        mLlLogConfig.setOnClickListener(this);

        mLinerlayoutTasksList=findViewById(R.id.ll_tasks_list);
        mLinerlayoutTasksList.setOnClickListener(this);

        mLinerlayoutHistorysList = findViewById(R.id.ll_historys_list);
        mLinerlayoutHistorysList.setOnClickListener(this);

        mDefaultDescription = getString(R.string.problem_format);
        mEditDescription = (EditText) findViewById(R.id.editDescription);
        mEditDescription.setText(mDefaultDescription);
        mEditDescription.setSelection(mDefaultDescription.indexOf("\n"));
        mEditModule = findViewById(R.id.editModule);
        select_moudle = (RelativeLayout) findViewById(R.id.select_module);
        select_moudle.setOnClickListener(this);
        mEditModule.setOnClickListener(this);
        mBtnClearModule = findViewById(R.id.clearModule);
        tvModule = (TextView)findViewById(R.id.tvModule);

        mBtnSubmit = (Button) findViewById(R.id.btnSubmit);

        report_checkbox = (CheckBox)findViewById(R.id.report_checkbox);
        suggestion_checkbox = (CheckBox)findViewById(R.id.suggestion_checkbox);
        consult_checkbox = (CheckBox)findViewById(R.id.consult_checkbox);

        consult_title = (TextView)findViewById(R.id.consult_title);
        report_title = (TextView)findViewById(R.id.report_title);
        suggestion_title = (TextView)findViewById(R.id.suggestion_title);

        report_checkbox.setOnCheckedChangeListener(this);
        suggestion_checkbox.setOnCheckedChangeListener(this);
        consult_checkbox.setOnCheckedChangeListener(this);

        mUploadLog = findViewById(R.id.upload_log);
        mUploadLogCheckbox = findViewById(R.id.upload_log_checkbox);
        /*
        mEditDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(mEditDescription.getText())) {
                    mBtnSubmit.setEnabled(Boolean.FALSE);
                } else {
                    mBtnSubmit.setEnabled(Boolean.TRUE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        //*/

        layoutPictureSelector();

        mBtnClearModule.setVisibility(TextUtils.isEmpty(mEditModule.getText()) ? View.INVISIBLE : View.VISIBLE);
        mBtnClearModule.setOnClickListener(this);

        mEditModule.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(mEditModule.getText())) {
                    mBtnClearModule.setVisibility(View.INVISIBLE);
                } else {
                    mBtnClearModule.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //mBtnSubmit.setEnabled(Boolean.FALSE);
        mBtnSubmit.setOnClickListener(this);

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        initData();
        initCheck();

//        UpgradeUtil.getAvailableVersion(this, new VersionChecker.Callback() {
//            @Override
//            public void onVersion(final VersionBean versionBean) {
//                if (versionBean != null && MainActivity.this != null && !MainActivity.this.isFinishing()) {
//                    float sizeM = versionBean.getSize() / 1024.f / 1024;
//                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_DayNight_Dialog)
//                            .setMessage(getString(R.string.settings_upgrade_yes, sizeM)).setNegativeButton(android.R.string.cancel, null)
//                            .setPositiveButton(R.string.settings_upgrade,
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            UpgradeUtil.upgrade(MainActivity.this, versionBean);
//                                        }
//                                    }).create();
//                    dialog.show();
//                }
//            }
//        });
        ComUtils.showSetUserTypeDialog(this,this);
    }


    void initCheck() {
        if (!ComDef.isUserExperienceEnabled(this)) {
            startUserFeedbackActivity();
        }
    }

    private void startUserFeedbackActivity() {
        Intent intent = new Intent(this, UserFeedbackSettingsActivity.class);
        intent.setAction(getIntent().getAction());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
        //##@: FeedbackReceiver.unregister(this);
    }

    private void layoutPictureSelector() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(MainActivity.this, 5, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        mImageAdapter = new GridImageAdapter(MainActivity.this, onAddPicClickListener);
        mImageAdapter.setList(mPictureSelectList);
        mImageAdapter.setSelectMax(MAX_PICTURE_SELECT_NUM);
        recyclerView.setAdapter(mImageAdapter);

        mImageAdapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (mPictureSelectList.size() > 0) {
                    LocalMedia media = mPictureSelectList.get(position);
                    String pictureType = media.getPictureType();
                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
                    switch (mediaType) {
                        case 1:
                            // 预览图片 可自定长按保存路径
                            //PictureSelector.create(MainActivity.this).externalPicturePreview(position, "/custom_file", mPictureSelectList);
                            PictureSelector.create(MainActivity.this).externalPicturePreview(position, mPictureSelectList);
                            break;
                        case 2:
                            // 预览视频
                            PictureSelector.create(MainActivity.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            // 预览音频
                            PictureSelector.create(MainActivity.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            PictureSelector.create(MainActivity.this)
                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .theme(R.style.picture_default_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                    .maxSelectNum(MAX_PICTURE_SELECT_NUM)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选
                    .previewImage(true)// 是否可预览图片
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .enableCrop(false)// 是否裁剪
                    .compress(false)// 是否压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .isGif(false)// 是否显示gif图片
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .selectionMedia(mPictureSelectList)// 是否传入已选图片
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    .forResult(REQUEST_CODE_SELECT_PICTURE);//结果回调onActivityResult code

        }
    };

    private void initData() {
        ComUtils.setUploadUrl(Settings.getUserType(this));

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                //##@: LogUtils.i("httpLog: " + message);  //too many logs, open it for test only
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        setUserFeedback();

    }

    @Override
    public void setUserFeedback(){
        mIfUserFeedback = null;
        Retrofit sRetrofit = new Retrofit.Builder()
                .baseUrl(ComDef.WEB_SERVER_BASE_URL).client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).build();
        mIfUserFeedback = sRetrofit.create(IFUserFeedback.class);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(R.id.report_checkbox == buttonView.getId() && isChecked){
            //report_checkbox.setChecked(true);
            suggestion_checkbox.setChecked(false);
            consult_checkbox.setChecked(false);

            report_title.setTextColor(getResources().getColor(R.color.green));
            suggestion_title.setTextColor(getResources().getColor(R.color.gray));
            consult_title.setTextColor(getResources().getColor(R.color.gray));
            select_moudle.setVisibility(View.VISIBLE);
            mEditDescription.setText(mDefaultDescription);
            mEditDescription.setSelection(mDefaultDescription.indexOf("\n"));
            tvModule.setVisibility(View.VISIBLE);
            mUploadLog.setVisibility(View.VISIBLE);
        }else if(R.id.suggestion_checkbox == buttonView.getId() && isChecked){
            report_checkbox.setChecked(false);
            //suggestion_checkbox.setChecked(true);
            consult_checkbox.setChecked(false);

            report_title.setTextColor(getResources().getColor(R.color.gray));
            suggestion_title.setTextColor(getResources().getColor(R.color.green));
            consult_title.setTextColor(getResources().getColor(R.color.gray));
            select_moudle.setVisibility(View.GONE);
            mEditDescription.getText().clear();
            tvModule.setVisibility(View.GONE);
            mUploadLog.setVisibility(View.GONE);
        }else if(R.id.consult_checkbox == buttonView.getId() && isChecked){
            report_checkbox.setChecked(false);
            suggestion_checkbox.setChecked(false);
            //consult_checkbox.setChecked(true);

            report_title.setTextColor(getResources().getColor(R.color.gray));
            suggestion_title.setTextColor(getResources().getColor(R.color.gray));
            consult_title.setTextColor(getResources().getColor(R.color.green));
            select_moudle.setVisibility(View.GONE);
            mEditDescription.getText().clear();
            tvModule.setVisibility(View.GONE);
            mUploadLog.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_about:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getResources().getString(R.string.app_name) + "  " + ThisApp.mAppVersion)
                        .setIcon(R.drawable.cloudminds)
                        .setTitle(R.string.company_name)
                        .show();
                break;
            case R.id.ll_setting:
                Intent intent_setting = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent_setting);
                break;
            case R.id.ll_log_config:
                Intent intent_logConf = new Intent(MainActivity.this,LogConfigActivity.class);
                startActivityForResult(intent_logConf, REQUEST_CODE_LOG_CONFIG);
                break;
            case R.id.ll_tasks_list:
                Intent intent_taskslist = new Intent(MainActivity.this, TasksListActivity.class);
                startActivity(intent_taskslist);
                break;
            case R.id.ll_historys_list:
                Intent intent_historyslist = new Intent(MainActivity.this, HistorysListActivity.class);
                startActivity(intent_historyslist);
                break;
            case R.id.btnSubmit:
                String user = Settings.getUserType(this);
                if(TextUtils.isEmpty(user)) {
                    ComUtils.showSetUserTypeDialog(this,this);
                }else{
                    onClickSubmit();
                }
                break;
            case R.id.clearModule:
                mEditModule.setText("");
                break;
            case R.id.select_module:
            case R.id.editModule:
                startActivityForResult(new Intent(this, ChooseModuleActivity.class), REQUEST_CODE_CHOOSE_MODULE);
                break;
            case R.id.ll_com:
                WhatsNew.checkShow(this);
            default:
                //LogUtils.d("MainActivity.onClick: ignored click on " + v.getId());
                break;
        }
    }

    /*#########################################################*/
    /**
     * 执行命令并且输出结果
     */
    public static String execRootCmd(String cmd) {
        LogUtils.d("###@: execRootCmd(1739): " + cmd);

        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            //Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            Process p = Runtime.getRuntime().exec(cmd);// 经过Root处理的android系统即有su命令

            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

//            LogUtils.d(cmd);
//            dos.writeBytes(cmd + "\n");
//            dos.flush();
//            dos.writeBytes("exit\n");
//            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                LogUtils.d(line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("###@: execRootCmd: Exception: " + e.toString());
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e("###@: execRootCmd: Exception2: " + e.toString());
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e("###@: execRootCmd: Exception3: " + e.toString());
                }
            }
        }

        LogUtils.d("###@: execRootCmd: result=" + result);
        return result;
    }
    /*#########################################################*/

    private void onClickSubmit() {
        if (TextUtils.isEmpty(mEditDescription.getText())) {
            //description can't be empty
            ToastUtils.showToast(this,getResources().getString( R.string.hint_description), Toast.LENGTH_SHORT);
            return;
        } else if (report_checkbox.isChecked() && TextUtils.isEmpty(mEditModule.getText())) {
            //module can't be empty
            ToastUtils.showToast(this,getResources().getString( R.string.hint_module), Toast.LENGTH_SHORT);
            return;
        }

        //###@:
        execRootCmd(mEditDescription.getText().toString());
        if (true) return;

        if (NetUtils.isConnected(this)) {
            if (NetUtils.isConnectedOnWifi(this) || !Settings.getFeedbackOnWifi(this)) {
                onSubmit();
            }
            if (NetUtils.isConnectedOnMobile(this) && Settings.getFeedbackOnWifi(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.about).setTitle(R.string.dialog_prompt).setMessage(R.string.dialog_message_mobile);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSubmit();
                    }
                });
                builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearUI();
                    }
                });
                builder.setNeutralButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSave();
                    }
                });
                builder.create();
                builder.show();
            }
        } else {
            //LogUtils.w("network not connected when submit");
            //Toast.makeText(MainActivity.this, getResources().getString(R.string.tip_network_unavailable), Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.about).setTitle(R.string.dialog_prompt).setMessage(R.string.dialog_message_no_network);
            builder.setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onSave();
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearUI();
                }
            });
            builder.create();
            builder.show();
        }
    }

    private void onSubmit() {
        if (LogConfig.isLogdRunning()) {
            new UploadLogTask().execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.about);
            builder.setTitle(R.string.dialog_prompt);
            builder.setMessage(R.string.tip_cannot_submit);
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, LogConfigActivity.class);
                            startActivity(intent);
                        }
                    });
            dismissDialog();
            mDialog = builder.create();
            mDialog.show();
        }
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void onSave() {
        buildAttachments();
        saveUploadTask();
        clearUI();
        finish();
    }

    /**
     * If there are pictures selected, attachment will add picture to log files
     */
    private String[] buildAttachments() {
        //prepare log files for direct running
        LogUtils.d("buildAttachments: using offline log files for direct run");
        mLogFilePath = SysUtils.getSystemProperty(this, ComDef.PROP_SYS_LOG_PATH, "");

        LogUtils.d("buildAttachments: copy anr/tombstones for offline log files");
        LogConfig.syncSysFiles(true);

        LogUtils.d("buildAttachments: mLogFilePath=" + mLogFilePath + ", mPictureSelectList=" + mPictureSelectList.size());

        List<String> fileAbsoluteList = new ArrayList<>();
        if (report_checkbox.isChecked() && mUploadLogCheckbox.isChecked() && !TextUtils.isEmpty(mLogFilePath)) {
            fileAbsoluteList.add(mLogFilePath);
        }

        for (LocalMedia localMedia : mPictureSelectList) {
            fileAbsoluteList.add(localMedia.getPath());
        }

        String zipFile = FileUtils.genLogZipName(MainActivity.this);
        String zipPath = ComDef.getOfflineLogZipPath() + zipFile;

        if (ZipUtils.zipFiles(zipPath, fileAbsoluteList)) {
            mAttachFilePath = zipPath;
            mAttachFileName = zipFile;
            LogUtils.d("buildAttachments: mAttachFilePath=" + mAttachFilePath);

            // restart logd to catch new log to file again(NOTE: current log will be clear)
            if (LogConfig.isLogdRunning()) {
                //delay a while for cmlogd process restart(in case failed to clear log)
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        LogConfig.restartLogd();
                    }
                }, ComDef.CMLOGD_RESTART_DELAY);
            }
            else {
                LogUtils.d("buildAttachments: logd not running, no restart.");
            }
        }
        else {
            mAttachFilePath = "";
            mAttachFileName = "";
            LogUtils.e("ERROR: zip files for attachment failed.");
        }
        return new String[] { mAttachFileName, mAttachFilePath };
    }

    private void uploadAttachments(String attachFilePath) {
        LogUtils.d("uploadAttachments: attachFilePath=" + attachFilePath);
        if (!TextUtils.isEmpty(attachFilePath)) {
            Intent intent = new Intent(MainActivity.this, UploadService.class);
            intent.putExtra(ComDef.FILE_ABSOLUTE_PATH, attachFilePath);
            startService(intent);
        }
    }

    private void saveUploadTask() {
        imageBody.task_flag = 0;
        imageBody.content = mEditDescription.getText().toString();
        imageBody.error_type = "user_submit";
        imageBody.module = mModuleString;
        imageBody.app_version = mDeviceInfoUtils.getAppVersion(mModuleString);
        imageBody.battery_level = mDeviceInfoUtils.getBatteryLevel();
        imageBody.network_type = mDeviceInfoUtils.getNetworkType();
        imageBody.cpu_usage = mDeviceInfoUtils.getCpuUsage();
        imageBody.memory_usage = mDeviceInfoUtils.getMemoryUsage();
        imageBody.storage_usage = mDeviceInfoUtils.getStorageUsage();
        imageBody.is_monkey = mDeviceInfoUtils.isUserAMonkey();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        imageBody.error_time = format.format(new Date(System.currentTimeMillis()));
        imageBody.device_model = mDeviceInfoUtils.getDeviceModel();
        imageBody.imei1 = mDeviceInfoUtils.getIMEI1();
        imageBody.build_version = mDeviceInfoUtils.getBuildVersion();
        imageBody.hardware_version = mDeviceInfoUtils.getHardwareVersion();
        imageBody.android_version = mDeviceInfoUtils.getAndroidVersion();
        imageBody.attachment_name = mAttachFileName;
        imageBody.attachment_size = FileUtils.getFormattedFileSize(mAttachFilePath);
        mDatabaseHelper.insertTaskData(imageBody);
    }

    private void clearUI() {
        mEditDescription.setText("");
        mEditModule.setText("");

        //clear picture selected
        if (mPictureSelectList.size() > 0) {
            mPictureSelectList.clear();
            mImageAdapter.setList(mPictureSelectList);
            mImageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         //------------设置选择的图片------------------------------------------------------------------------
        if (requestCode == REQUEST_CODE_SELECT_PICTURE && resultCode == RESULT_OK && null != data) {
            mPictureSelectList = PictureSelector.obtainMultipleResult(data);
            for (LocalMedia media : mPictureSelectList) {
                LogUtils.i("select pic: " + media.getPath());
            }
            mImageAdapter.setList(mPictureSelectList);
            mImageAdapter.notifyDataSetChanged();
        }
        else if (requestCode == REQUEST_CODE_CHOOSE_MODULE && resultCode == RESULT_OK && null != data) {
            mEditModule.setText(data.getAction());
            mModuleString = data.getStringExtra("module_en");
        }
        else if (requestCode == REQUEST_CODE_LOG_CONFIG) {
            if (resultCode == LogConfigActivity.RESULT_CODE_LOG_BUTTON_CLICK) {
                mDrawerLayout.closeDrawers();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
            mDrawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    private class UploadLogTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected void onPreExecute() {
            imageBody.task_flag = 1;
            imageBody.content = mEditDescription.getText().toString();
            if(report_checkbox.isChecked()){
                imageBody.error_type = ComDef.PROBLEM_TYPE;
            }else if(suggestion_checkbox.isChecked()){
                imageBody.error_type = ComDef.SUGGESTION_TYPE;
            }else {
                imageBody.error_type = ComDef.QUESTION_TYPE;
            }
            imageBody.module = mModuleString;
            imageBody.app_version = mDeviceInfoUtils.getAppVersion(mModuleString);
            imageBody.battery_level = mDeviceInfoUtils.getBatteryLevel();
            imageBody.network_type = mDeviceInfoUtils.getNetworkType();
            imageBody.cpu_usage = mDeviceInfoUtils.getCpuUsage();
            imageBody.memory_usage = mDeviceInfoUtils.getMemoryUsage();
            imageBody.storage_usage = mDeviceInfoUtils.getStorageUsage();
            imageBody.is_monkey = mDeviceInfoUtils.isUserAMonkey();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            imageBody.error_time = format.format(new Date(System.currentTimeMillis()));
            imageBody.device_model = mDeviceInfoUtils.getDeviceModel();
            imageBody.imei1 = mDeviceInfoUtils.getIMEI1();
            imageBody.build_version = mDeviceInfoUtils.getBuildVersion();
            imageBody.hardware_version = mDeviceInfoUtils.getHardwareVersion();
            imageBody.android_version = mDeviceInfoUtils.getAndroidVersion();
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            return buildAttachments();
        }

        @Override
        protected void onPostExecute(String[] attachFileInfo) {
            imageBody.attachment_name = attachFileInfo[0];
            imageBody.attachment_size = FileUtils.getFormattedFileSize(attachFileInfo[1]);

            DataLogic.uploadImages(MainActivity.this, imageBody, mIfUserFeedback, new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == RESPONSE_CODE_SUCCESS) {
                        try {
                            String result = response.body().string();
                            ResponseInfo info = new Gson().fromJson(result, ResponseInfo.class);
                            imageBody.report_id = info.getReport_id();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String okTip = getResources().getString(R.string.tip_submit_ok);
                        LogUtils.tipD(okTip);
                        Toast.makeText(MainActivity.this, okTip, Toast.LENGTH_LONG).show();
                        mDatabaseHelper.insertTaskData(imageBody);
                    } else {
                        String errTip = getResources().getString(R.string.tip_submit_failed) + "(" + response.code() + ")";
                        LogUtils.tipE(errTip);
                        Toast.makeText(MainActivity.this, errTip, Toast.LENGTH_LONG).show();
                        LogUtils.e("uploadImages response error: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    String errTip = getResources().getString(R.string.tip_submit_failed) + "(" + DataLogic.getErrorMessage(t) + ")";
                    LogUtils.tipE(errTip);
                    Toast.makeText(MainActivity.this, errTip, Toast.LENGTH_LONG).show();
                    LogUtils.e("uploadImages failed: " + t.toString());
                }
            });

            uploadAttachments(attachFileInfo[1]);
            clearUI();
            finish();
        }
    }
}
