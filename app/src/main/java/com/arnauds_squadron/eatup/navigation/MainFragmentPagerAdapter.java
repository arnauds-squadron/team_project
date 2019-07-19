package com.arnauds_squadron.eatup.navigation;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.visitor.VisitorFragment;

/**
 * Pager Adapter to handle the 3 main fragments we have in the MainActivity
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;

    // TODO: possibly remove?
    private String tabTitles[] = new String[] { "Tab1", "Tab2", "Tab3" };

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return LocalFragment.newInstance();
        if (position == 1)
            return HomeFragment.newInstance();
        if (position == 2)
            return VisitorFragment.newInstance();
        return HomeFragment.newInstance();
    }

    // TODO: Remove unnecessary titles?
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return null;
    }
}