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

        // Detect page switch and clear the back stack if the user switches to
        // a different fragment so the back button can exit the app
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}


            @Override
            public void onPageSelected(int i) {
                if(i != 0) {
                    LocalFragment localFragment = getLocalFragment();
                    if (localFragment != null) {
                        localFragment
                                .getChildFragmentManager()
                                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

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

        LocalFragment localFragment = getLocalFragment();

        if(localFragment == null) { // no local fragment
            super.onBackPressed();
        } else {
            FragmentManager setupManager = localFragment.getChildFragmentManager();

            // 1 on the stack means just the address tab is showing
            if (setupManager.getBackStackEntryCount() > 0) {
                setupManager.popBackStackImmediate();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onEventCreated() {
        viewPager.setCurrentItem(1);
    }

    private LocalFragment getLocalFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        LocalFragment localFragment = null;

        for (Fragment fragment : fragments) {
            if(fragment.getClass().equals(LocalFragment.class)) {
                localFragment = (LocalFragment) fragment;
            }
        }
        return localFragment;
    }
}
