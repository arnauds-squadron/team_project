package com.arnauds_squadron.eatup.navigation;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.arnauds_squadron.eatup.HomeFragment;
import com.arnauds_squadron.eatup.R;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;

    private int[] icons = {
            R.drawable.ic_launcher_background
    };

    private String tabTitles[] = new String[] { "Tab1", "Tab2", "Tab3" };
    private Context context;

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return HomeFragment.newInstance(position + 1);
        return HomeFragment.newInstance(position + 1);
    }


    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return null;
    }
}