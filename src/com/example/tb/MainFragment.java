package com.example.tb;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {
	private static final String PERMISSION = "publish_actions";
	private static final String TAG = "MainFragment";
	// 用于子自动打开，恢复，保存Session的
	// https://developers.facebook.com/docs/reference/android/current/class/UiLifecycleHelper/
	private UiLifecycleHelper uiHelper;
	// ---分享-----
	private Button postPhotoButton;
	private Button postStatusUpdateButton;
	private LoginButton authButton;
	private boolean canPresentShareDialog;
	private GraphPlace place;
	private List<GraphUser> tags;
	private GraphUser user;
	Session session;
	boolean pendingRequest;
	boolean falg = false;
	private int count = 0;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	private PendingAction pendingAction = PendingAction.NONE;
	// 登陆后的回调方法
	private Session.StatusCallback callback = new Session.StatusCallback() {
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
			/**
			 * 根据count的值来判断执行分享图片或分享文字的方法
			 */
			if (count == 1) {
				onClickPostPhoto();
			} else if (count == 2) {
				onClickPostStatusUpdate();
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(
				getActivity(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);
		authButton = (LoginButton) view.findViewById(R.id.loginBtn);
		// 登陆方法
		login();
		// 分享图片按钮及监听事件
		postPhotoButton = (Button) view.findViewById(R.id.postPhotoButton);
		postPhotoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Session loginsession = Session.getActiveSession();
				if (loginsession.isOpened()) {
					onClickPostPhoto();
				} else {
					count = 1;
					Toast.makeText(getActivity(), "请登录", Toast.LENGTH_SHORT)
							.show();
					// 登陆
					onClickLogin();
				}
			}
		});
		// 分享文字按钮及点击事件
		postStatusUpdateButton = (Button) view
				.findViewById(R.id.postStatusUpdateButton);
		postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Session session = Session.getActiveSession();
				if (session.isOpened()) {
					onClickPostStatusUpdate();
				} else {
					count = 2;
					onClickLogin();
				}
			}
		});
		return view;
	}

	// 登录状态
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			// postPhotoButton.setVisibility(View.VISIBLE);
		} else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
			// postPhotoButton.setVisibility(View.INVISIBLE);
		}

	}

	// --------------分享文字
	private void onClickPostStatusUpdate() {
		performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
	}

	// 分享图片按钮的点击事件
	private void onClickPostPhoto() {
		performPublish(PendingAction.POST_PHOTO, false);
	}

	private void performPublish(PendingAction action, boolean allowNoSession) {
		Session session = Session.getActiveSession();
		if (session != null) { // 此时状态为 opend
			pendingAction = action;
			// 这的返回值被改为true
			Log.i("aaa", hasPublishPermission() + "\t");
			if (hasPublishPermission()) {
				handlePendingAction();
				return;
			} else if (session.isOpened()) {
				// 请求新的权限
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSION));
				return;
			}
		}
		if (allowNoSession) {
			pendingAction = action;
			handlePendingAction();
		}
	}

	// 权限
	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		Log.i("aaa", session.getState() + "Session state for tb");
		// return session != null
		// && session.getPermissions().contains("publish_actions");
		return true;
	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		pendingAction = PendingAction.NONE;
		switch (previouslyPendingAction) {
		case POST_PHOTO:
			postPhoto();// 分享图片
			break;
		case POST_STATUS_UPDATE:
			postStatusUpdate();// fen'xiang'wen'zi
			break;
		}
	}

	// 分享文字
	private void postStatusUpdate() {
		if (canPresentShareDialog) {// 为false
			// 此处user用于获取用户信息
			// } else if (user != null && hasPublishPermission()) {
		} else if (hasPublishPermission()) {
			final String message = getString(R.string.status_update, "tb",
					(new Date().toString()));
			Request request = Request.newStatusUpdateRequest(
					Session.getActiveSession(), message, place, tags,
					new Request.Callback() {
						public void onCompleted(Response response) {
							showPublishResult(message,
									response.getGraphObject(),
									response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_STATUS_UPDATE;
		}
	}

	// 图片分享
	private void postPhoto() {
		if (hasPublishPermission()) {
			// 分享的图片
			// Bitmap image = BitmapFactory.decodeFileDescriptor(
			// new BitmapTool().getBitmap("/storage/sdcard0/aa/074.png", null));
			Bitmap image = new BitmapTool().getBitmap(
					"/storage/sdcard0/aa/074.png", null);
			Request request = Request.newUploadPhotoRequest(
					Session.getActiveSession(), image, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(getString(R.string.photo_post),
									response.getGraphObject(),
									response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	// 分享成功或失败时的Toast
	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		if (error == null) {
			Toast.makeText(getActivity(), "分享成功", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getActivity(), "分享失败", Toast.LENGTH_LONG).show();
		}

	}

	// static final String APP_ID = "366630730157613";

	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		uiHelper.onResume();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	// LoginButton 的点击事件
	public void login() {
		count = 0;
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("email", "user_likes",
				"user_status", "user_photos"));
		authButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					public void onUserInfoFetched(GraphUser user) {
						MainFragment.this.user = user;// 实例化 GraphUser
					}
				});
	}

	// 在没有登录的情况下 点击postPhoto按钮时触发的事件
	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this)
					.setCallback(callback));
		} else {
			Session.openActiveSession(getActivity(), this, true, callback);
			Log.i("sessionLogin", session.getState() + "");
		}
	}
}
