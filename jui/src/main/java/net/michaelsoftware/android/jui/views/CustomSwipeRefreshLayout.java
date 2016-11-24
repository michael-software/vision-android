package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by Michael on 24.11.2016.
 */

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    private int mTouchSlop;
    private float mPrevX;
    private boolean isDisabled;

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void disableInterceptTouchEvent(boolean isDisabled) {
        this.isDisabled = isDisabled;
        getParent().requestDisallowInterceptTouchEvent(isDisabled);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDisabled) {
                    return false;
                }
                float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);
                if (xDiff > mTouchSlop) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }
}