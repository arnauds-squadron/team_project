package com.arnauds_squadron.eatup;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.arnauds_squadron.eatup.chat.ChatFragment;
import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.navigation.MainFragmentPagerAdapter;
import com.arnauds_squadron.eatup.utils.Constants;
import com.google.android.libraries.places.api.Places;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        HomeFragment.OnFragmentInteractionListener,
        LocalFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener {

    @BindView(R.id.frameLayout)
    ViewPager viewPager;

    @BindView(R.id.tab_bar)
    TabLayout tabLayout;

    // Chat selected in the HomeFragment, stored to be accessed by the ChatFragment
    private Chat chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            gotoLoginActivity();
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager.setAdapter(new MainFragmentPagerAdapter(getSupportFragmentManager()));

        // All the tabs in this viewpager will be loaded (4 neighboring tabs)
        viewPager.setOffscreenPageLimit(4);
        viewPager.setCurrentItem(Constants.MAIN_PAGER_START_PAGE);

        // Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        // TODO: change visitor meal icon, profile icon, and chat icon
        int[] icons = {
                R.drawable.chat_tab,
                R.drawable.host_create_tab,
                R.drawable.home_tab,
                R.drawable.visitor_meal_tab,
                R.drawable.profile_tab
        };

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }

        Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
    }

    /**
     * Handle back button pressed so it goes through the fragment stack first before going
     * through the activity stack. Checks the selected index and notifies the selected fragment
     * to handle the back pressed if needed
     */
    @Override
    public void onBackPressed() {
        int currentFragmentIndex = viewPager.getCurrentItem();

        if(currentFragmentIndex == 0) { // chat fragment
            ChatFragment chatFragment = (ChatFragment) getTypedFragment(ChatFragment.class);

            if (!chatFragment.onBackPressed())
                finish();

        } else if(currentFragmentIndex == 1) { // local fragment
            LocalFragment localFragment = (LocalFragment) getTypedFragment(LocalFragment.class);

            if (!localFragment.onBackPressed())
                finish();

        } else {
            super.onBackPressed();
        }
    }

    /**
     * Overrides the LocalFragment interface
     *
     * Switches to the HomeFragment when the user finishes creating the event
     */
    @Override
    public void switchToHomeFragment() {
        viewPager.setCurrentItem(Constants.MAIN_PAGER_START_PAGE);
    }

    /**
     * Overrides the HomeFragment interface
     *
     * Switches to the ChatFragment when the user clicks on a chat
     */
    @Override
    public void switchToChatFragment(Chat chat) {
        this.chat = chat;
        viewPager.setCurrentItem(0);
    }

    /**
     * Overrides the ChatFragment interface
     *
     * Accessor for the chat object, DELETES the local copy of chat. Chat should not be null only
     * when the user just clicked on an event's chat in the HomeFragment
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

    private Fragment getTypedFragment(Class clazz) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        for (Fragment fragment : fragments) {
            if (fragment.getClass().equals(clazz)) {
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
