package com.example.forest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {

	private static final String TAG = "CrashHandler";
	
	//ϵͳĬ�ϵ�UncaughtException������
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	
	//Crashhandlerʵ��
	private static CrashHandler INSTANCE = new CrashHandler();
	
	private Context mContext;   //�����Context����
	
	//�����洢�豸��Ϣ���쳣��Ϣ
	private Map<String, String> info = new HashMap<String, String>();
	
	//���ڸ�ʽ�����ڣ���Ϊ��־�ļ���һ����
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	//��ֻ֤��һ��CrashHandlerʵ��
	private CrashHandler()
	{
		
	}
	
	//��ȡCrashHandlerʵ��������ģʽ
	public static CrashHandler getInstance()
	{
		return INSTANCE;
	}
	
	//��ʼ��
	public void init(Context context)
	{
		mContext =  context;
		//��ȡϵͳĬ�ϵ�UnCaughtException������
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		
		//��CrashhandlerΪ�����Ĭ�ϴ�����
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	//��UncaughtException����ʱ��ת�����д�ķ���������
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		
	}
	
	//�Զ���������ռ�������Ϣ
	public boolean handleException(Throwable ex)
	{
		if(ex == null)
			return false;
		Util.writedata(ex.getCause().getMessage());
		new  Thread(){
				public void run() 
				{
					Looper.prepare();
					Toast.makeText(mContext, "�ܱ�Ǹ����������쳣�������˳�", 0).show();
					Looper.loop();
				}
		}.start();
		
		//�ռ��豸��Ϣ
		collectDeviceInfo(mContext);
		
		//������־�ļ�
		saveCrashInfo2File(ex);
		return true;
	}
	
	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, String>entry :  info.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" +value + "\r\n");
		}
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		Throwable cause = ex.getCause();
		
		while(cause != null)
		{
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();
		String result = writer.toString();
		sb.append(result);
		
		long timetamp = System.currentTimeMillis();
		String time = format.format(new Date());
		String fileName = "crash-" + time +"-" + timetamp + ".log";
		if(Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
		{
			try
			{
				File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
						+ File.separator +"crash");
				Log.i("CrashHandler", dir.toString());
				Util.writedata("CrashHandler"+dir.toString());
				if(!dir.exists())
					dir.mkdir();
				FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
				fos.write(sb.toString().getBytes());
				fos.close();
				return fileName;
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	//�ռ��豸����
	public void collectDeviceInfo(Context context)
	{
		try{
			PackageManager pm = context.getPackageManager();  //��ð�������
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 
					PackageManager.GET_ACTIVITIES);
			if(pi != null)
			{
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				info.put("versionName", versionName);
				info.put("versionCode", versionCode);
			}
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		Field[] fields = Build.class.getDeclaredFields();
		for(Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				info.put(field.getName(), field.get("").toString());
				Log.d(TAG, field.getName()+":"+field.get(""));
				Util.writedata(field.getName()+":"+field.get(""));
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
			}catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	
}
