package com.liz.puretorch.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
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
    private VerticalSeekBar mSeekbarAll;

    private GestureDetectorCompat mDetector;
    TorchSwitchButton mBtnSwitch;

    private TextView mTorchInfo;

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
        mTorchInfo = findViewById(R.id.torchInfo);

        mSeekbarLight = (SeekBar) findViewById(R.id.seekbarLight);
        mSeekbarLight.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarLight.setProgress(DataLogic.lux); //TorchUtils.getSystemBrightness(this));
        mSeekbarLight.setOnSeekBarChangeListener(new OnLightSeekBakChangeListener());

        mSeekbarRed = (SeekBar) findViewById(R.id.seekbarRed);
        mSeekbarRed.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarRed.setProgress(red);
        mSeekbarRed.setOnSeekBarChangeListener(new OnColorSeekBakChangeListener());

        mSeekbarGreen = (VerticalSeekBar) findViewById(R.id.seekbarGreen);
        mSeekbarGreen.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarGreen.setProgress(green);
        mSeekbarGreen.setOnSeekBarChangeListener(new OnColorSeekBakChangeListener());

        mSeekbarBlue = (VerticalSeekBar) findViewById(R.id.seekbarBlue);
        mSeekbarBlue.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarBlue.setProgress(blue);
        mSeekbarBlue.setOnSeekBarChangeListener(new OnColorSeekBakChangeListener());

        mSeekbarAll = (VerticalSeekBar) findViewById(R.id.seekbarAll);
        mSeekbarAll.setMax(ComDef.SEEKBAR_MAX);
        mSeekbarAll.setProgress(ComDef.SEEKBAR_MAX);
        mSeekbarAll.setOnSeekBarChangeListener(new OnAllSeekBakChangeListener());

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

//        mBtnSwitch.setOnTouchListener(new View.OnTouchListener() {
//
//            float mPosX;
//            float mPosY;
//            float mCurPosX;
//            float mCurPosY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mDetector.onTouchEvent(event);
//
//                // TODO Auto-generated method stub
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mPosX = event.getX();
//                        mPosY = event.getY();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        mCurPosX = event.getX();
//                        mCurPosY = event.getY();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        if (mCurPosY - mPosY > 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {
//                            //向下滑動
//
//                        } else if (mCurPosY - mPosY < 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {
//                            //向上滑动
//                            //collapse();
//                        }
//                        v.performClick();
//                        break;
//                }
//                return true;
//            }
//        });

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
            TorchUtils.enableTorch(MainActivity.this, true);
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
        updateTorchInfo();
    }

    public void updateAppBrightness() {
        TorchUtils.changeAppBrightness(MainActivity.this, DataLogic.lux);
    }

    public void updateBackgroundColor() {
        //int red = mSeekbarRed.getProgress();
        //int green = mSeekbarGreen.getProgress();
        //int blue = mSeekbarBlue.getProgress();
        mLayoutMain.setBackgroundColor(Color.rgb(red, green, blue));
        LogUtils.d("updateBackgroundColor: red=" + red + ", green=" + green + ", blue=" + blue);
    }

    public void updateTorchInfo() {
        mTorchInfo.setText(DataLogic.getTorchInfo());
    }

//    public void updateSeekBarAll() {
//        int all = mSeekbarAll.getProgress();
//        TorchUtils.changeAppBrightness(MainActivity.this, all);
//        mLayoutMain.setBackgroundColor(Color.rgb(all, all, all));
//        LogUtils.d("updateSeekBarAll: all=" + all);
//    }

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
            TorchUtils.enableTorch(MainActivity.this, true);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSwitch:
                LogUtils.d("fastFling=" + mBtnSwitch.fastFling);
                if (mBtnSwitch.fastFling) {
                    TorchUtils.enableTorch(MainActivity.this, false);
                    this.finish();
                }
                else {
                    TorchUtils.switchTorch(MainActivity.this);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        TorchUtils.enableTorch(MainActivity.this, false);
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

    class OnAllSeekBakChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            DataLogic.red = progress;
            DataLogic.green = progress;
            DataLogic.blue = progress;
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
