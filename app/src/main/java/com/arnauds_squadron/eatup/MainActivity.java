package com.arnauds_squadron.eatup;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.arnauds_squadron.eatup.chat.MessengerFragment;
import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.navigation.MainFragmentPagerAdapter;
import com.google.android.libraries.places.api.Places;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.OnFragmentInteractionListener,
        LocalFragment.OnFragmentInteractionListener,
        MessengerFragment.OnFragmentInteractionListener {

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.tab_bar)
    TabLayout tabLayout;

    // Chat selected in the HomeFragment, stored to be accessed by the MessengerFragment
    private Chat chat;

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
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int index) {
                if (index != 0) { // switched to a fragment other than the local fragment
                    LocalFragment localFragment = getLocalFragment();
                    if (localFragment != null) {
                        localFragment.resetSetupViewPager();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        // Give the TabLayout the ViewPage
        tabLayout.setupWithViewPager(viewPager);

        // Set home fragment as the first screen
        viewPager.setCurrentItem(2);

        // TODO: change visitor meal icon, profile icon, and chat icon
        int[] icons = {
                R.drawable.chat_icon,
                R.drawable.host_create_tab,
                R.drawable.home_tab,
                R.drawable.visitor_meal_tab,
                R.drawable.profile_icon
        };

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }

        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
    }

    /**
     * Handle back button pressed so it goes through the fragment stack first before going
     * through the activity stack
     */
    @Override
    public void onBackPressed() {
        LocalFragment localFragment = getLocalFragment();

        if (localFragment == null) { // no local fragment
            super.onBackPressed();
        } else if (!localFragment.retreatViewPager()) {
            finish();
        }
    }

    /**
     * Overrides the LocalFragment interface
     *
     * Switches to the HomeFragment when the user finishes creating the event
     */
    @Override
    public void switchToHomeFragment() {
        viewPager.setCurrentItem(1);
    }

    /**
     * Overrides the HomeFragment interface
     *
     * Switches to the MessengerFragment when the user clicks on a chat
     */
    @Override
    public void switchToChatFragment(Chat chat) {
        this.chat = chat;
        viewPager.setCurrentItem(0);
    }

    /**
     * Overrides the MessengerFragment interface
     *
     * Accessor for the chat object, and also DELETES the local chat variable so the
     * MessengerFragment knows not to directly open a chat again
     *
     * @effects Deletes the local copy of the chat
     * @return The chat object selected through the HomeFragment
     */
    @Override
    public Chat getSelectedChat() {
        Chat temp = chat;
        chat = null;
        return temp;
    }

    private LocalFragment getLocalFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        LocalFragment localFragment = null;

        for (Fragment fragment : fragments) {
            if (fragment.getClass().equals(LocalFragment.class)) {
                localFragment = (LocalFragment) fragment;
            }
        }
        return localFragment;
    }
}
