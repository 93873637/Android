package com.liz.multidialer.ui;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.liz.multidialer.R;

public class DeviceConfigActivity extends AppCompatActivity {

    private EditText mEditDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_config);

        this.setTitle("Device Configurations");

        mEditDeviceId = findViewById(R.id.edit_device_id);
    }
}
