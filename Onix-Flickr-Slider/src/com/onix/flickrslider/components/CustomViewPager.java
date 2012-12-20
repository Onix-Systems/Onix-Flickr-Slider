package com.onix.flickrslider.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomViewPager extends ViewPager {

	private boolean	isCanScroll	= true;

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomViewPager(Context context) {
		super(context);
	}

	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (isCanScroll)
			return false;
		else
			return true;
	}

	public synchronized void setScrollEnabled(boolean _enabled) {
		isCanScroll = _enabled;
	}
}
