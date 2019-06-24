package com.liz.puremusic.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.liz.puremusic.R;
import com.liz.puremusic.adapter.FilePickListAdapter;
import com.liz.puremusic.logic.ComDef;
import com.liz.puremusic.logic.DataLogic;
import com.liz.puremusic.utils.LogUtils;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FilePickDialog extends Dialog implements OnItemClickListener {

    public FilePickDialog(Context context, File filePath) {
        super(context);
        onCreate();
        initData(filePath);
    }

    protected TextView mCurrentPathText;
    protected ListView mFileListView;

    //protected SimpleAdapter mFilePickListAdapter;
    protected FilePickListAdapter mFilePickListAdapter;
    protected List<Map<String, Object>> mFileDataList;
    protected OnFileSelectListener mSelectListener;

    protected static final String ICON = "icon";
    protected static final String NAME = "name";
    protected static final String FILE = "file";
    protected static final String ROOT = "root";
    protected static final String PICK = "pick";   //if picked or not
    protected static final String SUBS = "file_num";  //sub files number

    public static final int DEFAULT_LIST_BGCOLOR = Color.TRANSPARENT;
    protected int mLastSelectedPos = -1;

    protected void setLayoutView() {
        setContentView(R.layout.dialog_file_piker);
    }

    private void onCreate() {
        /*
        //create view directly without resource file
        mFileListView = new ListView(getContext());
        mFileListView.setAdapter(mFilePickListAdapter);
        mFileListView.setOnItemClickListener(this);
        setContentView(mFileListView);
        //*/

        //set ui by resource
        setLayoutView();

        //set show on bottom right
        Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.y = 60;  //distance from dialog bottom to screen bottom
//        lp.x = 6;  //distance from dialog right to screen right
//        dialogWindow.setAttributes(lp);

        mFileListView = findViewById(R.id.lv_items);
        mFileListView.setOnItemClickListener(this);

        mCurrentPathText = findViewById(R.id.tv_current_path);

        setCanceledOnTouchOutside(true);
    }

    private void initData(File filePath) {
        mCurrentPathText.setText(filePath.getPath());
        getChildrenList(filePath);
        //mFilePickListAdapter = new SimpleAdapter(getContext(), mFileDataList, R.layout.list_item_pick_file,
        //        new String[]{ICON, NAME}, new int[]{R.id.iv_file_type, R.id.tv_file_name});
        mFilePickListAdapter = new FilePickListAdapter(this.getContext(), mFileDataList);
        mFileListView.setAdapter(mFilePickListAdapter);
    }

    public void setOnFileSelectListener(OnFileSelectListener mSelectListener) {
        this.mSelectListener = mSelectListener;
    }

    protected void getChildrenList(File dir) {
        LogUtils.d("FilePickDialog.getChildrenList: dir=" + dir.getPath());
        File[] children = dir.listFiles();
        if (mFileDataList == null) {
            mFileDataList = new ArrayList<Map<String, Object>>();
        } else {
            mFileDataList.clear();
        }

        if (!DataLogic.isRoot(dir)) {
            LogUtils.d("FilePickDialog.getChildrenList: not root, add to parent dir...");
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ICON, R.drawable.folder);
            map.put(NAME, ComDef.PARENT_DIR);
            map.put(FILE, dir.getParentFile());
            map.put(ROOT, true);
            mFileDataList.add(map);
        }

        if (children == null) {
            return;
        }

        for (File file : children) {
            addToFileList(file);
        }

        //sort sub-files list
        Collections.sort(mFileDataList, new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> obj1, Map<String, Object> obj2) {
                int result = 0;

                if (isParentDir(obj1)) {
                    result = -1;
                } else if (isParentDir(obj2)) {
                    result = 1;
                } else {
                    File lhsFile = (File) obj1.get(FILE);
                    File rhsFile = (File) obj2.get(FILE);

                    if (lhsFile.isDirectory() && rhsFile.isFile()) {
                        result = -1;
                    } else if (lhsFile.isFile() && rhsFile.isDirectory()) {
                        result = 1;
                    } else {
                        Collator cmp = Collator.getInstance(Locale.getDefault());
                        result = cmp.compare(obj1.get(NAME), obj2.get(NAME));
                    }
                }
                return result;
            }
        });
    }

    protected boolean isParentDir(Map<String, Object> obj) {
        Object isParentObj = obj.get(ROOT);
        return ((isParentObj != null) && ((boolean) isParentObj));
    }

    protected void addToFileList(File file) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ICON, file.isDirectory()
                ? R.drawable.folder
                : R.drawable.file);
        map.put(NAME, file.getName());
        map.put(FILE, file);
        map.put(ROOT, false);
        mFileDataList.add(map);
    }

    public static int getIcon(Map<String, Object> map) {
        Object resId = map.get(ICON);
        if (resId == null) {
            return R.drawable.file;  //###@:
        }
        else {
            return (int)resId;
        }
    }

    public static String getFileInfo(Map<String, Object> map) {
        String fileName = (String)map.get(NAME);
        if (fileName == null) {
            fileName = "???";
        }
        String fileNum = (String)map.get(SUBS);
        if (TextUtils.isEmpty(fileNum)) {
            return fileName;
        }
        else {
            return fileName + "(" + fileNum + ")";
        }
    }

    public static boolean isPicked(Map<String, Object> map) {
        Object obj = map.get(PICK);
        if (obj == null) {
            return false;
        }
        else {
            return (boolean)obj;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = (File) mFileDataList.get(position).get(FILE);
        if (file.isDirectory()) {
            openDir(file);
        } else {
            if (mSelectListener != null) {
                mSelectListener.onFileSelect(file);
                this.dismiss();
            }
        }
    }

    protected void openDir(File dir) {
        mCurrentPathText.setText(dir.getPath());
        getChildrenList(dir);
        mLastSelectedPos = -1;
        mFilePickListAdapter.notifyDataSetChanged();
    }

    public interface OnFileSelectListener {
        void onFileSelect(File file);
    }
}
