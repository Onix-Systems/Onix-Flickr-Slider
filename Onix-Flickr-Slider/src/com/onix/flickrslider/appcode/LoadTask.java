package com.onix.flickrslider.appcode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class LoadTask extends AsyncTask<Void, Integer, Void> {

	private String			mUrl;
	private String			mFilePath;
	private LoadListener	mLoadListener;
	private boolean			isInterrupted;

	public LoadTask(String _url, String _fileName, LoadListener _listener) {
		mUrl = _url;
		mFilePath = _fileName;
		mLoadListener = _listener;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		if (mLoadListener != null)
		{
			mLoadListener.onProgress(progress[0]);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {

		try
		{
			if (checkForInterrupt())
				return null;

			URL url = new URL(mUrl);
			URLConnection connection = url.openConnection();
			connection.connect();

			if (checkForInterrupt())
				return null;

			int fileLength = connection.getContentLength();
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(mFilePath);

			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1)
			{
				total += count;
				publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
				Thread.sleep(10);
				if (checkForInterrupt())
					return null;
			}

			output.flush();
			output.close();
			input.close();
		}
		catch (Exception e)
		{
			if (mLoadListener != null)
			{
				mLoadListener.onError("");
			}
		}
		return null;
	}

	private boolean checkForInterrupt() {
		if (isInterrupted)
		{
			removeTempFile();
			return true;
		}
		else
			return false;
	}

	public void interrupt() {
		isInterrupted = true;
	}

	private void removeTempFile() {
		try
		{
			File file = new File(mFilePath);
			file.delete();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		if (mLoadListener != null)
		{
			if (isInterrupted)
				mLoadListener.onInterrupted();
			else
				mLoadListener.onFinish();
		}
	}

	public interface LoadListener {
		void onFinish();

		void onError(String _errorText);

		void onProgress(int progress);

		void onInterrupted();
	}

}
