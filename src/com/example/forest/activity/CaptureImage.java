package com.example.forest.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.forest.R;
import com.example.forest.dialog.ProgressBar;
import com.example.forest.util.Const;
import com.example.forest.util.DBManager;
import com.example.forest.util.Forest;
import com.example.forest.util.PicUtil;
import com.example.forest.util.Util;

public class CaptureImage extends Activity {
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    ProgressBar progressbar;

    Button take;
    SurfaceView sView;
    SurfaceHolder surfaceHolder;
    int screenWidth, screenHeight;
    // ����ϵͳ���õ������
    Camera camera;
    // �Ƿ��������
    boolean isPreview = false;

    Bundle from;

    // ��Ƭ���Ͳ��溦�����е����ں�ʱ��
    String datetime;

    // ��Ƭ��Ӧ�ľ�������
    String type;

    // ͬһ��λ��Ϣ��Ӧ�Ķ�����Ƭ�����
    int count = 0;

    static String photoName;

    Handler handler;

    // ����λͼ
    Bitmap bm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,
                FLAG_HOMEKEY_DISPATCHED);
        setContentView(R.layout.main);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Intent intent = getIntent();
        from = intent.getExtras();

        datetime = from.getString("datetime");
        type = from.getString("type");

        Display display = wm.getDefaultDisplay();
        // ��ȡ��Ļ�Ŀ�͸�
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        // ��ȡ������SurfaceView���
        sView = (SurfaceView) findViewById(R.id.sView);

        take = (Button) findViewById(R.id.take);
        take.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // �� ��
                camera.takePicture(null, null, myjpegCallback);
            }
        });

        // ���SurfaceView��SurfaceHolder
        surfaceHolder = sView.getHolder();
        // ΪsurfaceHolder���һ���ص�������
        surfaceHolder.addCallback(new Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                    int width, int height) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // ������ͷ
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // ���camera��Ϊnull ,�ͷ�����ͷ
                if (camera != null) {
                    if (isPreview)
                        camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });
        // ���ø�SurfaceView�Լ���ά������
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // �ϴ����ȿ���ʾ
        progressbar = new ProgressBar(CaptureImage.this, R.style._ProgressBar);
        progressbar.setContentView(R.layout.progress);
        progressbar.init(progressbar, "�����ϴ�ͼƬ������");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // ��ʾ������
                if (msg.what == Const.SHOWDIALOG) {
                    progressbar.show();
                }
                // ��ͼƬ���ͳ�ȥ����ȡ���������������粻ͨ��һֱ��ʾ������
                else if (msg.what == Const.SENDED) {
                    progressbar.cancel();
                }
                // ��ֹ���գ���ת���ĸ����棿����
                else if (msg.what == Const.FINISH) {
                    CaptureImage.this.finish();
                }
            }
        };
    }

    private void initCamera() {
        if (!isPreview) {
            camera = Camera.open();
        }
        if (camera != null && !isPreview) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                // ����Ԥ����Ƭ�Ĵ�С
                parameters.setPreviewSize(screenHeight, screenWidth);
                // ÿ����ʾ4֡
                parameters.setPreviewFrameRate(4);
                // ����ͼƬ��ʽ
                parameters.setPictureFormat(PixelFormat.JPEG);
                // ����JPG��Ƭ������
                parameters.set("jpeg-quality", 85);
                // ������Ƭ�Ĵ�С
                parameters.setPictureSize(screenHeight, screenWidth);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                // ͨ��SurfaceView��ʾȡ������
                camera.setPreviewDisplay(surfaceHolder);
                // ��ʼԤ��
                camera.startPreview();
                // �Զ��Խ�
                camera.autoFocus(null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            isPreview = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        // ���û�����������������ʱִ������
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_CAMERA:
                if (camera != null && event.getRepeatCount() == 0) {
                    // ����
                    camera.takePicture(null, null, myjpegCallback);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_HOME:
                System.out.println("HOME��������");
        }
        return super.onKeyDown(keyCode, event);
    }

    PictureCallback myjpegCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // �����������õ����ݴ���λͼ
            final Bitmap bm0 = BitmapFactory.decodeByteArray(data, 0,
                    data.length);

            Matrix m = new Matrix();
            m.setRotate(90, (float) bm0.getWidth() / 2,
                    (float) bm0.getHeight() / 2);
            bm = Bitmap.createBitmap(bm0, 0, 0, bm0.getWidth(),
                    bm0.getHeight(), m, true);

            // ����/layout/save.xml�ļ���Ӧ�Ĳ�����Դ
            View saveDialog = getLayoutInflater().inflate(R.layout.save, null);

            // ��ȡsaveDialog�Ի����ϵ�ImageView���
            ImageView show = (ImageView) saveDialog.findViewById(R.id.show);
            // ��ʾ�ո��ĵõ���Ƭ
            show.setImageBitmap(bm);

            // ʹ�öԻ�����ʾsaveDialog���
            new AlertDialog.Builder(CaptureImage.this).setView(saveDialog)
                    .setPositiveButton("��������", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /**
                             * @date 20141017
                             * @content �Ż�����
                             */
                            uoloadPhoto();

                        }
                    }).setNegativeButton("���", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            // ɾ����ǰ���ѿ�
                            arg0.dismiss();

                            uoloadPhoto();

                            // ����pests������������ɺ�ת��pestDetailҳ��
                            if (from != null && from.getBoolean("pests")) {
                                DBManager dbManager = new DBManager(
                                        CaptureImage.this);
                                dbManager.openDatabase();
                                boolean hasPestsInfo = dbManager
                                        .haspestsKinds();
                                dbManager.closeDatabase();
                                if (hasPestsInfo) {
                                    Bundle data = new Bundle();
                                    data.putString("datetime", datetime);
                                    Intent intent = new Intent(
                                            CaptureImage.this,
                                            PestsDetail.class);
                                    intent.putExtras(data);
                                    intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                    startActivity(intent);
                                    intent = null;
                                }
                            }
                            else {
                                Intent intent = new Intent(CaptureImage.this,
                                        Main.class);
                                intent.setFlags(intent.FLAG_ACTIVITY_NO_USER_ACTION);
                                startActivity(intent);
                                intent = null;
                            }
                            handler.sendEmptyMessage(Const.FINISH);
                        }
                    }).show();

            // �������
            camera.stopPreview();
            camera.startPreview();
            isPreview = true;
        }

    };

    // public void stopCamera() {
    // // TODO Auto-generated method stub
    // camera.stopPreview();
    //
    // }
    //
    // public void resetCamera() {
    // // TODO Auto-generated method stub
    // if (camera != null && isPreview) {
    // camera.stopPreview();
    // camera.release();
    // camera = null;
    // isPreview = false;
    // }
    // }

    /**
     * @date 20141017
     * @content �Ż�����
     * @return
     */
    public void uoloadPhoto() {

        handler.sendEmptyMessage(Const.SHOWDIALOG);

        StringBuilder photoNameStr = new StringBuilder();
        // photoNameStr.append(Forest.config_preferences.getString("phoneID",
        // "xxxxxxxx"));
        photoNameStr.append(Util.phoneID);
        photoNameStr.append(type);
        photoNameStr.append(datetime);
        photoNameStr.append("E");
        photoNameStr.append(customFormat(Const.cur_location.getLongitude()));
        photoNameStr.append("N");
        photoNameStr.append(customFormat(Const.cur_location.getLatitude()));
        photoNameStr.append(count);
        count++;

        photoName = photoNameStr.toString();
        Log.d("Forest-------", photoName);

        // ����һ��λ��SD���ϵ��ļ�
        final File file = new File(Environment.getExternalStorageDirectory()
                + "/forest/msg/", photoName + ".jpg");

        Util.newName = photoName + ".jpg";

        FileOutputStream outStream = null;
        try {
            // ��ָ���ļ���Ӧ�������
            outStream = new FileOutputStream(file);

            // ѹ��Ȼ���ű���
            PicUtil.comp(bm).compress(CompressFormat.JPEG, 50, outStream);
            outStream.close();

            Thread sendPicThread = new Thread() {
                @Override
                public void run() {
                    if (Util.uploadFile(file))// �ϴ����
                    {
                        handler.sendEmptyMessage(Const.SENDED);
                        file.delete();
                    }
                    else // δ�ϴ��ɹ��Ļ������ļ������浽���ݿ�
                    {
                        DBManager dbManager = new DBManager(CaptureImage.this);
                        dbManager.openDatabase();
                        dbManager.insert_photos(photoName, 0);
                        dbManager.closeDatabase();
                        /**
                         * @date 20141017
                         * @content �������´��룺δ�ϴ��ɹ�Ҳ����������
                         */
                    }
                };
            };
            if (Forest.isNetConnect(CaptureImage.this)) {
                sendPicThread.start();
            }
            else {
                DBManager dbManager = new DBManager(CaptureImage.this);
                dbManager.openDatabase();
                dbManager.insert_photos(photoName, 0);
                dbManager.closeDatabase();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ��double������ʽ�����
    public String customFormat(double value) {
        String pattern = "0000000000000000";
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(value * Math.pow(10, 13));
        return output;
    }

}
