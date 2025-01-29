package com.thezec.coffeein.Models;

/**
 * Created by Aleksandar on 9/18/2014.
 */
public class NavigationDrawerListModel {


    private String title;
    private int icon;
    // boolean to set visibility of the counter
    private boolean isCounterVisible = false;

    public NavigationDrawerListModel() {
    }

    public NavigationDrawerListModel(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public NavigationDrawerListModel(String title, int icon, boolean isCounterVisible) {
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = isCounterVisible;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean getCounterVisibility() {
        return this.isCounterVisible;
    }

    public void setCounterVisibility(boolean isCounterVisible) {
        this.isCounterVisible = isCounterVisible;
    }
}
