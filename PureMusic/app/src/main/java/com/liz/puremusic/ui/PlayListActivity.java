package com.liz.puremusic.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.liz.androidutils.LogUtils;
import com.liz.puremusic.R;
import com.liz.puremusic.adapter.PlayListAdapter;
import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class PlayListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        initToolbar();
        initPlayList();
        initActionBar();
    }

    private void initToolbar() {
        ImageButton btnGoBack = findViewById(R.id.go_back);
        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayListActivity.this.finish();
            }
        });
        setToolbarTitle();
        TextView btnPlayListHome = findViewById(R.id.playlist_home);
        btnPlayListHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlayListHome();
            }
        });
        TextView btnClearPlayList = findViewById(R.id.clear_playlist);
        btnClearPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlayListClear();
            }
        });
        TextView btnSortPlayList = findViewById(R.id.sort_playlist);
        btnSortPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlayListSort();
            }
        });
    }

    private void initPlayList() {
        ListView listView = findViewById(R.id.lv_items);
        listView.setAdapter(PlayListAdapter.inst());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                LogUtils.d("PlayListActivity.onItemClick: position=" + position + ", arg1=" + arg1 + ", arg3=" + arg3);
                if (position == DataLogic.getCurrentListPos()) {
                    if (DataLogic.isPlaying())
                        DataLogic.switchPlayOrPause();
                    else
                        DataLogic.onStartPlay();
                    ;
                } else {
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
    }

    private void initActionBar() {
        ImageButton btnMusicLists = findViewById(R.id.music_lists);
        btnMusicLists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayListActivity.this, SettingsActivity.class));
            }
        });
        ImageButton btnAddMusicFolder = findViewById(R.id.add_music_folder);
        btnAddMusicFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileDir = new File(DataLogic.getMusicHome());
                FolderPickDialog pickDialog = new FolderPickDialog(PlayListActivity.this, fileDir, "SELECT DIR");
                pickDialog.setOnFileSelectListener(new FilePickDialog.OnFileSelectListener() {
                    @Override
                    public void onFileSelect(File file) {
                        //Toast.makeText(PlayListActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                        final File folder = file;
                        final ProgressDialog dialog = new ProgressDialog(PlayListActivity.this);
                        dialog.setMessage("loading...");
                        dialog.setCancelable(false);
                        dialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DataLogic.addMusic(folder);
                                dialog.dismiss();
                                updateUI();
                            }
                        }).start();
                    }
                });
                pickDialog.show();
            }
        });
        ImageButton btnAddMusicFile = findViewById(R.id.add_music_file);
        btnAddMusicFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileDir = new File(DataLogic.getMusicHome());
                FilePickDialog filePickDialog = new FilePickDialog(PlayListActivity.this, fileDir);
                filePickDialog.setOnFileSelectListener(new FilePickDialog.OnFileSelectListener() {
                    @Override
                    public void onFileSelect(File file) {
                        //Toast.makeText(MainActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                        DataLogic.addMusic(file.getPath());
                        updateUI();
                    }
                });
                filePickDialog.show();
            }
        });
    }

    public void onClickPlayListHome() {
        final TextView tv = new TextView(this);
        String text = "This will clear current list and load home("+ DataLogic.getMusicHome() +"), are you sure?";
        tv.setText(text);
        tv.setPadding(10, 10, 10, 10);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);
        new AlertDialog
                .Builder(this)
                .setTitle("Confirm Home?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(tv)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog dialog = new ProgressDialog(PlayListActivity.this);
                        dialog.setMessage("loading, please wait...");
                        dialog.setCancelable(false);
                        dialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DataLogic.loadHomeList();
                                dialog.dismiss();
                                updateUI();
                            }
                        }).start();
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    public void onClickPlayListClear() {
        final TextView tv = new TextView(this);
        tv.setText("Are you sure to clear current play list? ");
        tv.setPadding(10, 10, 10, 10);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);
        new AlertDialog
                .Builder(this)
                .setTitle("Confirm Clear?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(tv)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DataLogic.clearPlayList();
                        updateUI();
                    }
                }).setNegativeButton("Cancel", null).show();
    }

    public void onClickPlayListSort() {
        DataLogic.sortPlayList();
        updateUI();
    }

    public void updateUI() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PlayListAdapter.onDataChanged();
                setToolbarTitle();
            }
        });
    }

    public void setToolbarTitle() {
        String info = ComDef.TEXT_PLAY_LIST + DataLogic.getPlayListInfo();
        ((TextView)findViewById(R.id.toolbar_title)).setText(info);
    }
}
