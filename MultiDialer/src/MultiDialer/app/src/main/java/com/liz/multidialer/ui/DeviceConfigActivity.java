package com.liz.multidialer.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.liz.multidialer.R;
import com.liz.multidialer.logic.DataLogic;

public class DeviceConfigActivity extends AppCompatActivity {

    private EditText mEditDeviceId;
    private EditText mEditServerAddress;
    private EditText mEditUserName;
    private EditText mEditPassword;
    private EditText mEditNetworkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_config);

        this.setTitle("本机设置");

        mEditDeviceId = findViewById(R.id.edit_device_id);
        mEditServerAddress = findViewById(R.id.edit_server_address);
        mEditUserName = findViewById(R.id.edit_user_name);
        mEditPassword = findViewById(R.id.edit_password);
        mEditNetworkType = findViewById(R.id.edit_network_type);

        mEditDeviceId.setText(DataLogic.getDeviceId());
        mEditServerAddress.setText(DataLogic.getServerAddress());
        mEditUserName.setText(DataLogic.getUserName());
        mEditPassword.setText(DataLogic.getPassword());
        mEditNetworkType.setText(DataLogic.getNetworkType());

        findViewById(R.id.btn_update_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_config();
                DeviceConfigActivity.this.finish();
            }
        });
    }

    private void update_config() {
        DataLogic.setDeviceId(mEditDeviceId.getText().toString());
        DataLogic.setServerAddress(mEditServerAddress.getText().toString());
        DataLogic.setUserName(mEditUserName.getText().toString());
        DataLogic.setPassword(mEditPassword.getText().toString());
        DataLogic.setNetworkType(mEditNetworkType.getText().toString());
    }
}
