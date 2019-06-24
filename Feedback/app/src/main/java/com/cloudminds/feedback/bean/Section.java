package com.cloudminds.feedback.bean;

import java.util.ArrayList;

/**
 * Created by root on 17-7-14.
 */

public class Section {

    public String sectionName;
    public ArrayList <Item> items;

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String name) {
        this.sectionName = name;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}
