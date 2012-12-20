package com.onix.flickrslider.activities;

import java.util.ArrayList;

import org.json.JSONArray;

import android.R.color;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onix.flickrslider.App;
import com.onix.flickrslider.R;
import com.onix.flickrslider.adapters.ImageGridAdapter;
import com.onix.flickrslider.appcode.Constants;
import com.onix.flickrslider.appcode.DataController;
import com.onix.flickrslider.appcode.api.GlobalResources;
import com.onix.flickrslider.appcode.api.WebApi;
import com.onix.flickrslider.appcode.api.WebApi.ImageListListener;
import com.onix.flickrslider.appcode.api.WebApi.ImageThumbsListener;
import com.onix.flickrslider.models.ImageItem;
import com.onix.flickrslider.utils.NetUtils;
import com.onix.flickrslider.utils.SharedPref;
import com.onix.flickrslider.utils.UiUtils;

public class MainActivity extends Activity implements OnItemClickListener {

	private GridView			mGridView;
	private Button				mFindButton;
	private EditText			mFindEditText;
	private ImageGridAdapter	mAdapter;
	private WebApi				mApi;
	private int					mCurrentPage;
	private boolean				isUpdaing;
	private int					mScrollItemCurrent;

	private TextView			mMoreTextView;
	private ImageView			mMoreImageView;

	private App					mApp;

	// search
	private LinearLayout		mSearchContainer;
	private boolean				isSearchShowed;
	private AnimationSet		mShowSearchAnim;
	private AnimationSet		mHideSearchAnim;
	private LinearLayout		mLoadContainer;

	private Button				mSearchButton;
	private ImageView			mSearchSeparator;
	private boolean				isSearchButtonShowed;
	private String				mPrevSearchText;

	// more
	private LinearLayout		mMoreContainer;
	private AnimationSet		mShowMoreAnim;
	private AnimationSet		mHideMoreAnim;
	private boolean				isShowingUpdater;
	private boolean				isMoreShowed;
	private SharedPref			mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mFindButton = (Button) findViewById(R.id.find_button);
		mFindEditText = (EditText) findViewById(R.id.find_edit_text_view);
		mGridView = (GridView) findViewById(R.id.main_image_grid_view);

		mMoreTextView = (TextView) findViewById(R.id.more_text_view);
		mMoreImageView = (ImageView) findViewById(R.id.more_image_view);

		mLoadContainer = (LinearLayout) findViewById(R.id.loading_container);
		mSearchContainer = (LinearLayout) findViewById(R.id.search_container);
		mMoreContainer = (LinearLayout) findViewById(R.id.more_container);

		mSearchButton = (Button) findViewById(R.id.find_button);
		mSearchSeparator = (ImageView) findViewById(R.id.find_separator_button);
		isSearchButtonShowed = false;

		mPrefs = new SharedPref(this);

		mPrevSearchText = mPrefs.getStringFromPrefs(com.onix.flickrslider.appcode.Constants.PREFS_LAST_SEARCH_QUERY);
		mFindEditText.setText(mPrevSearchText);

		mApp = (App) getApplication();

		DataController controller = mApp.getData();

		if (controller == null)
			mApp.initDataController();

		mAdapter = new ImageGridAdapter(this, mApp.getData().getImageList());

		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(MainActivity.this);

		isShowingUpdater = false;
		isSearchShowed = true;
		isMoreShowed = false;

		// init animations
		initSearchAnimation();

		// init more animation
		initMoreAnimation();

		// init main data
		mCurrentPage = 1;
		mApi = new WebApi();

		// init event handlers

		mFindButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isUpdaing)
					search();
			}
		});

		mMoreTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isUpdaing)
					loadNextPage();
			}
		});

		mFindEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (isUpdaing)
					return;
				if (s.toString().length() > 0)
				{
					showSearchButton();
				}
				else
					hideSearchButton();
			}
		});

		mFindEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE)
				{
					if (!isUpdaing)
						search();
				}
				return false;
			}
		});
	}

	private void lockMore(int _lastSearchCount) {
		if (_lastSearchCount < GlobalResources.IMGS_PER_PAGE)
		{
			mGridView.setOnScrollListener(null);
		}
	}

	private void search() {

		mPrevSearchText = mFindEditText.getText().toString();

		if (mPrevSearchText.equals(""))
		{
			Toast.makeText(this, getString(R.string.error_internet_empty), Toast.LENGTH_SHORT).show();
			return;
		}

		if (!NetUtils.isNetworkAvailable(this))
		{
			Toast.makeText(this, getString(R.string.error_internet_connection), Toast.LENGTH_SHORT).show();
			return;
		}

		isUpdaing = true;

		mApi.setImageListListener(new ImageListListener() {

			@Override
			public void onSuccess(JSONArray _imageList) {
				mApi.setImageThumbsListener(new ImageThumbsListener() {

					@Override
					public void onThumbObtained(ImageItem _imageItem) {

					}

					@Override
					public void onSuccess(ArrayList<ImageItem> _imageList) {
						// TODO Auto-generated method stub
						try
						{

							showSearchContainer();
							mSearchContainer.setVisibility(View.VISIBLE);
							mAdapter = new ImageGridAdapter(MainActivity.this, mApp.getData().getImageList());
							mGridView.setAdapter(mAdapter);
							mGridView.setOnScrollListener(mScrollListener);
							hideLoadingView();

							showSearchButton();

							isUpdaing = false;

							lockMore(_imageList.size());
						}
						catch (Exception e)
						{
						}
					}

					@Override
					public void onFalure(String _error) {
						// TODO Auto-generated method stub
						isUpdaing = false;
					}

					@Override
					public void onSuccessInBackground(ArrayList<ImageItem> _imageList) {
						mApp.getData().addList(_imageList);
					}
				});

				mApi.getThumbs(_imageList);

			}

			@Override
			public void onFalure(String _error) {

			}
		});

		mGridView.setOnScrollListener(null);

		mApp.getData().clear();

		if (mAdapter != null)
			mAdapter.finalizeAdapter();

		mGridView.setAdapter(null);
		mCurrentPage = 1;
		mApi.GetImageList(mFindEditText.getText().toString(), GlobalResources.IMGS_PER_PAGE, mCurrentPage);
		showLoadingView();

		UiUtils.hideKeyboard(MainActivity.this, mFindEditText);
		hideSearchContainer();
		isShowingUpdater = false;
		hideMore();
	}

	AbsListView.OnScrollListener	mScrollListener	= new AbsListView.OnScrollListener() {
														@Override
														public void onScrollStateChanged(AbsListView view, int scrollState) {

														}

														@Override
														public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

															if (!isUpdaing)
															{
																if (mScrollItemCurrent != firstVisibleItem)
																{
																	mScrollItemCurrent = firstVisibleItem;
																	isShowingUpdater = false;
																}

																if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount + visibleItemCount > 0 && visibleItemCount != totalItemCount)
																{

																	isShowingUpdater = true;
																}
															}

															if (isShowingUpdater)
															{
																showMore();
															}
															else
																hideMore();

														}
													};

	private void initSearchAnimation() {
		int searchHeight = UiUtils.dipToPx(this, 45);
		Animation trAnim = new TranslateAnimation(0, 0, 0, -searchHeight - 50);
		trAnim.setDuration(600);
		trAnim.setFillAfter(true);
		trAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mSearchContainer.setVisibility(View.GONE);
			}
		});

		Animation alphaAnim = new AlphaAnimation(1, 0);
		alphaAnim.setDuration(400);

		mHideSearchAnim = new AnimationSet(true);
		mHideSearchAnim.addAnimation(trAnim);
		mHideSearchAnim.addAnimation(alphaAnim);

		Animation trShowAnim = new TranslateAnimation(0, 0, -searchHeight - 50, 0);
		trShowAnim.setDuration(600);
		trShowAnim.setFillAfter(true);
		trShowAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				mSearchContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}
		});

		Animation alphaShowAnim = new AlphaAnimation(0, 1);
		alphaShowAnim.setDuration(400);

		mShowSearchAnim = new AnimationSet(true);
		mShowSearchAnim.addAnimation(trShowAnim);
		mShowSearchAnim.addAnimation(alphaShowAnim);
	}

	private void initMoreAnimation() {
		int searchHeight = UiUtils.dipToPx(this, 90);
		Animation trAnim = new TranslateAnimation(0, 0, searchHeight + 50, 0);
		trAnim.setDuration(600);
		trAnim.setFillAfter(true);
		trAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				mMoreContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub

			}
		});

		Animation alphaAnim = new AlphaAnimation(0, 1);
		alphaAnim.setDuration(400);

		mShowMoreAnim = new AnimationSet(true);
		mShowMoreAnim.addAnimation(trAnim);
		mShowMoreAnim.addAnimation(alphaAnim);

		Animation trHideAnim = new TranslateAnimation(0, 0, 0, searchHeight + 50);
		trHideAnim.setDuration(600);
		trHideAnim.setFillAfter(true);
		trHideAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				// mMoreContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mMoreContainer.setVisibility(View.GONE);
			}
		});

		Animation alphaShowAnim = new AlphaAnimation(1, 0);
		alphaShowAnim.setDuration(400);

		mHideMoreAnim = new AnimationSet(true);
		mHideMoreAnim.addAnimation(trHideAnim);
		mHideMoreAnim.addAnimation(alphaShowAnim);
	}

	private void loadNextPage() {

		if (mPrevSearchText.equals(""))
		{
			Toast.makeText(this, getString(R.string.error_internet_empty), Toast.LENGTH_SHORT).show();
			return;
		}

		if (!NetUtils.isNetworkAvailable(this))
		{
			Toast.makeText(this, getString(R.string.error_internet_connection), Toast.LENGTH_SHORT).show();
			return;
		}

		isUpdaing = true;

		showMoreLoadingView();
		hideSearchButton();
		if (mAdapter != null)
			mAdapter.clearMemoryCache();

		mCurrentPage++;

		mGridView.setOnScrollListener(null);

		mApi.setImageListListener(new ImageListListener() {

			@Override
			public void onSuccess(JSONArray _imageList) {
				mApi.setImageThumbsListener(new ImageThumbsListener() {

					@Override
					public void onThumbObtained(ImageItem _imageItem) {
					}

					@Override
					public void onSuccess(ArrayList<ImageItem> _imageList) {
						// TODO Auto-generated method stub
						try
						{
							hideMore();
							hideMoreLoadingView();
							if (mFindEditText.getText().toString().length() > 0)
								showSearchButton();
							isShowingUpdater = false;
							mGridView.setOnScrollListener(mScrollListener);
							isUpdaing = false;
							mAdapter.invalidateList();

							lockMore(_imageList.size());
						}
						catch (Exception e)
						{
						}
					}

					@Override
					public void onFalure(String _error) {
						// TODO Auto-generated method stub
						if (mFindEditText.getText().toString().length() > 0)
							showSearchButton();

						isUpdaing = false;
					}

					@Override
					public void onSuccessInBackground(ArrayList<ImageItem> _imageList) {
						mApp.getData().addList(_imageList);
					}
				});

				mApi.getThumbs(_imageList);

			}

			@Override
			public void onFalure(String _error) {

			}
		});
		mApi.GetImageList(mPrevSearchText, GlobalResources.IMGS_PER_PAGE_UPDATE, mCurrentPage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
			{
				case R.id.menu_help: {
					// show help dialog
					final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomDialogTheme);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.help_dialog);

					dialog.findViewById(R.id.help_dialog_cancel_button).setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});

					dialog.show();
					return true;
				}
			}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		if (mAdapter != null)
			mAdapter.finalizeAdapter();

		mApp.getData().save();
		mPrefs.saveToPrefs(Constants.PREFS_LAST_SEARCH_QUERY, mPrevSearchText);
		super.onDestroy();

		getIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		System.runFinalization();
		System.exit(0);
	}

	@Override
	protected void onPause() {
		super.onPause();
	};

	@Override
	protected void onResume() {
		super.onResume();

		if (!NetUtils.isNetworkAvailable(this))
		{
			Toast.makeText(this, getString(R.string.error_internet_connection), Toast.LENGTH_SHORT).show();
		}

		if (isUpdaing)
			return;
		if (mPrevSearchText.length() > 0)
		{
			showSearchButton();
		}
		mFindEditText.setText(mPrevSearchText);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long _id) {

		if (mAdapter != null)
			mAdapter.clearMemoryCache();

		Intent intent = new Intent(MainActivity.this, PictureDetailsActivity.class);
		intent.putExtra(com.onix.flickrslider.appcode.Constants.IMAGE_ID, position);
		startActivityForResult(intent, 123);
	}

	// animations method
	private void hideSearchContainer() {
		if (isSearchShowed)
		{
			mSearchContainer.setVisibility(View.VISIBLE);
			isSearchShowed = false;
			mSearchContainer.clearAnimation();
			mSearchContainer.startAnimation(mHideSearchAnim);
		}
	}

	private void showSearchContainer() {
		if (!isSearchShowed)
		{
			isSearchShowed = true;
			mSearchContainer.clearAnimation();
			mSearchContainer.startAnimation(mShowSearchAnim);
		}
	}

	private void showMore() {
		if (!isMoreShowed)
		{
			isMoreShowed = true;
			mMoreContainer.clearAnimation();
			mMoreContainer.startAnimation(mShowMoreAnim);
		}
	}

	private void hideMore() {
		if (isMoreShowed)
		{
			mMoreContainer.setVisibility(View.VISIBLE);
			isMoreShowed = false;
			mMoreContainer.clearAnimation();
			mMoreContainer.startAnimation(mHideMoreAnim);
		}
	}

	private void hideSearchButton() {
		if (isSearchButtonShowed)
		{
			isSearchButtonShowed = false;
			mSearchButton.clearAnimation();
			mSearchSeparator.clearAnimation();

			Animation anim = new AlphaAnimation(1, 0);
			anim.setDuration(500);
			anim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					mSearchButton.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					mSearchButton.setVisibility(View.INVISIBLE);
				}
			});

			mSearchButton.startAnimation(anim);

			Animation animation = new AlphaAnimation(1, 0);
			animation.setDuration(1000);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					mSearchSeparator.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					mSearchSeparator.setVisibility(View.INVISIBLE);
				}
			});

			mSearchSeparator.startAnimation(animation);
		}
	}

	private void showSearchButton() {
		if (!isSearchButtonShowed)
		{
			isSearchButtonShowed = true;
			mSearchButton.clearAnimation();
			mSearchSeparator.clearAnimation();

			Animation anim = new AlphaAnimation(0, 1);
			anim.setDuration(500);
			anim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					mSearchButton.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub

				}
			});

			mSearchButton.startAnimation(anim);

			Animation animation = new AlphaAnimation(0, 1);
			animation.setDuration(1000);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					mSearchSeparator.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub

				}
			});

			mSearchSeparator.startAnimation(animation);
		}
	}

	private void showMoreLoadingView() {

		mMoreTextView.clearAnimation();
		Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(400);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				Animation anim = new AlphaAnimation(0, 1);
				anim.setDuration(500);

				mMoreTextView.setText(getString(R.string.picture_viewer_load_dialog_text));

				mMoreTextView.setBackgroundColor(color.transparent);

				Animation animImage = new AlphaAnimation(0, 1);
				animImage.setDuration(500);
				animImage.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						AnimationDrawable drawable = (AnimationDrawable) getResources().getDrawable(R.drawable.load_animation);
						mMoreImageView.setImageDrawable(drawable);
						drawable.start();
					}
				});

				mMoreImageView.clearAnimation();
				mMoreImageView.setVisibility(View.VISIBLE);
				mMoreImageView.startAnimation(animImage);

				mMoreTextView.clearAnimation();
				mMoreTextView.startAnimation(anim);

			}
		});

		mMoreTextView.clearAnimation();
		mMoreTextView.startAnimation(animation);

	}

	private void hideMoreLoadingView() {
		mMoreTextView.setText(getString(R.string.picture_viewer_load_more));
		mMoreTextView.setBackgroundResource(R.drawable.help_ok_button_selector);
		mMoreImageView.setVisibility(View.GONE);
		mMoreImageView.setImageDrawable(null);
	}

	private void showLoadingView() {
		mLoadContainer.clearAnimation();
		AnimationDrawable drawable = (AnimationDrawable) getResources().getDrawable(R.drawable.load_animation);
		ImageView view = (ImageView) findViewById(R.id.load_image_view);
		view.setImageDrawable(drawable);
		drawable.start();

		Animation alphaAnim = new AlphaAnimation(0, 1);
		alphaAnim.setDuration(800);
		alphaAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				mLoadContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});

		mLoadContainer.startAnimation(alphaAnim);

	}

	private void hideLoadingView() {

		mLoadContainer.clearAnimation();

		Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(500);

		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				mLoadContainer.setVisibility(View.GONE);
				ImageView view = (ImageView) findViewById(R.id.load_image_view);
				view.setImageDrawable(null);
			}
		});

		mLoadContainer.startAnimation(animation);

	}
}
