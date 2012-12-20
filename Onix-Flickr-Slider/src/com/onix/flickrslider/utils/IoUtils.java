package com.onix.flickrslider.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;

public class IoUtils {
	public static String[] readStringFromAssets(Context context, String path) {
		try
		{
			// char[] buffer = new char[16384];
			InputStream is = context.getAssets().open(path);
			ArrayList<String> mas = new ArrayList<String>();

			// Read text from file

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 65535);
			String line;

			while ((line = br.readLine()) != null)
			{
				mas.add(line);
			}
			return mas.toArray(new String[mas.size()]);
		}
		catch (IOException e)
		{
			return null;
		}
	}
}
