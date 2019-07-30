package com.arnauds_squadron.eatup;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.arnauds_squadron.eatup.chat.ChatFragment;
import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.login.LoginActivity;
import com.arnauds_squadron.eatup.models.Chat;
import com.arnauds_squadron.eatup.navigation.MainFragmentPagerAdapter;
import com.arnauds_squadron.eatup.utils.Constants;
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
    private static Location currentLocation;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                currentLocation = (Location) intent.getExtras().get("location");
        }
    };

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    private final int[] tabIcons = {
            R.drawable.chat_tab,
            R.drawable.host_create_tab,
            R.drawable.home_tab,
            R.drawable.visitor_meal_tab,
            R.drawable.profile_tab
    };

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

        // Start on the HomeFragment
        viewPager.setCurrentItem(Constants.MAIN_PAGER_START_PAGE);

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
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
        }
    }

    // TODO account for case when device policy or previous settings set permission
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        boolean shouldProvideRationale = shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("LocationFragment", "Displaying permission rationale to provide additional context.");
            showSnackbar("EatUp needs your current location to find hosts near you.", "Grant permission",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            // Request permission. Can be auto answered if device policy sets the permission
            // or the user denied permission previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i("LocationFragment", "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("LocationFragment", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                startService(new Intent(this, LocationService.class));
            } else {
                // Permission denied.
                // Notify the user that GPS is necessary to use the current location component of the app.
                // Permission might have been rejected without asking the user for permission
                // device policy or "Never ask again" prompts).
                // TODO add ignore functionality so user can continue without inputting current location
                showSnackbar("EatUp needs your current location to find hosts near you.", "Settings",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 14;

    @TargetApi(Build.VERSION_CODES.M)
    private void startLocationPermissionRequest() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void showSnackbar(String mainString, String actionString,
                              View.OnClickListener listener) {
        Snackbar.make(this.findViewById(android.R.id.content),
                mainString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionString, listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(LocationService.LOCATION_UPDATE));

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startService(new Intent(this, LocationService.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        stopService(new Intent(this, LocationService.class));
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
        viewPager.setCurrentItem(Constants.MAIN_PAGER_START_PAGE);

        HomeFragment homeFragment = (HomeFragment) getTypedFragment(HomeFragment.class);
        ChatFragment chatFragment = (ChatFragment) getTypedFragment(ChatFragment.class);

        if (homeFragment != null)
            homeFragment.fetchTimelineAsync();

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
        viewPager.setCurrentItem(0);
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
