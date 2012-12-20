package com.onix.flickrslider.activities;

import java.io.File;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.onix.android.imagedownloader.CacheType;
import com.onix.android.imagedownloader.ImageDownloader;
import com.onix.flickrslider.App;
import com.onix.flickrslider.R;
import com.onix.flickrslider.appcode.Constants;
import com.onix.flickrslider.appcode.LoadTask;
import com.onix.flickrslider.appcode.api.WebApi;
import com.onix.flickrslider.components.CustomViewPager;
import com.onix.flickrslider.components.TouchImageView;
import com.onix.flickrslider.utils.NetUtils;
import com.onix.flickrslider.utils.UiUtils;

public class PictureDetailsActivity extends Activity {

	private int					mImageID;
	private ImageDownloader		mImageLoader;
	private WebApi				mApi;
	private App					mApp;
	private CustomViewPager		mViewPager;
	private ImagePagerAdapter	mAdapter;
	private Button				mShareButton;
	private Button				mSaveButton;
	private LoadTask			mLoadTask;
	private boolean				isNetworkAvalable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview_activity);

		mApp = (App) getApplication();
		mApi = new WebApi();

		mImageLoader = new ImageDownloader(this, (short) 3);
		mImageLoader.enableDiskCache(false);
		mImageLoader.setCacheType(CacheType.SOFT_REFERENCE);

		isNetworkAvalable = NetUtils.isNetworkAvailable(this);

		mViewPager = (CustomViewPager) findViewById(R.id.view_pager);
		mAdapter = new ImagePagerAdapter();
		mViewPager.setAdapter(mAdapter);

		mSaveButton = (Button) findViewById(R.id.save_button);
		mShareButton = (Button) findViewById(R.id.share_button);

		mSaveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try
				{
					if (NetUtils.isNetworkAvailable(PictureDetailsActivity.this))
						saveImage(mApi.getImageUrlMedium(new JSONObject(mApp.getData().getImageList().get(mViewPager.getCurrentItem()).getOBJ())));
					else
						Toast.makeText(PictureDetailsActivity.this, getString(R.string.error_internet_connection), Toast.LENGTH_SHORT).show();
				}
				catch (Exception e)
				{

				}
			}
		});

		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) && NetUtils.isNetworkAvailable(this))
			mSaveButton.setVisibility(View.VISIBLE);
		else
			mSaveButton.setVisibility(View.GONE);

		mShareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					String url = mApi.getImageUrlMedium(new JSONObject(mApp.getData().getImageList().get(mViewPager.getCurrentItem()).getOBJ()));
					UiUtils.share(PictureDetailsActivity.this, "", url);
				}
				catch (Exception ex)
				{

				}

			}
		});

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mViewPager.setScrollEnabled(true);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		Bundle extras = getIntent().getExtras();
		if (extras.containsKey(Constants.IMAGE_ID))
		{
			mImageID = extras.getInt(Constants.IMAGE_ID);
			mViewPager.setCurrentItem(mImageID);

		}
		else
			mImageID = -1;
	}

	private class ImagePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mApp.getData().getImageList().size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((TouchImageView) object);
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			final TouchImageView imageView = new TouchImageView(PictureDetailsActivity.this);
			imageView.setZoomListener(new TouchImageView.ZoomListener() {

				@Override
				public void onZoomed(boolean isZoomed) {
					if (isZoomed)
						mViewPager.setScrollEnabled(false);
					else
						mViewPager.setScrollEnabled(true);
				}
			});

			String fileName = mImageLoader.getImage(mApp.getData().getImageList().get(position).getThumbUrl());
			Bitmap mBitmap = null;
			try
			{
				mBitmap = BitmapFactory.decodeFile(fileName);
			}
			catch (OutOfMemoryError e)
			{
			}
			imageView.setMaxZoom(Constants.PICTURE_DETAILS_MAX_ZOOM_PARAMETER);
			imageView.setImageBitmap(mBitmap);

			if (isNetworkAvalable)
			{
				if (mBitmap == null)
				{
					mImageLoader.setStub(R.drawable.preview_stub);
				}
				else
				{
					mImageLoader.setStub(mBitmap);
				}
				try
				{
					mImageLoader.loadImage(imageView, mApi.getImageUrlMedium(new JSONObject(mApp.getData().getImageList().get(position).getOBJ())));
				}
				catch (Exception ex)
				{

				}
			}

			((ViewPager) container).addView(imageView, 0);

			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			TouchImageView view = (TouchImageView) object;
			UiUtils.unbindDrawables(view, false);
			((ViewPager) container).removeView((TouchImageView) object);
		}
	}

	@Override
	protected void onResume() {

		isNetworkAvalable = NetUtils.isNetworkAvailable(this);

		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		UiUtils.unbindDrawables(mViewPager, true);

		mAdapter = null;
		mViewPager.setAdapter(null);
		try
		{
			mViewPager.removeAllViews();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		UiUtils.releaseView(mViewPager);

		mViewPager = null;
		mImageLoader.finalizeLoader();
		mImageLoader = null;
		mApi.finalizeObj();
		mApi = null;
		System.gc();

		super.onDestroy();
	}

	private void saveImage(String _url) {
		final Dialog dialog = new Dialog(PictureDetailsActivity.this, R.style.CustomDialogTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.save_image_dialog);
		final ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.load_progress_view);
		final TextView mFolderTextView = (TextView) dialog.findViewById(R.id.folder_text_view);
		mFolderTextView.setText("sdcard" + Constants.PICTURE_FOLDER);

		File file = new File(Environment.getExternalStorageDirectory(), Constants.PICTURE_FOLDER);
		if (!file.exists())
			file.mkdir();

		String fileName = file.getAbsolutePath() + "/" + NetUtils.getFileName(_url);
		dialog.setCancelable(false);

		mLoadTask = new LoadTask(_url, fileName, new LoadTask.LoadListener() {

			@Override
			public void onProgress(int progress) {
				// TODO Auto-generated method stub
				if (progressBar != null)
					progressBar.setProgress(progress);
			}

			@Override
			public void onInterrupted() {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}

			@Override
			public void onError(String _errorText) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});

		dialog.findViewById(R.id.save_cancel_button).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLoadTask.interrupt();
				// dialog.dismiss();
			}
		});
		mLoadTask.execute();
		dialog.show();
	}
}
