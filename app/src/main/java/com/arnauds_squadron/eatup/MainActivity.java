package com.arnauds_squadron.eatup;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.arnauds_squadron.eatup.chat.ChatFragment;
import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.login.LoginActivity;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.navigation.MainFragmentPagerAdapter;
import com.arnauds_squadron.eatup.profile.ProfileFragment;
import com.arnauds_squadron.eatup.utils.Constants;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.OnFragmentInteractionListener,
        LocalFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {

    @BindView(R.id.flNoEventsScheduled)
    ViewPager viewPager;

    @BindView(R.id.tab_bar)
    TabLayout tabLayout;

    // Chat selected in the HomeFragment, stored to be accessed by the ChatFragment
    private Chat chat;

    private MainFragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            gotoLoginActivity();
        }

        pagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), this);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager.setAdapter(pagerAdapter);

        // All the tabs in this viewpager will be loaded (4 neighboring tabs)
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        // Start on the HomeFragment
        viewPager.setCurrentItem(Constants.HOME_FRAGMENT_INDEX);

        // Set the correct keyboard layout for the MessengerFragment
        // (show the toolbar while typing)
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                getWindow().setSoftInputMode(i == 0 ?
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE :
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(pagerAdapter.getTabView(i, null));
        }

        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        tab.getCustomView().setAlpha(1);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        tab.getCustomView().setAlpha(0.75f);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
    }

    /**
     * Handle back button pressed so it goes through the fragment stack first before going
     * through the activity stack. Checks the selected index and notifies the selected fragment
     * to handle the back pressed if needed
     */
    @Override
    public void onBackPressed() {
        int currentFragmentIndex = viewPager.getCurrentItem();

        if (currentFragmentIndex == 0) { // chat fragment
            ChatFragment chatFragment = (ChatFragment) getTypedFragment(ChatFragment.class);

            if (!chatFragment.onBackPressed())
                finish();

        } else if (currentFragmentIndex == 1) { // local fragment
            LocalFragment localFragment = (LocalFragment) getTypedFragment(LocalFragment.class);

            if (!localFragment.onBackPressed())
                finish();

        } else {
            super.onBackPressed();
        }
    }

    /**
     * Overrides the LocalFragment interface
     * <p>
     * Switches to the HomeFragment when the user finishes creating the event
     */
    @Override
    public void onEventCreated() {
        navigateToFragment(Constants.HOME_FRAGMENT_INDEX);

        ChatFragment chatFragment = (ChatFragment) getTypedFragment(ChatFragment.class);

        if (chatFragment != null)
            chatFragment.updateDashboardChats();
    }

    /**
     * Overrides the HomeFragment interface
     * <p>
     * Switches to the ChatFragment when the user clicks on a chat
     */
    @Override
    public void switchToChatFragment(Chat chat) {
        this.chat = chat;
        navigateToFragment(0);
    }

    @Override
    public void navigateToFragment(int index) {
        viewPager.setCurrentItem(index);
    }

    /**
     * Overrides the ChatFragment interface
     * <p>
     * Accessor for the chat object, DELETES the local copy of chat. Chat should not be null only
     * when the user just clicked on an event's chat in the HomeFragment
     *
     * @return The chat object selected through the HomeFragment
     * @effects Deletes the local copy of the chat
     */
    @Override
    public Chat getSelectedChat() {
        Chat temp = chat;
        chat = null;
        return temp;
    }

    @Override
    public void updateMessageNotifications(int notifications) {
        tabLayout.getTabAt(0).setCustomView(pagerAdapter.getTabView(0, notifications + ""));
    }

    /**
     * Overrides the ProfileFragment interface
     * <p>
     * Once the user logs out the Constants.CURRENT_USER object is null, so there is no point in
     * searching for any new events until the user logs in again
     */
    @Override
    public void stopUpdatingEvents() {
        HomeFragment homeFragment = (HomeFragment) getTypedFragment(HomeFragment.class);
        ChatFragment chatFragment = (ChatFragment) getTypedFragment(ChatFragment.class);
        homeFragment.stopUpdatingEvents();
        chatFragment.stopUpdatingMessages();
    }

    /**
     * Given the class of a specific fragment (ProfileFragment or HomeFragment), this method
     * searches through the child Fragments and returns the given fragment if found, or null if it's
     * not found
     *
     * @param fragmentClass The class of the fragment you are trying to find
     * @return The specified fragment with the matching class, or null if the fragment does not
     * exist
     */
    private Fragment getTypedFragment(Class fragmentClass) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            if (fragment.getClass().equals(fragmentClass)) {
                return fragment;
            }
        }
        return null;
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
