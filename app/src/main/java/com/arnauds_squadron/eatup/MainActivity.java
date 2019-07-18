package com.arnauds_squadron.eatup;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.navigation.MainFragmentPagerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        LocalFragment.OnFragmentInteractionListener {

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.tab_bar)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager.setAdapter(new MainFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        // Set home fragment as the first screen
        viewPager.setCurrentItem(1);

        // TODO: change visitor meal icon
        // Set icons to each tab
        int[] icons = {
                R.drawable.host_create_tab,
                R.drawable.home_tab,
                R.drawable.visitor_meal_tab,
        };

        for(int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }
    }

    /**
     * Handle back button pressed so it goes through the fragment stack first before going
     * through the activity stack
     */
    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment newest = fragments.get(fragments.size() - 1);

        if(!newest.getClass().equals(LocalFragment.class)) { // not in the local fragment
            super.onBackPressed();
        } else {
            // Should be LocalFragment
            FragmentManager setupManager = newest.getChildFragmentManager();

            if (setupManager.getBackStackEntryCount() > 1) {
                setupManager.popBackStackImmediate();
            } else {
                finish();
            }
        }
    }

    @Override
    public void addFragmentToStack(Fragment fragment) {


    }

    @Override
    public void removeFragmentFromStack(Fragment fragment) {

    }
}
