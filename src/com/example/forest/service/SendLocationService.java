package com.example.forest.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;

import com.example.forest.util.BDLocationHelper;
import com.example.forest.util.Const;
import com.example.forest.util.DBManager;
import com.example.forest.util.Util;

public class SendLocationService extends Service {

	// //��������״̬
	// NetState netreceiver = null;
	// IntentFilter netfilter = null;
	// //����GPS״̬
	// GPSState gpsreceiver = null;
	// IntentFilter gpsfilter = null;

	// SharedPreferences conPreferences;
	Util util = null;
	BDLocationHelper helper;

	@Override
	public void onCreate() {
		super.onCreate();

		// //ע���ƶ�����״̬������
		// netreceiver = new NetState();
		// netfilter = new IntentFilter();
		// netfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// this.registerReceiver(netreceiver, netfilter);
		// netreceiver.onReceive(this, null);
		//
		// //ע��gps״̬״̬������
		// gpsreceiver = new GPSState();
		// gpsfilter = new IntentFilter();
		// gpsfilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
		// this.registerReceiver(gpsreceiver, gpsfilter);
		// gpsreceiver.onReceive(this, null);

		// conPreferences = Forest.config_preferences;

		util = new Util(SendLocationService.this);

		helper = BDLocationHelper.getInstance(this);
		helper.getLocation();
	}

	public void send_unsendLocation() {
		List<String> unsendLocationList;
		DBManager dbManager = new DBManager(SendLocationService.this);
		dbManager.openDatabase();
		unsendLocationList = dbManager.query_unsendlocation();

		if (unsendLocationList != null && unsendLocationList.size() > 0) {
			for (int i = 0; i < unsendLocationList.size(); i++) {
				System.out.println("δ���ͳɹ��Ķ�λ��¼:" + unsendLocationList.get(i));
				boolean sendsuc_flag = util.sendLocation(unsendLocationList
						.get(i));
				if (sendsuc_flag == true) {
					dbManager.update_senddedlocation(unsendLocationList.get(i));
					dbManager.delete_sendedlocation();
				} else {
					break;
				}
			}
		}
		dbManager.closeDatabase();
	}

	public void send_unsendPhotos() {
		List<String> unsendPhotosList;
		DBManager dbManager = new DBManager(SendLocationService.this);
		dbManager.openDatabase();
		unsendPhotosList = dbManager.query_unsendPhotos();

		if (unsendPhotosList != null && unsendPhotosList.size() > 0) {
			for (int i = 0; i < unsendPhotosList.size(); i++) {
				System.out.println("δ���ͳɹ�����Ƭ��:" + unsendPhotosList.get(i)
						+ ".jpg");
				File file = new File(Environment.getExternalStorageDirectory()
						+ "/forest/msg/", unsendPhotosList.get(i) + ".jpg");

				boolean sendsuc_flag = Util.uploadFile(file);
				if (sendsuc_flag == true) {
					dbManager.update_sendPhoto(unsendPhotosList.get(i));
					dbManager.delete_sendedPhoto();
				} else {
					break;
				}
			}
		}
		dbManager.closeDatabase();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		helper.release();
		// this.unregisterReceiver(gpsreceiver);
		// this.unregisterReceiver(netreceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		int t = super.onStartCommand(intent, flags, startId);

		Date now = new Date();
		SimpleDateFormat dat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");

		final String loc_dat = dat.format(now);
		final String loc_time = time.format(now);

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				send_unsendLocation();
				send_unsendPhotos();

				// ����㼣��λ��Ϣ
				String msg = util.obtainLocation_Auto(loc_dat, loc_time,
						Const.FOOT, SendLocationService.this);
				boolean flag = util.sendLocation(msg);

				/**
				 * @date 20140915
				 * @content ����flagҲ����Ϊfalse,������������µ�if��������߼����󣡣���
				 */
				if (!flag) {
					System.out.println("SendService��thread����ʧ�ܣ��������ݿ�");
					DBManager dbManager = new DBManager(
							SendLocationService.this);
					dbManager.openDatabase();
					dbManager.insert_location(msg, 0);
					dbManager.closeDatabase();
				}
			}
		};

		// //���������Ϣͨ��
		// Thread notifyThread = new Thread()
		// {
		// @Override
		// public void run() {
		// super.run();
		// Main.notify = ResolveXML.getNotification();
		//
		// DBManager dbManager = new DBManager(SendLocationService.this);
		// dbManager.openDatabase();
		// dbManager.delete_readedMsg();
		//
		// for(int i=0; i<Main.notify.size();i++)
		// {
		// if(Main.notify.get(i) != null &&
		// !Main.notify.get(i).trim().equals(""))
		// dbManager.insert_serverMsg(Main.notify.get(i), 0);
		// System.out.println("��ȡ����������ͨ����Ϣ��"+Main.notify.get(i));
		// }
		// dbManager.closeDatabase();
		// }
		// };
		//
		// if(conPreferences.getBoolean("shangban", false))

		/**
		 * @date 20140915
		 * @content ������������������û����¡��ϰࡱ��ť���ִ�з��ͻ�����Ϣ�����͵�ǰλ����Ϣ�Ĳ���
		 */
		if (Util.shangbanFlag) {
			thread.start();
		}
		return t;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * @date 20140915
	 * @content �������������������ã��������ƶ��������ӵ�����������ִ�з��ͻ�����Ϣ����
	 * 
	 */
	// ��������״̬�㲥������
	class NetState extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo gprs = manager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (!gprs.isConnected()) {
				// ����δ����״̬
				System.out.println("�����Ϊ������");
			} else {
				System.out.println("�����Ϊ����");
				send_unsendLocation();
				send_unsendPhotos();
			}
		}
	}

	// GPS״̬�㲥������
	class GPSState extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			LocationManager locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			boolean gps_use = locmanager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (gps_use) {
				Const.GPS_STATE = Const.GPS_AVILIBALE;
			} else {
				Const.GPS_STATE = Const.GPS_UNAVILIBALE;
			}
		}
	}
}
