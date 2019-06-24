package com.cloudminds.feedback.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudminds.feedback.R;
import com.cloudminds.feedback.bean.CountFooterViewHolder;
import com.cloudminds.feedback.bean.CountHeaderViewHolder;
import com.cloudminds.feedback.bean.CountItemViewHolder;
import com.cloudminds.feedback.bean.Section;
import com.cloudminds.feedback.bean.Sections;
import com.cloudminds.feedback.bean.SystemItem;

import java.util.HashMap;

/**
 * Created by root on 17-7-13.
 */

public class CountSectionAdapter extends SectionedRecyclerViewAdapter<CountHeaderViewHolder,
        CountItemViewHolder,CountFooterViewHolder> {
    private LayoutInflater layoutInflater;
    private Sections mAllSections;
    private View.OnClickListener mOnclickListener;
    private View.OnTouchListener mOnTouchListener;
    private HashMap<String,Integer>mItemImageMap = new HashMap<String,Integer>();
    private HashMap<String,SystemItem> mItemDrawableMap;
    @Override
    protected int getItemCountForSection(int section) {
        Section s = mAllSections.getSections().get(section);
        return s.getItems().size();
    }

    public HashMap<String,SystemItem> getItemDrawableMap() {
        return mItemDrawableMap;
    }

    @Override
    protected int getSectionCount() {
        return mAllSections.getSections().size();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    protected LayoutInflater getLayoutInflater(){
        return layoutInflater;
    }


    public Sections getAllSections(){
        return mAllSections;
    }

    public CountSectionAdapter(LayoutInflater layoutInflater , Sections alldata  , View.OnClickListener mOnclickListener, View.OnTouchListener onTouchListener,
                               HashMap<String,SystemItem> mItemDrawableMap) {
        this.layoutInflater = layoutInflater;
        this.mAllSections = alldata;
        this.mOnclickListener = mOnclickListener;
        this.mOnTouchListener = onTouchListener;
        this.mItemDrawableMap = mItemDrawableMap;
        initItemImageMap();
    }

    @Override
    protected CountHeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_header, parent,false);
        CountHeaderViewHolder countHeaderViewHolder = new CountHeaderViewHolder(view);
        return countHeaderViewHolder;
    }

    @Override
    protected CountItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_item, parent,false);
        CountItemViewHolder countItemViewHolder = new CountItemViewHolder(view);
        view.setOnClickListener(mOnclickListener);
        view.setOnTouchListener(mOnTouchListener);
        return countItemViewHolder;
    }

    @Override
    protected CountFooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_footer, parent, false);
        return new CountFooterViewHolder(view);
    }

    @Override
    protected void onBindSectionHeaderViewHolder(CountHeaderViewHolder holder, int section) {
        String sectionName = mAllSections.getSections().get(section).getSectionName();

        holder.render(sectionName);
    }

    @Override
    protected void onBindItemViewHolder(CountItemViewHolder holder, int section, int position) {
        String itemName = mAllSections.getSections().get(section).getItems().get(position).getName();
        String itemImageKey = mAllSections.getSections().get(section).getItems().get(position).getImageKey();
        SystemItem systemItem = mItemDrawableMap.get(itemName);
        Drawable icon = null;
        if(systemItem != null){
            icon = systemItem.getIcon();
        }
        if(icon != null){
            holder.render(itemName, icon);
        }else{
            Integer itemImage = mItemImageMap.get(itemImageKey);
            int itemIcon = (itemImage==null ? R.mipmap.ic_launcher_feedback : itemImage.intValue());

            holder.render(itemName, itemIcon);
        }

    }

    @Override
    protected void onBindSectionFooterViewHolder(CountFooterViewHolder holder, int section) {
        //holder.render("Footer " + (section + 1));
    }

    private void initItemImageMap(){
        mItemImageMap.put("CrashAndReboot",R.mipmap.ic_crash_and_reboot);
        mItemImageMap.put("PowerAndHeat",R.mipmap.ic_power_and_heat);
        mItemImageMap.put("ChargingException",R.mipmap.ic_charging_exception);
        mItemImageMap.put("SlowingAndHanging",R.mipmap.ic_slowing_and_hanging);
        mItemImageMap.put("FingerPrint",R.mipmap.ic_finger_print);
        mItemImageMap.put("Iris",R.mipmap.ic_iris);
        mItemImageMap.put("Bluetooth",R.mipmap.ic_bluetooth);
        mItemImageMap.put("PressButton",R.mipmap.ic_press_button);
        mItemImageMap.put("GPS",R.mipmap.ic_gps);
        mItemImageMap.put("Peripheral",R.mipmap.ic_peripheral);
        mItemImageMap.put("MobileNetwork",R.mipmap.ic_mobile_network);
        mItemImageMap.put("WI-FI",R.mipmap.ic_wifi);
        mItemImageMap.put("SystemUpgrade",R.mipmap.ic_system_upgrade);
        mItemImageMap.put("Dialer",R.mipmap.ic_dialer);
        mItemImageMap.put("Contacts",R.mipmap.ic_contacts);
        mItemImageMap.put("SMS/MMS",R.mipmap.ic_sms_mms);
        mItemImageMap.put("DomainSwitching",R.mipmap.ic_domain_switching);
        mItemImageMap.put("Camera",R.mipmap.ic_camera);
        mItemImageMap.put("Gallery",R.mipmap.ic_gallery);
        mItemImageMap.put("Calendar",R.mipmap.ic_calendar);
        mItemImageMap.put("FileManager",R.mipmap.ic_file_manager);
        mItemImageMap.put("OtherApplications",R.mipmap.ic_other_applications);
        mItemImageMap.put("OtherMistakesAndOpinions",R.mipmap.ic_other_opinions);
        mItemImageMap.put("Hardware",R.mipmap.ic_hardware);
        mItemImageMap.put("4View",R.mipmap.ic_camera_4v);
        mItemImageMap.put("Launcher",R.mipmap.ic_launcher);
        mItemImageMap.put("RedPlayer",R.mipmap.ic_redplayer);
        mItemImageMap.put("LeiaLoft",R.mipmap.ic_leialoft);
        mItemImageMap.put("HydrogenNetwork",R.mipmap.ic_rednetwork);
        mItemImageMap.put("StatusBar",R.mipmap.ic_statusbar);
        mItemImageMap.put("Wallpaper",R.mipmap.ic_wallpaper);
        mItemImageMap.put("Lookscreen",R.mipmap.ic_lookscreen);
        mItemImageMap.put("Settings",R.mipmap.ic_settings);
        mItemImageMap.put("Audio",R.mipmap.ic_audio);

    }
}
