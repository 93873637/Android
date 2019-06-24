package com.cloudminds.feedback.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cloudminds.feedback.R;
/**
 * add by jordan.jing
 */
public class UserFeedbackDetailActivity extends Activity implements View.OnClickListener{
    private static final String ACTION_SETUP_WIZARD = "com.android.setupwizard.USER_FEEDBACK";
    private Toolbar mToolbarSet;
    private ImageButton mImgBtnReturn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String action = getIntent().getAction();
        if (ACTION_SETUP_WIZARD.equals(action)) {
            setTheme(R.style.SetupWizardDetailTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_feedback_statement);
        mToolbarSet = findViewById(R.id.toolbar);
        mImgBtnReturn = mToolbarSet.findViewById(R.id.left_img_btn);
        mImgBtnReturn.setBackgroundResource(R.drawable.ic_return);
        mImgBtnReturn.setOnClickListener(this);
        TextView tvTitle = mToolbarSet.findViewById(R.id.tool_bar_title);
        tvTitle.setText(R.string.user_experience_text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_img_btn:
                finish();
                break;
    }
    }
}
