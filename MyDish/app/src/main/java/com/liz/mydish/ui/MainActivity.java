package com.liz.mydish.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import com.liz.mydish.R;
import com.liz.mydish.data.MenuListAdapter;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private MenuListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.lv_menu_items);
        mAdapter = new MenuListAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.addFooterView(new ViewStub(this));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

            }
        });
    }
}
