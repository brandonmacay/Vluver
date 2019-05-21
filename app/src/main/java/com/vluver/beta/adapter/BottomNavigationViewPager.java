package com.vluver.beta.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vlover on 15/04/2018.
 */

public class BottomNavigationViewPager extends ViewPager {
    private boolean isPagingEnable;


    public BottomNavigationViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isPagingEnable = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isPagingEnable) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isPagingEnable) {
            return super.onTouchEvent(event);
        }

        return false;
    }


    public void setPagingEnable(boolean pagingEnable) {
        isPagingEnable = pagingEnable;
    }

    public void setPagingEnabled(boolean enabled) {
        this.isPagingEnable = enabled;
    }
}