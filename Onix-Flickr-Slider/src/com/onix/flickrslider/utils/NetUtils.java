package com.onix.flickrslider.utils;

import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null)
		{
			return false;
		}
		else
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
			{
				for (int i = 0; i < info.length; i++)
				{
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public static final String getFileName(String _uri) {
		if (_uri == null)
			return "" + (new Random().nextInt());

		String extension = ".jpg";
		String fileName = "";

		String[] strParts = _uri.split("\\.");

		if (strParts != null)
		{
			if (strParts.length == 0)
				return "" + (new Random().nextInt()) + "." + extension;

			extension = strParts[strParts.length - 1];
			if (strParts.length > 1)
			{
				String tempstr[] = strParts[strParts.length - 2].split("\\/");
				if (tempstr == null)
				{
					fileName = "" + _uri.hashCode();
				}
				else if (tempstr.length == 0)
				{
					fileName = "" + _uri.hashCode();
				}
				else
				{
					fileName = tempstr[tempstr.length - 1];
				}
			}
			else
				fileName = "" + _uri.hashCode();
		}
		else
			fileName = "" + _uri.hashCode();

		return fileName + "." + extension;

	}
}
