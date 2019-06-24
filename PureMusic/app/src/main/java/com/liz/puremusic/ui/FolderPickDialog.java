package com.liz.puremusic.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.liz.puremusic.R;
import com.liz.puremusic.utils.LogUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FolderPickDialog extends FilePickDialog implements OnItemClickListener {

    public FolderPickDialog(Context context, File filePath) {
        super(context, filePath);

        Button btnSelect = findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("FolderPickDialog: on select, mLastSelectedPos=" + mLastSelectedPos);
                if (mSelectListener == null) {
                    Toast.makeText(FolderPickDialog.this.getContext(), "No select listener", Toast.LENGTH_LONG).show();
                }
                else {
                    if (mLastSelectedPos == -1) {
                        Toast.makeText(FolderPickDialog.this.getContext(), "Please click to select a folder", Toast.LENGTH_LONG).show();
                    }
                    else {
                        File file = (File) mFileDataList.get(mLastSelectedPos).get(FILE);
                        mSelectListener.onFileSelect(file);
                        FolderPickDialog.this.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void setLayoutView() {
        setContentView(R.layout.dialog_folder_piker);
    }

    @Override
    protected void addToFileList(File file) {
        //only add file path
        if (file != null && file.isDirectory()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ICON, R.drawable.folder);
            map.put(NAME, file.getName());
            map.put(FILE, file);
            map.put(ROOT, false);
            map.put(SUBS, "" + file.listFiles().length);
            mFileDataList.add(map);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object fileObj = mFileDataList.get(position).get(FILE);
        if (fileObj == null) {
            LogUtils.e("ERROR: FolderPickDialog.onItemClick: no file object for click");
            return;
        }

        File file = (File) fileObj;
        if (isParentDir(mFileDataList.get(position))) {
            LogUtils.d("FolderPickDialog.onItemClick: go to parent dir");
            openDir(file);
            return;
        }

        LogUtils.d("FolderPickDialog.onItemClick: position=" + position + ", mLastSelectedPos=" + mLastSelectedPos);
        if (position == mLastSelectedPos) {
            //second click on the same item, means entering the dir
            openDir(file);
            return;
        }

        //first click on the folder
        if (mLastSelectedPos >= 0  && mLastSelectedPos < mFileDataList.size()) {
            mFileDataList.get(mLastSelectedPos).put(PICK, false);
        }
        mFileDataList.get(position).put(PICK, true);
        mLastSelectedPos = position;
        mFilePickListAdapter.notifyDataSetChanged();
    }
}
