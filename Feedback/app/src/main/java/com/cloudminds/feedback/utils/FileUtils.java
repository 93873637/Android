package com.cloudminds.feedback.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.feedback.bean.Item;
import com.cloudminds.feedback.bean.Section;
import com.cloudminds.feedback.bean.Sections;
import com.cloudminds.feedback.bean.SystemItem;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FileUtils {
    public static final String TAG = "FileUtils";

    public static final int SYSTEMSESSIONINDEX = 2;//system app index
    public static Sections getDefaultJson(Context context , String content,HashMap<String,SystemItem> mItemDrawableMap){
        StringBuilder builder = null;
        Sections Sections =null;

        try {
            InputStreamReader open = new InputStreamReader(context.getAssets().open(content),"UTF-8");
            BufferedReader br = new BufferedReader(open);
            String line;
            builder= new StringBuilder();
            while((line = br.readLine()) != null){
                builder.append(line);
            }
            br.close();
            open.close();
        }catch (IOException e){
            Log.e("IOException",e.toString());
        }
        String s = builder == null ? null : builder.toString();
        if(s!=null){
            Gson goGson =new Gson();

            Sections = goGson.fromJson(s, Sections.class);
        }


/*
        if(mItemDrawableMap != null){
            String systemSectionName = context.getResources().getString(R.string.system_application_section_name);
            Section systemSection = null;
            for(Section sections :Sections.getSections()){
               if(sections.sectionName.equals(systemSectionName)){
                   systemSection = sections;
                   break;
               }
            }
            Section newSystemSection = createSystemApplicationSection(getSystemApp(context,mItemDrawableMap),systemSectionName);
            if(newSystemSection != null){
                if(systemSection != null){
                    if(newSystemSection.items != null)
                        systemSection.setItems(newSystemSection.items);
                }else{
                    if(Sections.getSections().size() > SYSTEMSESSIONINDEX){
                        Sections.getSections().add(SYSTEMSESSIONINDEX,newSystemSection);
                    }else{
                        Sections.getSections().add(newSystemSection);
                    }
                }
            }
        }
*/


        return Sections;
    }
    public static String moduleToPackageName(String modulename) {
        for(int i = 0;i < GSON_APP_NAME.length;i++){
            if(GSON_APP_NAME[i].equals(modulename)){
                return GSON_PACKAGE_NAME[i];
            }
       }
        return modulename;
    }

    private static final String[] GSON_APP_NAME = {
            "StatusBar",
            "Launcher",
            "Wallpaper",
            "Lookscreen",
            "Red Player",
            "LeiaLoft",
            "Hydrogen Network",
            "Camera",
            "Settings",
            "Dialer",
            "FileManager",
    };

    private static final String[] GSON_PACKAGE_NAME = {
            "com.android.systemui",
            "com.cloudminds.launcher3",
            "com.android.wallpaper.livepicker",
            "com.android.systemui",
            "com.leialoft.redmediaplayer",
            "com.leia.leialoft",
            "com.red.hydrogen",
            "com.cloudminds.camera",
            "com.android.settings",
            "com.android.dialer",
            "com.cloudminds.filemanager",

    };

    private static final String[] APP_NAME = {
            "statusBar",
            "launcher",
            "wallpaper",
            "lookscreen",
    };

    private static final String[] PACKAGE_NAME = {
            "com.android.systemui",
            "com.cloudminds.launcher3",
            "com.android.wallpaper.livepicker",
            "com.android.systemui",
    };

    private static final String[] RES_ICON_NAME = {
            "com_android_systemui",
            "com_cloudminds_launcher3",
            "com_android_wallpaper_livepicker",
            "com_android_systemui_lookscreen",
    };

    public static int getResIconId(Context context, String iconName){
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    public static int getResNameId(Context context, String iconName){
        return context.getResources().getIdentifier(iconName, "string", context.getPackageName());
    }

    public static Section createSystemApplicationSection(ArrayList<Item> list,String name){
        Section section = new Section();
        section.setSectionName(name);
        section.setItems(list);
        return section;
    }

    public static ArrayList getSystemApp(Context context, HashMap<String,SystemItem> itemDrawableMap){
        PackageManager mPackageManager = context.getApplicationContext().getPackageManager();
        LauncherApps  mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        UserManager mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        List<UserHandle> users = mUserManager.getUserProfiles();
        List<UserHandle> profiles= users == null ? Collections.<UserHandle>emptyList() : users;

        ArrayList<Item> list = new ArrayList<>();
        for(int i = 0;i < APP_NAME.length;i++){
            Item model = new Item();
            String name = context.getResources().getString(getResNameId(context,APP_NAME[i]));
            model.setName(name);
            SystemItem systemItem = new SystemItem();
            systemItem.setItemName(name);
            systemItem.setPackName(PACKAGE_NAME[i]);
            systemItem.setIcon(context.getDrawable(getResIconId(context,RES_ICON_NAME[i])));
            itemDrawableMap.put(model.itemName,systemItem);
            list.add(model);
        }
        for (UserHandle user : profiles) {
            // Query for the set of apps
            final List<LauncherActivityInfo> apps = mLauncherApps.getActivityList(null, user);
            // Fail if we don't have any apps
            // TODO: Fix this. Only fail for the current user.
            if (apps == null || apps.isEmpty()) {
                continue;
            }
            // Create the ApplicationInfos
            for (int i = 0; i < apps.size(); i++) {
                LauncherActivityInfo app = apps.get(i);
                // This builds the icon bitmaps.
                ComponentName componentName = app.getComponentName();
                String appName =getSystemApplicationName(componentName.getPackageName(),mPackageManager);
                if(!TextUtils.isEmpty(appName)){
                    Item model = new Item();
                    model.setName(appName);
                    SystemItem systemItem = new SystemItem();
                    systemItem.setItemName(appName);
                    systemItem.setPackName(componentName.getPackageName());
                    systemItem.setIcon(app.getIcon(0));
                    itemDrawableMap.put(model.itemName,systemItem);
                    list.add(model);
                }
            }
        }
        return list;
    }

    public static String getSystemApplicationName(String packageName, PackageManager packageManager) {
        String applicationName = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            //filter system app
            if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 ||
                    (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {

        }
        return applicationName;
    }

    /**
     * Format As:
     * Log_IMEI_YYMMDD_hhmmss.zip
     * Log_862851030258951_180122_161632.zip
     */
    public static String genLogZipName(Context context) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHmmss");
        String strDateTime = format.format(new Date(System.currentTimeMillis()));
        return "log_" + SysUtils.getIMEI(context) + "_" + strDateTime + ".zip";
    }

    public static String getFormattedFileSize(String fileAbsolutePath) {
        return FormatFileSize(FileUtils.getFileSize(fileAbsolutePath));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static long getFileSize(String filePath) {
        long size = 0;
        try {
            File f = new File(filePath);
            if (f.isDirectory()) {
                size = getFolderSize(f);
            } else {
                size = getFileSize(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("getFileSize exception: " + e.toString());
        }
        return size;
    }

    private static long getFolderSize(File f) throws Exception {
        long size = 0;
        File fileList[] = f.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                size = size + getFolderSize(fileList[i]);
            } else {
                size = size + getFileSize(fileList[i]);
            }
        }
        return size;
    }

    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file != null && file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String FormatFileSize(long fileSize) {
        final long KB_V = 1024;   //real value
        final long KB_C = 1000;  //compare value, less than 4 degits
        final long MB_V = KB_V * KB_V;
        final long MB_C = KB_C * KB_C;
        final long GB_V = MB_V * KB_V;
        final long GB_C = MB_C * KB_C;

        String sizeString = "";
        DecimalFormat df = new DecimalFormat("#.0");
        if (fileSize <= 0) {
            LogUtils.w("FormatFileSize: wrong size " + fileSize);
        }
        else if (fileSize < KB_C) {
            sizeString = df.format((double) fileSize)+ " B";
        }
        else if (fileSize < MB_C) {
            sizeString = df.format((double) fileSize / KB_V) + " KB";
        }
        else if (fileSize < GB_C) {
            sizeString = df.format((double) fileSize / MB_V)+ " MB";
        }
        else {
            sizeString = df.format((double) fileSize / GB_V) + " GB";
        }

        return sizeString;
    }
}
