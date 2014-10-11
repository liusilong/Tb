package com.example.instagram;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.instagram.InstagramApp.OAuthAuthenticationListener;
import com.example.tb.R;

@SuppressLint("NewApi")
public class InstagramFragment extends Fragment {
	private View iview;
	private Button button;
	private InstagramApp instaObj;
	public static final String CLIENT_ID = "d2b1adff9ed245d2b9e57c0559bd3c07";
	public static final String CLIENT_SECRET = "f02100110110477e862d99576a730091";
	public static final String CALLBACK_URL = "http://localhost";

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		iview = inflater.inflate(R.layout.instagramfragment, container, false);
		instaObj = new InstagramApp(getActivity(), CLIENT_ID, CLIENT_SECRET,
				CALLBACK_URL);
		instaObj.setListener(listener);

		button = (Button) iview.findViewById(R.id.InsBtn);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				instaObj.authorize();
			}
		});
		return iview;
	}

	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {

		public void onSuccess() {
			Log.e("Userid", instaObj.getId());
			Log.e("Name", instaObj.getName());
			Log.e("UserName", instaObj.getUserName());
			// Instagram 文档上得分享是分享到第三方应用中
			String type = "image/*";
			// String filename = "/20140919_110056.jpg";
			// String mediaPath = Environment.getExternalStorageDirectory()
			// .getPath() + filename;
			// 分享的图片
			String mediaPath = "/storage/sdcard0/DCIM/Camera/20140919_110056.jpg";
			// 分享的文字
			String captionText = "From Instagram";
			createInstagramIntent(type, mediaPath, captionText);
			Toast.makeText(getActivity(), "Share Success", Toast.LENGTH_SHORT)
					.show();
		}

		public void onFail(String error) {
			Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
		}
	};

	// 分享（分享到第三方）
	private void createInstagramIntent(String type, String mediaPath,
			String caption) {
		// Create the new Intent using the 'Send' action.
		Intent share = new Intent(Intent.ACTION_SEND);
		// Set the MIME type
		share.setType(type);
		// Create the URI from the media
		File media = new File(mediaPath);
		Uri uri = Uri.fromFile(media);
		// Add the URI and the caption to the Intent.
		share.putExtra(Intent.EXTRA_STREAM, uri);
		share.putExtra(Intent.EXTRA_TEXT, caption);
		// Broadcast the Intent.
		startActivity(Intent.createChooser(share, "Share to"));
	}

}
