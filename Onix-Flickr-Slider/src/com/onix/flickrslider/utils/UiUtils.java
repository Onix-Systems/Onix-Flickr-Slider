package com.onix.flickrslider.utils;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

public class UiUtils {
	public static void unbindDrawables(View view, final boolean agressive) {
		if (view instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				unbindDrawables(((ViewGroup) view).getChildAt(i), agressive);
			}
			if (!AdapterView.class.isInstance(view))
			{
				((ViewGroup) view).removeAllViews();
			}
		}
		else
		{
			try
			{
				Drawable bmp = view.getBackground();
				if (bmp == null && ImageView.class.isInstance(view))
					bmp = ((ImageView) view).getDrawable();
				if (bmp != null)
				{
					bmp.setCallback(null);
					if (agressive && (Drawable.class.isInstance(bmp)))
					{
						try
						{
							Bitmap bm = ((BitmapDrawable) bmp).getBitmap();
							if (bm != null)
							{
								bm.recycle();
								bm = null;
								view.destroyDrawingCache();
								view = null;
							}
						}
						catch (Exception e)
						{
							view.destroyDrawingCache();
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}

	public static void releaseView(View view) {
		try
		{
			if (view != null)
			{
				view.removeCallbacks(null);
				view.destroyDrawingCache();
				view = null;
			}
		}
		catch (Exception e)
		{
		}
	}

	public static int dipToPx(Activity _activity, int _dip) {
		Resources r = _activity.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _dip, r.getDisplayMetrics());
		return px;
	}

	public static void hideKeyboard(Activity _activity, EditText _view) {
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) _activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_view.getWindowToken(), 0);
	}

	public static void share(Activity _activity, String subject, String text) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		_activity.startActivity(intent);
	}

}
