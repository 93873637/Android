package com.cloudminds.feedback.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.logic.ComDef;
import com.cloudminds.feedback.logic.Settings;
import com.cloudminds.feedback.utils.ComUtils;

public class SelectUserTypeDialog extends Dialog {


    public SelectUserTypeDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private CheckBox testCheckBox;
        private View layout;
        private Button saveBtn;
        private SelectUserTypeDialog dialog;
        private DialogSaveListener mDialogSaveListener;
        private Context context;
        public Builder(Context context,DialogSaveListener mDialogSaveListener) {
            this.context = context;
            this.mDialogSaveListener = mDialogSaveListener;
            //这里传入自定义的style，直接影响此Dialog的显示效果。style具体实现见style.xml
            dialog = new SelectUserTypeDialog(context, R.style.Dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.select_user_dialog, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        }

        /**
         * 创建双按钮对话框
         *
         * @return
         */
        public SelectUserTypeDialog createDialog() {
            create();
            return dialog;
        }

        /**
         * 单按钮对话框和双按钮对话框的公共部分在这里设置
         */
        private void create() {
            testCheckBox = (CheckBox) layout.findViewById(R.id.test_user);
            saveBtn = (Button) layout.findViewById(R.id.save_btn);
            String user = Settings.getUserType(context);
            if(!TextUtils.isEmpty(user) && user.equals(Settings.TEST_USER)){
                testCheckBox.setChecked(true);
            }else{
                testCheckBox.setChecked(false);
            }
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(testCheckBox.isChecked()){
                        Settings.setUserType(context,Settings.TEST_USER);
                    }else{
                        Settings.setUserType(context,Settings.NORMAL_USER);
                    }
                    ComUtils.setUploadUrl(Settings.getUserType(context));
                    if(mDialogSaveListener != null){
                        mDialogSaveListener.setUserFeedback();
                    }
                    dialog.dismiss();
                    dialog = null;
                }
            });

            dialog.setContentView(layout);
            dialog.setCancelable(true);     //用户可以点击手机Back键取消对话框显示
            dialog.setCanceledOnTouchOutside(false);        //用户不能通过点击对话框之外的地方取消对话框显示
            dialog.show();
        }


    }
}
