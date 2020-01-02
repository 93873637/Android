package com.liz.puretorch.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.liz.puretorch.R;
import com.liz.puretorch.logic.ComDef;
import com.liz.puretorch.logic.DataLogic;
import com.liz.puretorch.utils.LogUtils;

import static com.liz.puretorch.logic.DataLogic.blue;
import static com.liz.puretorch.logic.DataLogic.green;
import static com.liz.puretorch.logic.DataLogic.red;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // For Storage Permissions
    private static final int REQUEST_CAMERA = 1;
    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
    };

//    PowerManager mPowerManager = null;
//    PowerManager.WakeLock mWakeLock = null;

    private LinearLayout mLayoutMain;
    private SeekBar mSeekbarLight;
    private SeekBar mSeekbarRed;
    private VerticalSeekBar mSeekbarGreen;
    private VerticalSeekBar mSeekbarBlue;
    private VerticalSeekBar mSeekbarGray;

    private GestureDetectorCompat mDetector;
    TorchSwitchButton mBtnSwitch;

    //private TextView mTorchBorder;
    private LinearLayout mTorchBorder;
    private TextView mColorInfo;

    /*
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

//        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
//        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getName());

        mLayoutMain = findViewById(R.id.layoutMain);
        mColorInfo = findViewById(R.id.colorInfo);
        mTorchBorder = findViewById(R.id.layoutBorder);
        //mTorchBorder = findViewById(R.id.torchInfo);

        //the seek bar to adjust brightness
        mSeekbarLight = (SeekBar) findViewById(R.id.seekbarLight);
        mSeekbarLight.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarLight.setProgress(DataLogic.lux); //TorchUtils.getSystemBrightness(this));
        mSeekbarLight.setOnSeekBarChangeListener(new OnLightSeekBakChangeListener());

        //the seek bar to adjust colors
        mSeekbarRed = findViewById(R.id.seekbarRed);
        mSeekbarRed.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarRed.setProgress(red);
        mSeekbarRed.setOnSeekBarChangeListener(new OnColorSeekBakChangeListener());

        mSeekbarGreen = findViewById(R.id.seekbarGreen);
        mSeekbarGreen.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarGreen.setProgress(green);
        mSeekbarGreen.setOnSeekBarChangeListener(new OnColorSeekBakChangeListener());

        mSeekbarBlue = findViewById(R.id.seekbarBlue);
        mSeekbarBlue.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarBlue.setProgress(blue);
        mSeekbarBlue.setOnSeekBarChangeListener(new OnColorSeekBakChangeListener());

        //the seek bar to adjust grayness
        mSeekbarGray = findViewById(R.id.seekbarGray);
        mSeekbarGray.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarGray.setProgress(ComDef.SEEKBAR_MAX);
        mSeekbarGray.setOnSeekBarChangeListener(new OnGraySeekBakChangeListener());

        mBtnSwitch = findViewById(R.id.btnSwitch);
        mBtnSwitch.setOnClickListener(this);
        mBtnSwitch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                }
                return true;
            }
        });

        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this, mBtnSwitch);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(mBtnSwitch);

        //BottomNavigationView navigation = findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Check if we have permission
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(MainActivity.this,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
        }
        else {
            onEnableTorch(true);
            /*
            //to flash activity
            Intent intent = new Intent();
            intent.setClass(this, FlashActivity.class);
            startActivity(intent);
            //*/
        }

        updateUI();
    }

    public void updateUI() {
        updateAppBrightness();
        updateBackgroundColor();
        updateColorInfo();
    }

    public void updateColorBars(){
        mSeekbarRed.setProgress(red);  //######@@@@@:
    }

    public void updateAppBrightness() {
        TorchUtils.changeAppBrightness(MainActivity.this, DataLogic.lux);
    }

    public void updateBackgroundColor() {
        mLayoutMain.setBackgroundColor(Color.rgb(red, green, blue));
        LogUtils.d("updateBackgroundColor: red=" + red + ", green=" + green + ", blue=" + blue);
    }

    public void updateColorInfo() {
        mColorInfo.setText(DataLogic.getTorchInfo());
    }

//    public void updateSeekBarAll() {
//        int all = mSeekbarGray.getProgress();
//        TorchUtils.changeAppBrightness(MainActivity.this, all);
//        mLayoutMain.setBackgroundColor(Color.rgb(all, all, all));
//        LogUtils.d("updateSeekBarAll: all=" + all);
//    }

    public void onEnableTorch(boolean bEnable) {
        TorchUtils.enableTorch(MainActivity.this, bEnable);
        updateTorchInfo();
    }

    public void onSwitchTorch() {
        TorchUtils.switchTorch(MainActivity.this);
        updateTorchInfo();
    }

    private void updateTorchInfo() {
        int color = 0xff0000ff;
        if (TorchUtils.isTorchEnabled()) {
            color = 0xffff0000;
        }
        mTorchBorder.setBackgroundColor(color);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mWakeLock.acquire();
    }

    protected void onPause() {
        super.onPause();
        //mWakeLock.release();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Toast.makeText(MainActivity.this, "onRequestPermissionsResult of REQUEST_CAMERA", Toast.LENGTH_SHORT).show();
        if (requestCode == REQUEST_CAMERA) {
            onEnableTorch(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSwitch:
                int flingType = mBtnSwitch.getFlingType();
                LogUtils.d("FlingType = " + flingType);
                switch (flingType) {
                    case TorchSwitchButton.FLING_TYPE_EXIT:
                        onEnableTorch(false);
                        this.finish();
                        break;
                    case TorchSwitchButton.FLING_TYPE_SWITCH:
                        onSwitchTorch();
                        break;
                    default:
                        LogUtils.e("ERROR: Unknown fling type " + flingType);
                        break;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        onEnableTorch(false);
        super.onBackPressed();
    }

    class OnLightSeekBakChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            DataLogic.lux = progress;
            MainActivity.this.updateUI();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    class OnGraySeekBakChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            DataLogic.red = progress;
            DataLogic.green = progress;
            DataLogic.blue = progress;
            DataLogic.lux = progress;
            MainActivity.this.updateUI();
            MainActivity.this.updateColorBars();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    class OnColorSeekBakChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar == mSeekbarRed) {
                red = seekBar.getProgress();
            }
            if (seekBar == mSeekbarGreen) {
                green = seekBar.getProgress();
            }
            if (seekBar == mSeekbarBlue) {
                blue = seekBar.getProgress();
            }
            MainActivity.this.updateUI();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
