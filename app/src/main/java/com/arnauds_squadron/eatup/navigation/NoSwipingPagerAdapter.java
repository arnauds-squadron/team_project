package com.arnauds_squadron.eatup.navigation;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Child of the ViewPager but prohibits the user from swiping between fragments.
 * Switching between fragments programatically still gives the cool swiping animation
 */
public class NoSwipingPagerAdapter extends ViewPager {

    public NoSwipingPagerAdapter(Context context) {
        super(context);
    }

    public NoSwipingPagerAdapter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Always returns false because the viewpager doesn't handle any touch inputs
     * @return false to not accept the touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        return false;
    }

    /**
     * Always returns false because the user shouldn't be able to interact with the viewpager
     * @return false to not intercept the touch event
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    /**
     * Always returns false because the viewpager shouldn't ever scroll horizontally
     * @return false to not scroll when swiped on
     */
    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}