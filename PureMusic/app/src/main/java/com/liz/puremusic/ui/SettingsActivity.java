package com.liz.puremusic.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.liz.puremusic.R;
import com.liz.puremusic.logic.DataLogic;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private EditText mEditMusicHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        mEditMusicHome = findViewById(R.id.edit_music_home);
        mEditMusicHome.setText(DataLogic.getMusicHome());

        findViewById(R.id.select_music_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMusicHome();
            }
        });

        findViewById(R.id.btn_save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataLogic.setMusicHome(mEditMusicHome.getText().toString());
                Toast.makeText(SettingsActivity.this, "Configurations Saved", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btn_cancel_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });
    }

    private void selectMusicHome() {
        FolderPickDialog pickDialog = new FolderPickDialog(SettingsActivity.this,
                new File(DataLogic.getMusicHome()), "Set Home Dir");
        pickDialog.setOnFileSelectListener(new FilePickDialog.OnFileSelectListener() {
            @Override
            public void onFileSelect(File file) {
                mEditMusicHome.setText(file.getAbsolutePath());
            }
        });
        pickDialog.show();
    }
}
