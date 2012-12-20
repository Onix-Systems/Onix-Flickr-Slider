package com.onix.flickrslider;

import com.onix.flickrslider.appcode.DataController;

import android.app.Application;
import android.content.Context;

public class App extends Application {

	private DataController	mData;

	public static int getVersionCode(Context _context) {
		try
		{
			return _context.getPackageManager().getPackageInfo(_context.getPackageName(), _context.getPackageManager().GET_META_DATA).versionCode;
		}
		catch (Exception e)
		{
			return 1;
		}
	}

	public void initDataController() {
		mData = new DataController(getApplicationContext());
	}

	public DataController getData() {
		return mData;
	}
}
