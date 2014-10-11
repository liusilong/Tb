package com.example.instagram;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * InstagramSession类用于存取数据
 * 
 * @author hughes
 * 
 */
public class InstagramSession {
	private SharedPreferences sharedPref;
	private Editor editor;
	private static final String SHARED = "Instagram_Preferences";
	private static final String API_USERNAME = "username";
	private static final String API_ID = "id";
	private static final String API_NAME = "name";
	private static final String API_ACCESS_TOKEN = "access_token";
	private static final String API_USER_IMAGE = "user_image";

	/**
	 * 构造方法
	 * 
	 * @param context
	 */
	public InstagramSession(Context context) {
		sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
	}

	/**
	 * 将相关数据保存在SharedPreferences中
	 * 
	 * @param accessToken
	 * @param expireToken
	 * @param expiresIn
	 * @param username
	 */
	public void storeAccessToken(String accessToken, String id,
			String username, String name, String image) {
		editor.putString(API_ID, id);
		editor.putString(API_NAME, name);
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.putString(API_USERNAME, username);
		editor.putString(API_USER_IMAGE, image);
		editor.commit();
	}

	public void storeAccessToken(String accessToken) {
		editor.putString(API_ACCESS_TOKEN, accessToken);
		editor.commit();
	}

	/**
	 * Reset access token and user name
	 */
	public void resetAccessToken() {
		editor.putString(API_ID, null);
		editor.putString(API_NAME, null);
		editor.putString(API_ACCESS_TOKEN, null);
		editor.putString(API_USERNAME, null);
		editor.putString(API_USER_IMAGE, null);
		editor.commit();
	}

	/**
	 * Get user name
	 * 
	 * @return User name
	 */
	public String getUsername() {
		return sharedPref.getString(API_USERNAME, null);
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return sharedPref.getString(API_ID, null);
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return sharedPref.getString(API_NAME, null);
	}

	/**
	 * Get access token
	 * 
	 * @return Access token
	 */
	public String getAccessToken() {
		return sharedPref.getString(API_ACCESS_TOKEN, null);
	}

	/**
	 * Get userImage
	 * 
	 * @return userImage
	 */
	public String getUserImage() {
		return sharedPref.getString(API_USER_IMAGE, null);
	}
}
