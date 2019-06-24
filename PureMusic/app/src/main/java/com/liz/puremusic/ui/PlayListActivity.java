package com.liz.puremusic.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.liz.puremusic.R;
import com.liz.puremusic.adapter.PlayListAdapter;
import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.utils.LogUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class PlayListActivity extends AppCompatActivity {

    private TextView mTextLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        ImageButton btnGoBack = findViewById(R.id.go_back);
        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayListActivity.this.finish();
            }
        });

        TextView btnPlayListHome = findViewById(R.id.playlist_home);
        btnPlayListHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(PlayListActivity.this);
                dialog.setMessage("loading, please wait...");
                dialog.setCancelable(false);
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DataLogic.loadPlayListHome();
                        dialog.dismiss();
                    }
                }).start();
            }
        });

        TextView btnClearPlayList = findViewById(R.id.clear_playlist);
        btnClearPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataLogic.clearPlayList();
                updateUI();
            }
        });

        TextView btnSortPlayList = findViewById(R.id.sort_playlist);
        btnSortPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataLogic.sortPlayList();
                updateUI();
            }
        });

        setToolbarTitle();

        ListView listView = findViewById(R.id.lv_items);
        listView.setAdapter(PlayListAdapter.getAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                LogUtils.d("PlayListActivity.onItemClick: position=" + position + ", arg1=" + arg1 + ", arg3=" + arg3);
                if (position == DataLogic.getCurrentListPos()) {
                    if (DataLogic.isPlaying())
                        DataLogic.switchPlayOrPause();
                    else
                        DataLogic.onStartPlay();;
                }
                else {
                    DataLogic.goTo(position);
                }
                updateUI();
            }
        });

        PlayListAdapter.setOnListOperListener(new PlayListAdapter.OnListOperListener() {
            @Override
            public void onItemRemove(int position) {
                DataLogic.removeFromList(position);
                updateUI();
            }
        });

        ImageButton btnMusicLists = findViewById(R.id.music_lists);
        btnMusicLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileDir = new File(ComDef.PURE_MUSIC_DEFAULT_PATH);
                FolderPickDialog pickDialog = new FolderPickDialog(PlayListActivity.this, fileDir);
                pickDialog.setOnFileSelectListener(new FilePickDialog.OnFileSelectListener() {
                    @Override
                    public void onFileSelect(File file) {
                        //Toast.makeText(PlayListActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                        DataLogic.onAddMusic(file);
                        updateUI();
                    }
                });
                pickDialog.show();
            }
        });

        ImageButton btnAddMusicFile = findViewById(R.id.add_music_file);
        btnAddMusicFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileDir = new File(ComDef.PURE_MUSIC_DEFAULT_PATH);
                FilePickDialog filePickDialog = new FilePickDialog(PlayListActivity.this, fileDir);
                filePickDialog.setOnFileSelectListener(new FilePickDialog.OnFileSelectListener() {
                    @Override
                    public void onFileSelect(File file) {
                        //Toast.makeText(MainActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                        DataLogic.onAddMusic(file.getPath());
                        updateUI();
                    }
                });
                filePickDialog.show();
            }
        });

        ImageButton btnAddMusicFolder = findViewById(R.id.add_music_folder);
        btnAddMusicFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileDir = new File(ComDef.PURE_MUSIC_DEFAULT_PATH);
                FolderPickDialog pickDialog = new FolderPickDialog(PlayListActivity.this, fileDir);
                pickDialog.setOnFileSelectListener(new FilePickDialog.OnFileSelectListener() {
                    @Override
                    public void onFileSelect(File file) {
                        //Toast.makeText(PlayListActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                        final File folder = file;
                        final ProgressDialog dialog = new ProgressDialog(PlayListActivity.this);
                        dialog.setMessage("loading, please wait...");
                        dialog.setCancelable(false);
                        dialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DataLogic.onAddMusic(folder);
                                dialog.dismiss();
                            }
                        }).start();
                    }
                });
                pickDialog.show();
            }
        });

        startUiTimer();
    }

    public void setToolbarTitle() {
        TextView title = findViewById(R.id.toolbar_title);
        title.setText(ComDef.PLAY_LIST_TITLE  + DataLogic.getPlayListInfo());
    }

    private void startUiTimer() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, ComDef.PLAYLIST_TIMER_DELAY, ComDef.PLAYLIST_TIMER_PERIOD);
    }

    public void updateUI() {
        PlayListAdapter.onDataChanged();
        setToolbarTitle();
    }
}
