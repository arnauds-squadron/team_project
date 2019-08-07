package com.arnauds_squadron.eatup.local;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.arnauds_squadron.eatup.local.setup.AddressFragment;
import com.arnauds_squadron.eatup.local.setup.DateFragment;
import com.arnauds_squadron.eatup.local.setup.YelpBusinessFragment;
import com.arnauds_squadron.eatup.local.setup.start.StartFragment;

/**
 * Pager Adapter to handle all the setup fragments we need to create an event
 */
public class SetupFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 4;

    SetupFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return new StartFragment();
        if (position == 1)
            return new AddressFragment();
        if (position == 2)
            return new YelpBusinessFragment();
        if (position == 3)
            return new DateFragment();
        return new StartFragment();
    }
}