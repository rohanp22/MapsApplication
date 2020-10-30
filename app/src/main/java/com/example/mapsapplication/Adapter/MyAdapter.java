package com.example.mapsapplication.Adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.mapsapplication.Fragment.MyGallery;
import com.example.mapsapplication.Fragment.MyPlaces;

public class MyAdapter extends FragmentPagerAdapter {
    Context context;
    int totalTabs;
    public MyAdapter(Context c, FragmentManager fm, int totalTabs) {
        super(fm);
        this.context = c;
        this.totalTabs = totalTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MyPlaces();
            case 1:
                return new MyGallery();
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return totalTabs;
    }
}
