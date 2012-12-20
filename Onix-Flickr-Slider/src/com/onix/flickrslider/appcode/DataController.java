package com.onix.flickrslider.appcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.onix.flickrslider.models.ImageItem;

import android.content.Context;

public class DataController {
	private ArrayList<ImageItem>	mImageList;
	private File					cacheDir;
	private static String			CACHE_FILE_NAME	= "data.dat";

	public DataController(Context _context, ArrayList<ImageItem> mImageList) {
		this.mImageList = mImageList;
		cacheDir = _context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	public DataController(Context _context) {
		cacheDir = _context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		restoreList();
	}

	public final void clear() {
		mImageList.clear();
	}

	public final synchronized void addList(ArrayList<ImageItem> _items) {
		mImageList.addAll(_items);
	}

	public final synchronized void addItem(ImageItem _item) {
		mImageList.add(_item);
	}

	public final synchronized ArrayList<ImageItem> getImageList() {
		return mImageList;
	}

	public final synchronized void setImageList(ArrayList<ImageItem> mImageList) {
		this.mImageList = mImageList;
	}

	public final void save() {
		try
		{

			File hashFile = new File(cacheDir, CACHE_FILE_NAME);

			if (!hashFile.exists())
			{
				hashFile.createNewFile();
			}
			ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(hashFile));
			outStream.writeObject(mImageList);
			outStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private final void restoreList() {
		try
		{
			File hashFile = new File(cacheDir, CACHE_FILE_NAME);

			ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(hashFile));
			mImageList = (ArrayList<ImageItem>) inStream.readObject();
			inStream.close();

			if (mImageList == null)
				mImageList = new ArrayList<ImageItem>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mImageList = new ArrayList<ImageItem>();
		}
	}

}
