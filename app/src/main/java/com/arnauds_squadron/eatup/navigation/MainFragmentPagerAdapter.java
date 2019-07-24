package com.arnauds_squadron.eatup.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.arnauds_squadron.eatup.chat.ChatFragment;
import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.profile.ProfileFragment;
import com.arnauds_squadron.eatup.visitor.VisitorFragment;

/**
 * Pager Adapter to handle the 3 main fragments we have in the MainActivity
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 5;

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return ChatFragment.newInstance();
        if(position == 1)
            return LocalFragment.newInstance();
        if (position == 2)
            return HomeFragment.newInstance();
        if (position == 3)
            return VisitorFragment.newInstance();
        if (position == 4)
            return ProfileFragment.newInstance();
        return HomeFragment.newInstance();
    }
}