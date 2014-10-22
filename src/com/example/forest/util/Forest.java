package com.example.forest.util;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * �����Լ���Application���������ݴ��ݡ����ݹ������ݻ���Ȳ����� ��Ҫ��manifest�ļ���application��ǩ��ע��
 * 
 * @author comprq
 * 
 */

public class Forest extends Application {

	/****************************** 20140912 �������´��� ��ʼ ***************************/
	public static SharedPreferences config_preferences;
	/****************************** 20140912 �������´��� ���� ***************************/
	// ���ڱ���δ���ͳɹ����ݵ��ļ�
	final static String path = Environment.getExternalStorageDirectory() + "/";

	File filedir = new File(path + "forest/msg/");

	@Override
	public void onCreate() {

		super.onCreate();

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(this);

		filedir.mkdirs();
		/****************************** 20140912 �������´��� ��ʼ ***************************/
		config_preferences = getSharedPreferences("saveContent", MODE_PRIVATE);
		/****************************** 20140912 �������´��� ���� ***************************/
	}

	// ��鵱ǰ��������״̬
	/**
	 * isNetConnectֻ�ǳ��Ե��ж��Ƿ����������磬�������ĵ�ǰʹ�õ�����������(WIFI or MOBILE)
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();

				if (info != null && info.isConnected()) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}

	// ��ȡAndroid_ID
	public static String getAndroidID(Context context) {
		String android_id = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		return android_id;
	}

	// ���Device_ID
	public String getDeviceID() {
		return ((TelephonyManager) Forest.this
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
}