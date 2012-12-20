package com.onix.flickrslider.models;

import java.io.Serializable;

public class ImageItem implements Serializable {

	private String	mThumbUrl;
	private String	mJObj;

	public ImageItem(String mThumbUrl, String _obj) {
		this.mThumbUrl = mThumbUrl;
		this.mJObj = _obj;
	}

	public String getOBJ() {
		return mJObj;
	}

	public String getThumbUrl() {
		return mThumbUrl;
	}

	public void setThumbUrl(String mThumbUrl) {
		this.mThumbUrl = mThumbUrl;
	}
}
