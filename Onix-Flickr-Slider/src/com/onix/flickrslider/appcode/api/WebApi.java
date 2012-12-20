package com.onix.flickrslider.appcode.api;

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onix.flickrslider.models.ImageItem;

import android.os.AsyncTask;

public class WebApi {

	private ImageListListener	mImageListListener;
	private ImageThumbsListener	mImageThumbsListener;

	public void GetImageList(String _query, int perPage, int page) {
		new ImageListTask(_query, perPage, page).execute();
	}

	public void getThumbs(JSONArray _jsArray) {
		new ThumbsTask(_jsArray).execute();
	}

	public String getImageUrlMedium(JSONObject _obj) {
		try
		{
			String img_url = GlobalResources.getImageURL(_obj.getString("farm"), _obj.getString("server"), _obj.getString("id"), _obj.getString("secret"), GlobalResources.ImgSize.MED, "jpg");
			return img_url;
		}
		catch (Exception ex)
		{
			return null;
		}

	}

	// api tasks

	private class ImageListTask extends AsyncTask<Void, Void, Void> {
		private String		mQuery;
		private int			mPerPage;
		private int			mPage;
		private JSONArray	m_imglist;

		public ImageListTask(String _query, int perPage, int page) {
			mQuery = _query;
			mPerPage = perPage;
			mPage = page;
			m_imglist = new JSONArray();
		}

		@Override
		protected Void doInBackground(Void... params) {

			String m_fail_msg = null;
			String[] paramNames = null, paramVals = null;
			JSONObject json_obj = null;
			String obj_toplevel_key = "photos";

			Vector<String> pNames = new Vector<String>();
			Vector<String> pVals = new Vector<String>();

			pNames.add("per_page");
			pVals.add(String.valueOf(mPerPage));
			pNames.add("page");
			pVals.add(String.valueOf(mPage));

			pNames.add("text");
			pVals.add(mQuery);

			paramNames = paramVals = new String[] {};
			paramNames = pNames.toArray(paramNames);
			paramVals = pVals.toArray(paramVals);
			json_obj = RestClient.CallFunction("flickr.photos.search", paramNames, paramVals);

			if (json_obj != null)
			{
				String stat = JSONParser.getString(json_obj, "stat");
				if (stat == null || stat.equals("fail"))
				{
					m_fail_msg = JSONParser.getString(json_obj, "message");
					if (m_fail_msg == null)
					{
						m_fail_msg = "Unknown Error while reading pool";
					}
				}
				else
				{
					m_imglist = JSONParser.getArray(json_obj, obj_toplevel_key + "/photo");
					if (m_imglist == null)
					{
						m_imglist = new JSONArray();
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (mImageListListener != null)
			{
				mImageListListener.onSuccess(m_imglist);
			}
		}
	}

	private class ThumbsTask extends AsyncTask<Void, ImageItem, Void> {

		private JSONArray				m_imglist;
		private ArrayList<ImageItem>	items;

		public ThumbsTask(JSONArray _jsArray) {
			m_imglist = _jsArray;
		}

		@Override
		protected Void doInBackground(Void... params) {
			int m_size = 0;
			items = new ArrayList<ImageItem>();

			if (m_imglist != null && m_imglist.length() <= GlobalResources.IMGS_PER_PAGE)
			{
				m_size = m_imglist.length();
			}
			else
			{
				m_size = GlobalResources.IMGS_PER_PAGE;
			}
			String img_url;
			for (int i = 0; i < m_size; i++)
			{
				JSONObject img_obj;
				try
				{
					img_obj = m_imglist.getJSONObject(i);
					img_url = GlobalResources.getImageURL(img_obj.getString("farm"), img_obj.getString("server"), img_obj.getString("id"), img_obj.getString("secret"), GlobalResources.ImgSize.THUMB, "jpg");

					ImageItem imageItem = new ImageItem(img_url, img_obj.toString());
					items.add(imageItem);

					GlobalResources.sleep(50);

					publishProgress(imageItem);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}

			}

			if (mImageThumbsListener != null)
			{
				mImageThumbsListener.onSuccessInBackground(items);
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(ImageItem... values) {
			super.onProgressUpdate(values);

			if (mImageThumbsListener != null)
			{
				mImageThumbsListener.onThumbObtained(values[0]);
			}

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (mImageThumbsListener != null)
			{
				mImageThumbsListener.onSuccess(items);
			}
		}
	}

	// getters & setters

	public void setImageListListener(ImageListListener mImageListListener) {
		this.mImageListListener = mImageListListener;
	}

	public void setImageThumbsListener(ImageThumbsListener mImageThumbsListener) {
		this.mImageThumbsListener = mImageThumbsListener;
	}

	// interfaces
	public interface ImageListListener {
		void onSuccess(JSONArray _imageList);

		void onFalure(String _error);
	}

	public interface ImageThumbsListener {
		void onSuccess(ArrayList<ImageItem> _imageList);

		void onSuccessInBackground(ArrayList<ImageItem> _imageList);

		void onThumbObtained(ImageItem _imageItem);

		void onFalure(String _error);
	}

	public interface ImageMediumListener {
		void onSuccess(String _url);

		void onFalure(String _error);
	}

	public void finalizeObj() {
		mImageListListener = null;
		mImageThumbsListener = null;
	}

}
