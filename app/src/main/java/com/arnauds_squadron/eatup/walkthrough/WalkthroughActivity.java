package com.arnauds_squadron.eatup.walkthrough;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arnauds_squadron.eatup.MainActivity;
import com.arnauds_squadron.eatup.R;
import com.arnauds_squadron.eatup.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalkthroughActivity extends AppCompatActivity {

    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @BindView(R.id.btnNext)
    Button btnNext;

    @BindView(R.id.llDots)
    LinearLayout llDots;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        ButterKnife.bind(this);

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
                setDot(llDots, viewPager.getAdapter().getCount(), position);

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
        setDot(llDots, viewPager.getAdapter().getCount(), 0);
    }

    @OnClick(R.id.btnNext)
    public void advanceViewPager() {
        if (viewPager.getCurrentItem() == Constants.LAST_WALKTHROUGH_SCREEN)
            startHomeActivity();
        else
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    @OnClick(R.id.btnSkip)
    public void skipWalkthrough() {
        startHomeActivity();
    }

    private void setDot(ViewGroup viewGroup, int count, int position) {
        viewGroup.removeAllViews();
        TextView[] dot = new TextView[count];

        for (int i = 0; i < count; i++) {
            dot[i] = new TextView(this);
            dot[i].setText(Html.fromHtml("&#8226;"));
            dot[i].setTextSize(50);
            dot[i].setTextColor(Color.BLACK);
            viewGroup.addView(dot[i]);
        }
        dot[position].setTextColor(Color.WHITE);
    }

    private void startHomeActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
