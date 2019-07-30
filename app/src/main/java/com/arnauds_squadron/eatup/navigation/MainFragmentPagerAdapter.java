package com.arnauds_squadron.eatup.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.chat.ChatFragment;
import com.arnauds_squadron.eatup.home.HomeFragment;
import com.arnauds_squadron.eatup.local.LocalFragment;
import com.arnauds_squadron.eatup.profile.ProfileFragment;
import com.arnauds_squadron.eatup.visitor.VisitorFragment;

/**
 * Pager Adapter to handle the 5 main fragments we have in the MainActivity
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private final static int PAGE_COUNT = 5;

    private final int[] tabIcons = {
            R.drawable.chat_tab,
            R.drawable.host_create_tab,
            R.drawable.home_tab,
            R.drawable.visitor_meal_tab,
            R.drawable.profile_tab
    };

    private View[] views = new View[5];

    private Context context;

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return ChatFragment.newInstance();
        if (position == 1)
            return LocalFragment.newInstance();
        if (position == 2)
            return HomeFragment.newInstance();
        if (position == 3)
            return VisitorFragment.newInstance();
        if (position == 4)
            return ProfileFragment.newInstance();
        return HomeFragment.newInstance();
    }

    public View getTabView(int position, String text) {
        if (views[position] == null) {
            @SuppressLint("InflateParams")
            View v = LayoutInflater.from(context).inflate(R.layout.tab_messaging, null);
            views[position] = v;
        }
        View v = views[position];

        ImageView img = v.findViewById(R.id.ivIcon);
        img.setImageResource(tabIcons[position]);
        TextView tv = v.findViewById(R.id.tvNotification);

        if (text == null || text.equals("0")) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
        return v;
    }
}