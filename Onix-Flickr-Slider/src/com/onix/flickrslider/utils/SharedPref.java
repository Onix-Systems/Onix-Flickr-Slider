/* 
 * Author: Eugene Sakara (vzov@yandex.ua)
 * URL: https://market.android.com/details?id=org.chaszmin.notes
 * License: GPLv3 (http://www.gnu.org/licenses/gpl-3.0.html)
 * Start: 01.02.2012
 */

package com.onix.flickrslider.utils;

import com.onix.flickrslider.appcode.Constants;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
	private Context	mContext;

	public SharedPref(Context context) {
		mContext = context;
	}

	public void saveToPrefs(String _key, String _value) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor prefsEdit = prefs.edit();

		prefsEdit.putString(_key, _value);
		prefsEdit.commit();
	}

	public void saveToPrefsInt(String _key, int _value) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor prefsEdit = prefs.edit();

		prefsEdit.putInt(_key, _value);
		prefsEdit.commit();
	}

	public void saveToPrefsBoolean(String _key, boolean _value) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor prefsEdit = prefs.edit();

		prefsEdit.putBoolean(_key, _value);
		prefsEdit.commit();
	}

	public String getStringFromPrefs(String _key) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		String accessToken = prefs.getString(_key, "");

		return accessToken;
	}

	public boolean getBooleanFromPrefs(String _key) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		boolean accessToken = prefs.getBoolean(_key, false);

		return accessToken;
	}

	public int getIntFromPrefs(String _key) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		int accessToken = prefs.getInt(_key, 0);

		return accessToken;
	}

	public int getIntFromPrefs(String _key, int _default_value) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		int accessToken = prefs.getInt(_key, _default_value);

		return accessToken;
	}
}
