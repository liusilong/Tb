package com.example.tb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapTool {
	public Bitmap getBitmap(String path, String text) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bm = BitmapFactory.decodeFile(path, options);
		return bm;
	}

	public Bitmap getBitmap(byte[] bs, BitmapFactory.Options options,
			String text) {
		if (bs != null) {
			if (options != null) {
				return BitmapFactory.decodeByteArray(bs, 0, bs.length, options);
			} else {
				return BitmapFactory.decodeByteArray(bs, 0, bs.length);
			}
		}
		return null;
	}
}
