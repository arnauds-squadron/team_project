package com.arnauds_squadron.eatup.walkthrough;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.login.LoginActivity;
import com.arnauds_squadron.eatup.utils.Constants;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arnauds_squadron.eatup.utils.Constants.FIRST_LOAD;
import static com.arnauds_squadron.eatup.utils.Constants.PREFS_NAME;

public class WalkthroughActivity extends AppCompatActivity {

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.btnNext)
    Button btnNext;

    @BindView(R.id.pager_indicator)
    IndefinitePagerIndicator pagerIndicator;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        ButterKnife.bind(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(FIRST_LOAD, true)) {
            goToLoginActivity();
        }
        prefs.edit().putBoolean(FIRST_LOAD, false).apply();

        Window window = getWindow();

        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int offset) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == Constants.LAST_WALKTHROUGH_SCREEN)
                    btnNext.setText(getString(R.string.done));
                else
                    btnNext.setText(getString(R.string.next));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setAdapter(new WalkthroughAdapter(this));
        pagerIndicator.attachToViewPager(viewPager);
    }

    /**
     * Advances the ViewPager to the next slide or starts the MainActivity if the ViewPager is on
     * the last slide
     */
    @OnClick(R.id.btnNext)
    public void advanceViewPager() {
        if (viewPager.getCurrentItem() == Constants.LAST_WALKTHROUGH_SCREEN)
            goToLoginActivity();
        else
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    @OnClick(R.id.btnSkip)
    public void skipWalkthrough() {
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}

