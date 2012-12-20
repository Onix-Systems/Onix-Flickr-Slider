package com.onix.flickrslider.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.onix.flickrslider.App;
import com.onix.flickrslider.R;
import com.onix.flickrslider.utils.UiUtils;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				App app = (App) getApplication();
				app.initDataController();

				try
				{
					Thread.sleep(900);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();
			};
		}.execute();
	}

	@Override
	protected void onDestroy() {

		// UiUtils.releaseView(findViewById(R.id.main_container));
		// UiUtils.releaseView(findViewById(R.id.image_logo_view));

		UiUtils.unbindDrawables(findViewById(R.id.main_container), true);
		UiUtils.unbindDrawables(findViewById(R.id.image_logo_view), true);

		System.gc();
		super.onDestroy();
	}
}