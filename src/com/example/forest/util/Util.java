package com.example.forest.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import com.example.forest.activity.Login;

public class Util {
	public static List<String> notify;

	/******** ��¼���͹��� **********************/
	static final String SDPATH = Environment.getExternalStorageDirectory()
			+ "/";
	static final String filename = "fail_record.txt";
	static File file = null;
	/***************************************************************************
	 * ��ʼ
	 * 
	 * @date 20140912 �������´���
	 * @content ʹphoneID��userID����ʼ��Ϊ��xxxxxxxx��,���ǳ�ʼ��Ϊ��sharedPreferences��ȡ��ֵ;
	 * 
	 **/
	// �򿪱����Ѿ����ڵ�sharedPreferences�ļ�
	static SharedPreferences sharedPreferences = Forest.config_preferences;
	final Editor editor = sharedPreferences.edit();
	// ��̬ȫ�ֱ���
	public static String phoneID = sharedPreferences.getString("phoneID", "");
	public static String userID = sharedPreferences.getString("userID", "");
	/************************************** ���� **********************************/
	public static boolean shangbanFlag = false;

	public static String newName = "image.jpg";

	Context context;
	// Location cur_location = new Location(0, 0);

	// ��¼��λ��ʱ�䣬���ڣ�����Ƭ�Լ����溦���鱨���ʱ�������֮ǰ��λ��Ϣ��ʱ��һ�£�
	Date now = null;
	public String loc_dat = null;
	public String loc_time = null;

	// SharedPreferences config_preferences;

	public Util(Context context) {
		this.context = context;
		// config_preferences = Forest.config_preferences;
	}

	// ���ݴ����ı�ţ����ض�λ��Ӧ�ĸ�ʽ���ַ��������飬���溦���Ŀ��ķ���
	public String obtainLocation(String msg_type, Context context) {
		StringBuilder result = new StringBuilder();
		now = new Date();
		SimpleDateFormat dat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");

		loc_dat = dat.format(now);
		loc_time = time.format(now);

		// String phoneID = Forest.config_preferences.getString("phoneID",
		// "xxxxxxxx");
		// String userID = Forest.config_preferences.getString("userID", null);

		result.append(phoneID + ","); // �ֻ��ն˱��
		result.append(dat.format(now) + ","); // ����
		result.append(time.format(now) + ","); // ��׼��λʱ��
		result.append(Const.GPS_STATE + ","); // GPS��λ״̬��ʶ
		result.append("E,"); // ���������ʶ
		result.append(Const.cur_location.getLongitude() + ","); // ����
		result.append("N,"); // �ϱ������ʶ
		result.append(Const.cur_location.getLatitude() + ","); // γ��
		result.append(msg_type + ","); // ����
		result.append(userID);

		return result.toString();
	}

	// �Զ�����ʱ��λ��Ϣ
	public String obtainLocation_Auto(String date, String time,
			String msg_type, Context context) {
		StringBuilder result = new StringBuilder();
		// String phoneID = Forest.config_preferences.getString("phoneID",
		// "xxxxxxxx");
		// String userID = Forest.config_preferences.getString("userID", null);

		result.append(phoneID + ","); // �ֻ��ն˱��
		result.append(date + ","); // ����
		result.append(time + ","); // ��׼��λʱ��
		result.append(Const.GPS_STATE + ","); // GPS��λ״̬��ʶ
		result.append("E,"); // ���������ʶ
		result.append(Const.cur_location.getLongitude() + ","); // ����
		result.append("N,"); // �ϱ������ʶ
		result.append(Const.cur_location.getLatitude() + ","); // γ��
		result.append(msg_type + ","); // ����
		result.append(userID);

		return result.toString();
	}

