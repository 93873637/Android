package com.cloudminds.feedback.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.adapter.CountSectionAdapter;
import com.cloudminds.feedback.adapter.SectionedSpanSizeLookup;
import com.cloudminds.feedback.bean.CountItemViewHolder;
import com.cloudminds.feedback.bean.Sections;
import com.cloudminds.feedback.bean.SystemItem;
import com.cloudminds.feedback.utils.FileUtils;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by root on 17-7-13.
 */

public class ChooseModuleActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{
    private final static int SPAN_COUNT = 4;

    private RecyclerView recycler;
    private Toolbar toolbar;
    private CountSectionAdapter countSectionAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_module_activity);
        intView();
    }

    public  boolean isZh() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    private Sections loadData(boolean show,HashMap<String,SystemItem> mItemDrawableMap) {
        return FileUtils.getDefaultJson(this, (show && isZh()) ? "default.json" : "en_default.json",mItemDrawableMap);
    }

    private void intView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton imgBtn=toolbar.findViewById(R.id.left_img_btn);
        TextView textView=toolbar.findViewById(R.id.tool_bar_title);
        textView.setText(R.string.choose_module);
        imgBtn.setBackgroundResource(R.drawable.ic_toolbar_back);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        HashMap<String,SystemItem> mItemDrawableMap = new HashMap<>();
        Sections sections = loadData(true,mItemDrawableMap);
        countSectionAdapter = new CountSectionAdapter(LayoutInflater.from(this), sections, this, this,mItemDrawableMap);
        recycler = findViewById(R.id.recycler);
        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT,GridLayoutManager.VERTICAL,false);
        SectionedSpanSizeLookup lookup = new SectionedSpanSizeLookup(countSectionAdapter, layoutManager);

        layoutManager.setSpanSizeLookup(lookup);
        recycler.setAdapter(countSectionAdapter);
        recycler.setLayoutManager(layoutManager);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof CountItemViewHolder) {
            String moduleStringForUI = ((CountItemViewHolder) v.getTag()).textView.getText().toString();
            int itemId = ((CountItemViewHolder) v.getTag()).getAdapterPosition();
            int section = countSectionAdapter.getSectionForPosition(itemId);
            int position = countSectionAdapter.getPositionWithinSection(itemId);
            String moduleStringForServer = countSectionAdapter.getAllSections().getSections().get(section).getItems().get(position).getName();
            SystemItem systemItem = countSectionAdapter.getItemDrawableMap().get(moduleStringForServer);
            if(systemItem != null){
                moduleStringForServer = systemItem.getPackName();
            }
            if(systemItem == null){//is system index
                Sections sections = loadData(false,null);//need en model name
                moduleStringForServer = sections.getSections().get(section).getItems().get(position).getName();
                moduleStringForServer = FileUtils.moduleToPackageName(moduleStringForServer);

            }
            Intent intent = new Intent(moduleStringForUI);
            intent.putExtra("module_en", moduleStringForServer);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private View mAnimationView;
    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(mAnimationView!=null){
                    mAnimationView.setScaleX(1);
                    mAnimationView.setScaleY(1);
                }
                mAnimationView = v;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return false;
    }
}
