package com.liz.puremusic.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.puremusic.R;
import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.utils.LogUtils;

public class MainActivity extends AppCompatActivity implements View.OnCreateContextMenuListener {
    private TextView playInfo;

    //progress info
    private TextView musicTime;
    private TextView musicTotal;
    private SeekBar seekBar;

    //control buttons
    private ImageButton btnPlayOrPause;
    private int mPlayOrPauseBackground;  //resource id of playOrPause button
    private ImageButton btnStop;
    private ImageButton btnPlayPrev;
    private ImageButton btnPlayNext;
    private ImageButton btnPlayMode;
    private ImageButton btnPlayList;
    //private ImageButton btnQuit;

    private final int REQUEST_CODE_PLAY_LIST = 1000;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public Handler mTimerHandler;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateUiOnTimer();
            if (mTimerHandler != null) {
                mTimerHandler.postDelayed(runnable, ComDef.PLAYUI_REFRESH_TIMER);
            }
        }
    };
    public void startUiTimer() {
        LogUtils.d("MainActivity.startUiTimer: handler=" + mTimerHandler);
        if (mTimerHandler == null) {
            mTimerHandler = new Handler();
            mTimerHandler.post(runnable);
        }
    }
    public void stopUiTimer() {
        LogUtils.d("MainActivity.stopUiTimer: handler=" + mTimerHandler);
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacks(runnable);
            mTimerHandler = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        findViewById();
        setListener();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    DataLogic.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void updateUiOnTimer() {
        //LogUtils.d("MainActivity.updateUiOnTimer");
        musicTime.setText(DataLogic.getFormatCurrentPosition());
        musicTotal.setText(DataLogic.getFormatCurrentDuration());
        seekBar.setMax(DataLogic.getCurrentPlayDuration());
        seekBar.setProgress(DataLogic.getCurrentPlayPosition());
        playInfo.setText(DataLogic.getPlayInfo());
        if (DataLogic.getPlayStatus() == ComDef.PLAY_STATUS_STOPPED) {
            btnPlayOrPause.setBackgroundResource(R.drawable.play);
        }

        //update play button in case of play/pause music on daemon(bluetooth, etc)
        if (DataLogic.isPlaying() && mPlayOrPauseBackground != R.drawable.pause) {
            LogUtils.d("MainActivity.updateUiOnTimer: set backgroud as play, play status changed daemon?");
            btnPlayOrPause.setBackgroundResource(R.drawable.pause);
            mPlayOrPauseBackground = R.drawable.pause;
        }
        else if (!DataLogic.isPlaying() && mPlayOrPauseBackground != R.drawable.play) {
            LogUtils.d("MainActivity.updateUiOnTimer: set backgroud as pause, play status changed daemon?");
            btnPlayOrPause.setBackgroundResource(R.drawable.play);
            mPlayOrPauseBackground = R.drawable.play;
        }
    }

    private void findViewById() {
        musicTime = findViewById(R.id.MusicTimePlayed);
        musicTotal =  findViewById(R.id.MusicTimeTotal);
        seekBar = findViewById(R.id.MusicSeekBar);

        playInfo = findViewById(R.id.PlayInfo);

        btnPlayOrPause = findViewById(R.id.BtnPlayorPause);
        btnStop = findViewById(R.id.BtnStop);
        btnPlayPrev = findViewById(R.id.BtnPlayPrev);
        btnPlayNext = findViewById(R.id.BtnPlayNext);
        btnPlayMode = findViewById(R.id.BtnPlayMode);
        btnPlayList = findViewById(R.id.BtnPlayList);
        //btnQuit = findViewById(R.id.BtnQuit);
    }

    private void setListener() {
        btnPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataLogic.isPlaying()) {
                    DataLogic.onPausePlay();
                    btnPlayOrPause.setBackgroundResource(R.drawable.play);
                    mPlayOrPauseBackground = R.drawable.play;
                } else {
                    DataLogic.onStartPlay();
                    btnPlayOrPause.setBackgroundResource(R.drawable.pause);
                    mPlayOrPauseBackground = R.drawable.pause;
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataLogic.onStopPlay();
                btnPlayOrPause.setBackgroundResource(R.drawable.play);
            }
        });

        btnPlayPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataLogic.isPlayFirst()) {
                    Toast.makeText(MainActivity.this, "Already be the first one", Toast.LENGTH_SHORT).show();
                }
                else {
                    DataLogic.goPrev();
                }
            }
        });

        btnPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataLogic.isPlayLast()) {
                    Toast.makeText(MainActivity.this, "Already be the last one", Toast.LENGTH_SHORT).show();
                }
                else {
                    DataLogic.goNext();
                }
            }
        });

        //###@: btnPlayMode.setText(DataLogic.getPlayModeName());
        btnPlayMode.setBackgroundResource(getPlayModeResId());
        btnPlayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataLogic.switchPlayMode();
                btnPlayMode.setBackgroundResource(getPlayModeResId());
                /*
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.mode_popupmenu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.play_mode_list:
                                DataLogic.setPlayMode(ComDef.PLAY_MODE_LIST);
                                break;
                            case R.id.play_mode_list_loop:
                                DataLogic.setPlayMode(ComDef.PLAY_MODE_LIST_LOOP);
                                break;
                            case R.id.play_mode_random:
                                DataLogic.setPlayMode(ComDef.PLAY_MODE_RANDOM);
                                break;
                            case R.id.play_mode_single_loop:
                                DataLogic.setPlayMode(ComDef.PLAY_MODE_SINGLE_LOOP);
                                break;
                            case R.id.play_mode_single:
                                DataLogic.setPlayMode(ComDef.PLAY_MODE_SINGLE);
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
                //*/
            }
        });

        btnPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PlayListActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PLAY_LIST);
            }
        });

//        btnQuit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopUiTimer();
//                DataLogic.stopMusicService();
//                MainActivity.this.finish();
//            }
//        });
    }

    public static int getPlayModeResId() {
        switch (DataLogic.getPlayMode()) {
            case ComDef.PLAY_MODE_LIST:
                return R.drawable.list;
            case ComDef.PLAY_MODE_LIST_LOOP:
                return R.drawable.list_loop;
            case ComDef.PLAY_MODE_RANDOM:
                return R.drawable.random;
            case ComDef.PLAY_MODE_SINGLE:
                return R.drawable.single;
            case ComDef.PLAY_MODE_SINGLE_LOOP:
                return R.drawable.single_loop;
             default:
                 break;
        }
        return R.drawable.list;
    }

    @Override
    public void onResume() {
        super.onResume();
        startUiTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUiTimer();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PLAY_LIST) {
                //####@:
//                Uri uri = data.getData();
//                //Toast.makeText(this, "文件路径："+uri.getPath().toString(), Toast.LENGTH_SHORT).show();
//                String[] proj = {MediaStore.Images.Media.DATA};
//                Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
//                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                actualimagecursor.moveToFirst();
//                String img_path = actualimagecursor.getString(actual_image_column_index);
//                MusicService.mMusicFile = img_path;
                Toast.makeText(MainActivity.this, "play list return", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d("MainActivity.onDestroy");
        stopUiTimer();
        super.onDestroy();
    }
}
