package com.onix.flickrslider.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.onix.android.imagedownloader.ImageDownloader;
import com.onix.flickrslider.R;
import com.onix.flickrslider.models.ImageItem;

public class ImageGridAdapter extends ArrayAdapter<Void> {

	private final LayoutInflater	mInflater;
	private ImageDownloader			mImageLoader;
	private ArrayList<ImageItem>	mItems;

	public ImageGridAdapter(Activity _activity, ArrayList<ImageItem> _items) {
		super(_activity, R.id.empty_view);
		mInflater = LayoutInflater.from(_activity);
		mItems = _items;
		initLoader(_activity);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null)
			view = mInflater.inflate(R.layout.image_grid_item, parent, false);

		ImageView imageView = (ImageView) view.findViewById(R.id.item_image_view);
		ImageItem item = mItems.get(position);

		Integer tag = (Integer) view.getTag();

		if (item.getThumbUrl() != null && (tag == null || tag != (Integer) position))
		{
			mImageLoader.loadImage(imageView, item.getThumbUrl());
			view.setTag((Integer) position);
		}

		return view;
	}

	public void initLoader(Activity _activity) {
		mImageLoader = new ImageDownloader(_activity);
		//mImageLoader.setStub(R.drawable.stub);
	}

	public void finalizeAdapter() {
		if (mImageLoader != null)
			mImageLoader.finalizeLoader();
	}

	public void invalidateList() {
		add(null);
	}

	public void clearMemoryCache() {
		mImageLoader.clearMemoryCache();
	}

}
