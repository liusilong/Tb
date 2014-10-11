package com.example.twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tb.R;

@SuppressLint("NewApi")
public class TwitterFragment extends Fragment {
	public static final String TAG = TwitterFragment.class.getSimpleName();

	private Button mLoginButton;// 登陆按钮
	private Button mLogoutButton;// 退出按钮
	private Button mTweetButton;// 推文按钮
	private Twitter mTwitter;// 声明一个Twitter
	private RequestToken mRequestToken;// 请求用户授权Request Token
	private AccessToken accessToken = null;
	private View tView;
	private EditText statusText;// 推文

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		tView = inflater.inflate(R.layout.twitterfragment, container, false);
		mLoginButton = (Button) tView.findViewById(R.id.login);
		// 登陆按钮的监听事件
		mLoginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				// // 验证key和secret
				// ConfigurationBuilder confbuilder = new
				// ConfigurationBuilder();
				// Configuration conf = confbuilder
				// .setOAuthConsumerKey(Const.CONSUMER_KEY)
				// .setOAuthConsumerSecret(Const.CONSUMER_SECRET).build();
				// // 实例化Twitter
				// mTwitter = new TwitterFactory(conf).getInstance();
				// mTwitter.setOAuthAccessToken(null);

				// new Thread(new Runnable() {
				// public void run() {
				// try {
				// mRequestToken = mTwitter
				// .getOAuthRequestToken(Const.CALLBACK_URL);
				// Intent intent = new Intent(getActivity(),
				// TwitterLogin.class);
				// intent.putExtra(Const.IEXTRA_AUTH_URL,
				// mRequestToken.getAuthorizationURL());//
				// 将重定向的地址传入TwitterLogin中
				// startActivityForResult(intent, 0);
				// } catch (TwitterException e) {
				// e.printStackTrace();
				// }
				// }
				// }).start();
				// 验证key和secret
				Check();
				LoginListener listener = new LoginListener();
				// 线程池工具类
				ThreadPoolUtil.getInstance().execute(listener);
			}
		});

		// 退出按钮及监听事件
		mLogoutButton = (Button) tView.findViewById(R.id.logout);
		mLogoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences pref = getActivity().getSharedPreferences(
						Const.PREF_NAME, getActivity().MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.remove(Const.PREF_KEY_ACCESS_TOKEN);
				editor.remove(Const.PREF_KEY_ACCESS_TOKEN_SECRET);
				editor.commit();

				if (mTwitter != null) {
					// mTwitter.shutdown();
				}

				Toast.makeText(getActivity(), "unauthorized",
						Toast.LENGTH_SHORT).show();
			}
		});
		// 推文按钮及监听事件
		mTweetButton = (Button) tView.findViewById(R.id.tweet);
		mTweetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ConfigurationBuilder confbuilder = null;
				try {
					if (mTwitter == null) {
						Check();
					}
					SharedPreferences pref = getActivity()
							.getSharedPreferences(Const.PREF_NAME,
									getActivity().MODE_PRIVATE);
					String accessToken = pref.getString(
							Const.PREF_KEY_ACCESS_TOKEN, null);
					String accessTokenSecret = pref.getString(
							Const.PREF_KEY_ACCESS_TOKEN_SECRET, null);
					if (accessToken == null || accessTokenSecret == null) {
						Toast.makeText(getActivity(), "not authorize yet",
								Toast.LENGTH_SHORT).show();
						return;
					}
					mTwitter.setOAuthAccessToken(new AccessToken(accessToken,
							accessTokenSecret));
					statusText = (EditText) tView.findViewById(R.id.status);
					// 分享的文字及图片
					new Thread(new Runnable() {
						String status = statusText.getText().toString();

						public void run() {
							try {
								StatusUpdate mstatus = new StatusUpdate(status);
								Bitmap bitmap = BitmapFactory
										.decodeFile("/storage/sdcard0/DCIM/Camera/20140919_110056.jpg");
								InputStream is = BitmapToInputStream(bitmap);
								mstatus.setMedia(status, is);
								mTwitter.updateStatus(mstatus);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
					}).start();

					statusText.setText(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return tView;
	}

	// 保存用户信息（SharedPreferences）
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == 0) {
			if (resultCode == getActivity().RESULT_OK) {
				try {
					final String oauthVerifier = intent.getExtras().getString(
							Const.IEXTRA_OAUTH_VERIFIER);
					new Thread(new Runnable() {
						public void run() {
							try {
								accessToken = mTwitter.getOAuthAccessToken(
										mRequestToken, oauthVerifier);
								SharedPreferences pref = getActivity()
										.getSharedPreferences(Const.PREF_NAME,
												getActivity().MODE_PRIVATE);
								SharedPreferences.Editor editor = pref.edit();
								editor.putString(Const.PREF_KEY_ACCESS_TOKEN,
										accessToken.getToken());
								editor.putString(
										Const.PREF_KEY_ACCESS_TOKEN_SECRET,
										accessToken.getTokenSecret());
								editor.commit();
							} catch (TwitterException e) {
								e.printStackTrace();
							}
						}
					}).start();

					// 登陆成功后的提示
					Toast.makeText(getActivity(), "authorized",
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (resultCode == getActivity().RESULT_CANCELED) {
				Log.w(TAG, "Twitter auth canceled.");
			}
		}
	}

	// 验证key和secret及实例化Twitter
	public void Check() {
		// 验证key和secret
		ConfigurationBuilder confbuilder = new ConfigurationBuilder();
		Configuration conf = confbuilder
				.setOAuthConsumerKey(Const.CONSUMER_KEY)
				.setOAuthConsumerSecret(Const.CONSUMER_SECRET).build();
		// 实例化Twitter
		mTwitter = new TwitterFactory(conf).getInstance();
		// mTwitter.setOAuthAccessToken(null);
	}

	// 登录按钮的监听事件
	public class LoginListener implements Runnable {

		public void run() {
			try {
				mRequestToken = mTwitter
						.getOAuthRequestToken(Const.CALLBACK_URL);
				Intent intent = new Intent(getActivity(), TwitterLogin.class);
				intent.putExtra(Const.IEXTRA_AUTH_URL,
						mRequestToken.getAuthorizationURL());// 将重定向的地址传入TwitterLogin中
				startActivityForResult(intent, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	// 推文按钮的监听事件
	public class TweetListener implements Runnable {
		public void run() {

		}
	}

	/**
	 * 将Bitmap转化成Inputstrem
	 * 
	 * @param bitmap
	 * @return
	 */
	public InputStream BitmapToInputStream(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Log.i("length", baos.toByteArray().length + "");
		bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);// 30 是压缩率,表示压缩70%
		Log.i("length", baos.toByteArray().length + "");
		Log.i("length", baos.toByteArray().length + "");
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}
}
