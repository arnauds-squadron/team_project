package com.arnauds_squadron.eatup.walkthrough;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arnauds_squadron.eatup.R;

/**
 * Created by ravi on 17/3/18.
 */

public class WalkthroughAdapter extends PagerAdapter {

    private final Context context;
    private int[] layoutIds = {R.layout.walkthrough_slide_1,
            R.layout.walkthrough_slide_2,
            R.layout.walkthrough_slide_3,
            R.layout.walkthrough_slide_4};

    WalkthroughAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return layoutIds.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(layoutIds[position], container, false);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }
}
