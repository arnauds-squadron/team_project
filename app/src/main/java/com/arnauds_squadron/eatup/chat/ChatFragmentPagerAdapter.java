package com.arnauds_squadron.eatup.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.arnauds_squadron.eatup.chat.dashboard.ChatDashboardFragment;

/**
 * Pager Adapter to handle all the setup fragments we need to create an event
 */
public class ChatFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;

    public ChatFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return ChatDashboardFragment.newInstance();
        if(position == 1)
            return ChatFragment.newInstance();
        return ChatDashboardFragment.newInstance();
    }
}