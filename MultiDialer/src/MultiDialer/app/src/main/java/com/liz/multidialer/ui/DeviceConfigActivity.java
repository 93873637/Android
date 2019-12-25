package com.liz.multidialer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.liz.multidialer.R;
import com.liz.multidialer.logic.DataLogic;

public class DeviceConfigActivity extends AppCompatActivity {

    public static final int RESULT_CODE_UPDATE = 0;
    public static final int RESULT_CODE_CANCEL = 1;

    private EditText mEditDeviceId;
    private EditText mEditServerAddress;
    private EditText mEditServerPort;
    private EditText mEditUserName;
    private EditText mEditPassword;
    private EditText mEditNetworkType;
    private EditText mEditServerHome;
    private EditText mEditJpegQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_config);

        this.setTitle("本机设置");

        mEditDeviceId = findViewById(R.id.edit_device_id);
        mEditServerAddress = findViewById(R.id.edit_server_address);
        mEditServerPort = findViewById(R.id.edit_server_port);
        mEditUserName = findViewById(R.id.edit_user_name);
        mEditPassword = findViewById(R.id.edit_password);
        mEditNetworkType = findViewById(R.id.edit_network_type);
        mEditServerHome = findViewById(R.id.edit_server_home);
        mEditJpegQuality = findViewById(R.id.edit_jpeg_quality);

        mEditDeviceId.setText(DataLogic.getDeviceId());
        mEditServerAddress.setText(DataLogic.getServerAddress());
        mEditServerPort.setText(DataLogic.getServerPortInfo());
        mEditUserName.setText(DataLogic.getUserName());
        mEditPassword.setText(DataLogic.getPassword());
        mEditNetworkType.setText(DataLogic.getNetworkType());
        mEditServerHome.setText(DataLogic.getServerHome());
        mEditJpegQuality.setText(DataLogic.getJpegQualityInfo());

        findViewById(R.id.btn_update_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_config();
                setResult(RESULT_CODE_UPDATE, new Intent());
                DeviceConfigActivity.this.finish();
            }
        });
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CODE_CANCEL, new Intent());
                DeviceConfigActivity.this.finish();
            }
        });
    }

    private void update_config() {
        DataLogic.setDeviceId(mEditDeviceId.getText().toString());
        DataLogic.setServerAddress(mEditServerAddress.getText().toString());
        DataLogic.setServerPort(Integer.parseInt(mEditServerPort.getText().toString()));
        DataLogic.setUserName(mEditUserName.getText().toString());
        DataLogic.setPassword(mEditPassword.getText().toString());
        DataLogic.setNetworkType(mEditNetworkType.getText().toString());
        DataLogic.setServerHome(mEditServerHome.getText().toString());
        DataLogic.setJpegQuality(Integer.parseInt(mEditJpegQuality.getText().toString()));
    }
}
