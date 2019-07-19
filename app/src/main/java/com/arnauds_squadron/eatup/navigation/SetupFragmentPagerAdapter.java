package com.arnauds_squadron.eatup.navigation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.arnauds_squadron.eatup.local.setup.AddressFragment;
import com.arnauds_squadron.eatup.local.setup.DateFragment;
import com.arnauds_squadron.eatup.local.setup.FoodTypeFragment;
import com.arnauds_squadron.eatup.local.setup.ReviewFragment;
import com.arnauds_squadron.eatup.local.setup.StartFragment;

/**
 * Pager Adapter to handle all the setup fragments we need to create an event
 */
public class SetupFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 5;

    public SetupFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return StartFragment.newInstance();
        if(position == 1)
            return AddressFragment.newInstance();
        if (position == 2)
            return FoodTypeFragment.newInstance();
        if (position == 3)
            return DateFragment.newInstance();
        if (position == 4)
            return ReviewFragment.newInstance();
        return StartFragment.newInstance();
    }
}