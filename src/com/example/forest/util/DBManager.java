/**
 * forest.sdb������ݿ������õ�һ��SQLite DataBase������ֱ�Ӵ����ģ������ô����������ģ���Ϊ��������ֱ��
 * �ù����߿��ӻ��Ŀ������ݿ���ʲô���ģ�Ȼ�󽫴��������.sdb�����ݿ��ļ�������raw�ļ�����ֱ��ʹ�ã�
 * Ҫ������.sdb���ݿ��ļ���Ҫ����һ��SQLite�����ߴ򿪲鿴���ݱ�Ľṹ���ֶΣ�ֱ�Ӵ򿪿϶�������ģ�
 * ������Ҳ�����ڵ��Ե����У�����cmd,Ȼ����DOS���ڣ���������adb shell ����android Linux�����У�
 * ʹ������鿴���ݿ�ṹ��
 */

package com.example.forest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.forest.R;
import com.example.forest.bean.UserInfo;

public class DBManager {
	private final int BUFFER_SIZE = 1024;
	public static final String DB_NAME = "forest.sdb";
	public static final String PACKAGE_NAME = "com.example.forest";
	public static final String DB_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME;
	private SQLiteDatabase database;
	private Context context;
	private File file = null;

	public DBManager(Context context) {
		this.context = context;
	}

	/**
	 * �����ݿ�
	 */
	public void openDatabase() {
		this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
	}

	/**
	 * �������ݿ�
	 * 
	 * @return
	 */
	private SQLiteDatabase getDatabase() {
		return this.database;
	}

	/**
	 * �����ݿ��ļ������������ݿ�
	 * 
	 * @param dbfile
	 * @return
	 */
	private SQLiteDatabase openDatabase(String dbfile) {
		try {
			file = new File(dbfile);
			if (!file.exists()) {
				InputStream is = context.getResources().openRawResource(
						R.raw.forest);
				FileOutputStream fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
					fos.flush();
				}
				fos.close();
				is.close();
			}
			// ???�˴�Ϊ�β���database = SQLiteDatabase.openOrCreateDatabase(file,
			// null);
			database = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
			return database;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * �ر����ݿ�
	 */
	public void closeDatabase() {
		if (this.database != null)
			this.database.close();
	}

	/****************** ��λ��Ϣ��ȡɾ��������ݿ���� *******************************************/
	/**
	 * 1�������ݿ��в���һ����λ��¼
	 * 
	 * @param content
	 * @param sendsuc_flag
	 */
	public void insert_location(String content, int sendsuc_flag) {
		System.out.println("���ݿ���붨λ��¼");
		database.execSQL("insert into location(content,sendsuc_flag) "
				+ "values('" + content + "'," + sendsuc_flag + ")");
	}

	/**
	 * 2����ѯδ���ͳɹ��Ķ�λ��Ϣ
	 * 
	 * @return
	 */
	public List<String> query_unsendlocation() {
		System.out.println("���ݿ��ѯδ���Ͷ�λ��¼");
		List<String> list = new ArrayList<String>();

		Cursor cursor = database.rawQuery(
				"select * from location where sendsuc_flag=0", null);
		while (cursor.moveToNext()) {
			String location = cursor
					.getString(cursor.getColumnIndex("content"));
			list.add(location);
		}
		cursor.close();
		return list;
	}

	/**
	 * 3�������ѷ��ɹ��Ķ�λ��Ϣ��־λ
	 * 
	 * @param content
	 */
	public void update_senddedlocation(String content) {
		System.out.println("���ݿ���¶�λ��¼");
		database.execSQL("update location set sendsuc_flag=1 where content='"
				+ content + "'");
	}

	/**
	 * 4��ɾ�����ݿ����ѷ��Ķ�λ��¼
	 */
	public void delete_sendedlocation() {
		System.out.println("���ݿ�ɾ����λ��¼");
		database.execSQL("delete from location where sendsuc_flag=1");
	}

	/*************** ��������Ϣ��ȡɾ�����ݿ���� *************************************************/
	/**
	 * 1�������ݿ��в���һ���ӷ��������յ�����Ϣ��¼
	 * 
	 * @param content
	 * @param read_flag
	 */
	public void insert_serverMsg(String content, int read_flag) {
		database.execSQL("insert into server_msg(content,read_flag) "
				+ "values('" + content + "'," + read_flag + ")");
	}

	/**
	 * 2����ѯδ��ȡ�ķ�������Ϣ
	 * 
	 * @return
	 */
	public List<String> query_unreadMsg() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = database.rawQuery(
				"select * from server_msg where read_flag=0", null);
		while (cursor.moveToNext()) {
			String msg = cursor.getString(cursor.getColumnIndex("content"));
			list.add(msg);
		}
		cursor.close();
		return list;
	}