	// ���Ͷ�λ��Ϣ
	public boolean sendLocation(String locationMsg) {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		String urlStr = BASE_URL + "ReceiveMsgServlet";
		System.out.println(urlStr);
		try {
			createFile();
			writedata(locationMsg + "\n");
			String result = null;
			String notifications = null;
			if (Forest.isNetConnect(context)) {
				URL url = new URL(urlStr + "?location=" + locationMsg);
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(3000);
				InputStream in = conn.getInputStream();

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(in);
				result = doc.getElementsByTagName("received").item(0)
						.getFirstChild().getNodeValue();
				System.out.println("��λ�ɹ�ȷ�ϣ���" + result);

				// �ڷ��Ͷ�λ��Ϣ��ͬʱ���Դӷ������˲�ѯ�Ƿ�����ͨ����Ϣ������У���˳�㽫����뱾�����ݿ�
				notifications = doc.getElementsByTagName("notification")
						.item(0).getFirstChild().getNodeValue();
				System.out.println("��Ϣͨ�棺" + notifications);
				writedata("��Ϣͨ�棺" + notifications);
				if (notifications != null && !notifications.trim().equals("")) {
					DBManager dbManager = new DBManager(context);
					dbManager.openDatabase();

					String[] notifyList = notifications.split(",");
					// Ϊʲô����notifyList.size()?
					// notifyList.length��ʾԪ�ظ���
					for (int i = 0; i < notifyList.length; i++) {
						dbManager.insert_serverMsg(notifyList[i], 0);
						System.out.println("�������ݿ⣺" + notifyList[i]);
					}
					dbManager.closeDatabase();
				}
			}

			if (result != null && !result.equals("")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			writedata("���Ͷ�λ��" + e.getMessage() + "\n");
			return false;
		}
	}

	// ���Ͳ��溦����
	public boolean sendPestsDetail(String detailMsg) {
		String BASE_URL = "http://" + Login.SERVER_IP
				+ ":8080/AndroidServer/servlet/";
		String urlStr = BASE_URL + "ReceivePestsDetailServlet";
		System.out.println(urlStr);
		try {
			URL url = new URL(urlStr + "?pestsDetail=" + detailMsg);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			String result = doc.getElementsByTagName("received").item(0)
					.getFirstChild().getNodeValue();
			if (result != null && !result.equals("")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			writedata("���Ͳ��溦��" + e.getMessage() + "\n");
			e.printStackTrace();
			return false;
		}
	}

	// ������������ļ�
	public static boolean uploadFile(File file) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		try {
			String actionUrl = "http://" + Login.SERVER_IP
					+ ":8080/AndroidServer/servlet/ReceivePhotoServlet";
			// ��������ַ
			URL url = new URL(actionUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			/* ����Input��Output����ʹ��Cache */
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			/* ���ô��͵�method=POST */
			con.setRequestMethod("POST");
			/* setRequestProperty */
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Charset", "UTF-8");
			con.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			/* ����DataOutputStream */
			DataOutputStream ds = new DataOutputStream(con.getOutputStream());
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"file1\";filename=\"" + newName + "\"" + end);
			ds.writeBytes(end);

			/* ȡ���ļ���FileInputStream */
			FileInputStream fStream = new FileInputStream(file);
			System.out.println(file + "-------------------------------file");

			/* ����ÿ��д��1024bytes */
			int bufferSize = 2048;
			byte[] buffer = new byte[bufferSize];
			int length = -1;
			/* ���ļ���ȡ������������ */
			while ((length = fStream.read(buffer)) != -1) {
				/* ������д��DataOutputStream�� */
				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			/* close streams */
			System.out.println(ds + "========ds");
			fStream.close();
			ds.flush();
			/* ȡ��Response���� */
			InputStream is = con.getInputStream();
			System.out.println(is + "=============is");
			/* �ر�DataOutputStream */
			ds.close();
			return true;
		} catch (Exception e) {
			writedata("�ϴ���Ƭ��" + e.getMessage() + "\n");
			e.printStackTrace();
			return false;
		}
	}

	/***** �ж�ip��ַ�Ƿ�Ϸ���Ч *************/
	public static boolean isIpv4(String ipAddress) {
		String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	// ��SD���ĸ�Ŀ¼�´���һ��txt�ļ�������������
	public static File createFile() throws IOException {
		System.out.println(SDPATH);

		file = new File(SDPATH, filename);
		if (file == null)
			file.createNewFile();
		return file;
	}

	// �ж��ļ��Ƿ��Ѿ�����
	public static boolean checkFileExists() {
		// File file = new File(SDPATH,filename);
		return file.exists();
	}

	public static void writedata(String data) {
		try {
			if (checkFileExists()) {
				FileWriter out = new FileWriter(file, true);
				out.write(data);
				out.close();
			} else {
				createFile();
				FileWriter out = new FileWriter(file, true);
				out.write(data);
				out.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.toString());
		}
	}

}