	/**
	 * 3���������Ķ��ķ�������Ϣ��־λ �Ķ��󽫱�־λ��Ϊ1
	 * 
	 * @param content
	 */
	public void update_readedMsg(String content) {
		database.execSQL("update server_msg set read_flag=1 where content='"
				+ content + "'");
	}

	/**
	 * 4��ɾ�����ݿ������Ķ��ķ�������Ϣ
	 */
	public void delete_readedMsg() {
		database.execSQL("delete from server_msg where read_flag=1");
	}

	/**************** ��Ƭ��ȡɾ�����ݿ���� ******************************************************/
	/**
	 * 1�������ݿ��в���һ����Ƭ��·��
	 * 
	 * @param path
	 * @param sendsuc_flag
	 */
	public void insert_photos(String path, int sendsuc_flag) {
		database.execSQL("insert into photos(path,sendsuc_flag) " + "values('"
				+ path + "'," + sendsuc_flag + ")");
	}

	/**
	 * 2����ѯδ���ͳɹ�����Ƭ��¼
	 * 
	 * @return
	 */
	public List<String> query_unsendPhotos() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = database.rawQuery(
				"select * from photos where sendsuc_flag=0", null);
		while (cursor.moveToNext()) {
			String photo_name = cursor.getString(cursor.getColumnIndex("path"));
			list.add(photo_name);
		}
		cursor.close();
		return list;
	}

	/**
	 * 3�������ѷ��ͳɹ�����Ƭ��Ϣ ���ͳɹ�����־λ��Ϊ1
	 * 
	 * @param name
	 */
	public void update_sendPhoto(String name) {
		database.execSQL("update photos set sendsuc_flag=1 where path='" + name
				+ "'");
	}

	/**
	 * 4��ɾ�����ݿ����Ѿ����͵���Ƭ��Ϣ
	 */
	public void delete_sendedPhoto() {
		database.execSQL("delete from photos where sendsuc_flag=1");
	}

	/*************** �����������ݿ��ȡɾ���� *****************************************************/

	public boolean haspestsKinds() {
		Cursor cursor = database.rawQuery("select * from pests_kinds", null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	/**
	 * 1��������������
	 * 
	 * @param kindsnumber
	 * @param kindsname
	 */
	public void insert_pestsKinds(String kindsnumber, String kindsname) {
		// ������������֮ǰ�ѱ������������������Ϣɾ�������²���
		delete_pestsKinds();

		String kindsname_list[] = kindsname.split(",");
		String kindsnumber_list[] = kindsnumber.split(",");

		for (int i = 0; i < kindsnumber_list.length; i++) {
			database.execSQL("insert into pests_kinds(id,kinds) values('"
					+ kindsnumber_list[i] + "','" + kindsname_list[i] + "')");
		}
	}

	/**
	 * 2����ѯ��������
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsKinds() {
		Map<String, String> kinds_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_kinds", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("kinds"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			kinds_list.put(number, name);
		}
		cursor.close();
		return kinds_list;
	}

	/**
	 * 3��ɾ����������������Ϣ
	 */
	private void delete_pestsKinds() {
		database.execSQL("delete from pests_kinds");
	}

	/*************** ���������׶����ݿ��ȡɾ���� *****************************************************/
	/**
	 * 1���������������׶�
	 * 
	 * @param stagenumber
	 * @param stagename
	 */
	public void insert_pestsStage(String stagenumber, String stagename) {
		// ���������׶�֮ǰɾ������֮ǰ����������׶ζ���
		delete_pestsStage();

		String stagename_list[] = stagename.split(",");
		String stagenumber_list[] = stagenumber.split(",");

		for (int i = 0; i < stagename_list.length; i++) {
			database.execSQL("insert into pests_stage(id,stage) values('"
					+ stagenumber_list[i] + "','" + stagename_list[i] + "')");
		}
	}

	/**
	 * 2����ѯ���������׶�
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsStage() {
		Map<String, String> stage_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_stage", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("stage"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			stage_list.put(number, name);
		}
		cursor.close();
		return stage_list;
	}

	/**
	 * 3��ɾ�����гɳ��׶�
	 */
	private void delete_pestsStage() {
		database.execSQL("delete from pests_stage");
	}

	/*************** �ܺ��������ݿ��ȡɾ���� *****************************************************/
	/**
	 * 1�������ܺ�����
	 * 
	 * @param amountnumber
	 * @param amountname
	 */
	public void insert_pestsAmount(String amountnumber, String amountname) {
		// �����ܺ���������֮ǰ��ɾ��֮ǰ����������
		delete_pestsAmount();

		String amountname_list[] = amountname.split(",");
		String amountnumber_list[] = amountnumber.split(",");

		for (int i = 0; i < amountname_list.length; i++) {
			database.execSQL("insert into pests_amount(id,amount) values('"
					+ amountnumber_list[i] + "','" + amountname_list[i] + "')");
		}
	}

	/**
	 * 2����ѯ�ܺ�����
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsAmount() {
		Map<String, String> amount_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_amount", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("amount"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			amount_list.put(number, name);
		}
		cursor.close();
		return amount_list;
	}

	/**
	 * 3��ɾ��������������
	 */
	private void delete_pestsAmount() {
		database.execSQL("delete from pests_amount");
	}

	/*************** Σ���̶����ݿ��ȡɾ���� *****************************************************/
	/**
	 * 1�������ܺ��̶�
	 * 
	 * @param levelnumber
	 * @param levelname
	 */
	public void insert_pestsLevel(String levelnumber, String levelname) {
		// �����ܺ��̶ȶ���֮ǰ����ɾ��֮ǰ����ĸ����ܺ��̶�
		delete_pestsLevel();

		String levelnumber_list[] = levelnumber.split(",");
		String levelname_list[] = levelname.split(",");

		for (int i = 0; i < levelname_list.length; i++) {
			database.execSQL("insert into pests_level(id,level) values('"
					+ levelnumber_list[i] + "','" + levelname_list[i] + "')");
		}
	}

	/**
	 * 2����ѯ�ܺ��̶�
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsLevel() {
		Map<String, String> level_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_level", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("level"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			level_list.put(number, name);
		}
		cursor.close();
		return level_list;
	}

	/**
	 * 3��ɾ�������ܺ��̶�
	 */
	private void delete_pestsLevel() {
		database.execSQL("delete from pests_level");
	}

	/*************** ���������ݿ��ȡɾ���� *****************************************************/
	/**
	 * 1�����봦����
	 * 
	 * @param adviseName
	 * @param adviseNumber
	 */
	public void insert_pestsAdvise(String adviseName, String adviseNumber) {
		// ���봦����֮ǰ��ɾ������֮ǰ�Ľ��鶨��
		delete_pestsAdvise();

		String advisenumber_list[] = adviseNumber.split(",");
		String advisename_list[] = adviseName.split(",");

		for (int i = 0; i < advisename_list.length; i++) {
			database.execSQL("insert into pests_advise(id,advise) values('"
					+ advisenumber_list[i] + "','" + advisename_list[i] + "')");
		}
	}

	/**
	 * 2����ѯ������
	 * 
	 * @return
	 */
	public Map<String, String> query_pestsAdvise() {
		Map<String, String> advise_list = new HashMap<String, String>();
		Cursor cursor = database.rawQuery("select * from pests_advise", null);
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("advise"));
			String number = cursor.getString(cursor.getColumnIndex("id"));

			advise_list.put(number, name);
		}
		cursor.close();
		return advise_list;
	}

	// 3��ɾ�����д�����
	private void delete_pestsAdvise() {
		database.execSQL("delete from pests_advise");
	}

	/***************** ������������������ʱ�� **************************************************/
	/**
	 * 1����ȡ������ʱ��
	 * 
	 * @return
	 */
	public String query_LastTime() {
		String lastTime = "";
		Cursor cursor = database.rawQuery("select * from pests_lasttime", null);
		while (cursor.moveToNext()) {
			lastTime = cursor.getString(cursor.getColumnIndex("lasttime"));
			System.out.println("��ѯ�ϴθ��²��溦�����ʱ��Ϊ��" + lastTime);
		}
		cursor.close();
		return lastTime;
	}

	/**
	 * 2������������ʱ��
	 * 
	 * @param lastTime
	 */
	public void insert_LastTime(String lastTime) {
		database.execSQL("delete from pests_lasttime");
		database.execSQL("insert into pests_lasttime(lasttime) values('"
				+ lastTime + "')");
	}

	/******************** ������֤�ɹ��󣬽��û�����������ڱ��� ***************************************/
	/**
	 * 
	 * @param user
	 *            1������һ���Ϸ����û�
	 */
	public void insert_User(UserInfo user) {
		database.execSQL("insert into user_info(username,password,user_id,belong_farm) values('"
				+ user.getUserName()
				+ "','"
				+ user.getPassword()
				+ "','"
				+ user.getUserID() + "','" + user.getBelongFarm() + "')");
	}

	/**
	 * ��ѯ�����Ƿ�����û���Ϊusername������Ϊpassword�ĺϷ��û�
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean isUserValid(String username, String password) {
		System.out.println("������֤");
		Cursor cursor = database.rawQuery(
				"select * from user_info where username='" + username
						+ "' and password='" + password + "'", null);
		if (cursor.moveToNext()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @return ��ȡ���������û�
	 */
	public List<String> getLocalUserNameList() {
		List<String> localUserList = new ArrayList<String>();
		Cursor cursor = database.rawQuery("select * from user_info", null);
		while (cursor.moveToNext()) {
			String username = cursor.getString(cursor
					.getColumnIndex("username"));

			localUserList.add(username);
		}
		cursor.close();
		return localUserList;
	}

	/**
	 * 
	 * @param username
	 * @return �ж����ݿ����Ƿ��Ѵ��ڸ��û�
	 */
	public boolean isExisted(String username) {
		Cursor cursor = database.rawQuery(
				"select * from user_info where username='" + username + "'",
				null);
		if (cursor.moveToNext()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param username
	 * @return �����û�������û��ı��
	 */
	public String queryUserIDByName(String username) {
		String userID = null;
		Cursor cursor = database.rawQuery(
				"select user_id from user_info where username='" + username
						+ "'", null);
		while (cursor.moveToNext()) {
			userID = cursor.getString(cursor.getColumnIndex("user_id"));
		}
		cursor.close();
		return userID;
	}

	/**
	 * ɾ���������е��û�
	 */
	public void deleteAllLocalUser() {
		database.execSQL("delete from user_info where username!='admin'");
	}

	public void updatePassword(String username, String password) {
		database.execSQL("update user_info set password='" + password
				+ "' where username='" + username + "'");
	}
}