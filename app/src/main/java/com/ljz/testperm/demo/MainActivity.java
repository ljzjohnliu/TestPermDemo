package com.ljz.testperm.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ljz.testperm.demo.util.FileChooseUtil;
import com.ljz.testperm.demo.util.SystemUtil;

import java.io.File;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final String BRAND_XIAOMI = "Xiaomi";
    public static final String BRAND_ONEPLUS = "OnePlus";
    public static final String BRAND_MEIZU = "Meizu";
    public static final String BRAND_HONOR = "HONOR";
    public static final String BRAND_HUAWEI = "HUAWEI";
    //申请权限的请求码
    static final int REQUESTE = 1;
    // 表单的结果回调
    private final static int FILECHOOSER_RESULTCODE = 1;

    private Button mGetMediaAudioBtn;
    private Button mGetRecorderAudioBtn;
    private Button mGotoFileManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FILECHOOSER_RESULTCODE:
                    Uri uri = data.getData();
                    String chooseFilePath = FileChooseUtil.getInstance(this).getChooseFileResultPath(uri);
                    Log.d(TAG, "选择文件返回：" + chooseFilePath);
//                    sendFileMessage(chooseFilePath);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGetMediaAudioBtn = findViewById(R.id.get_media_audio);
        mGetRecorderAudioBtn = findViewById(R.id.get_record_audio);
        mGotoFileManager = findViewById(R.id.jump_to_filemanager);
        verifyStoragePermissions();

        mGetMediaAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExternalAudioInfo();
                geInternalAudioInfo();
            }
        });
        mGetRecorderAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getPath();
                Log.d(TAG, "onClick: path = " + path);
                getFileName(path);
            }
        });
        mGotoFileManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                intent.setType("*/*");
//                intent.setType("audio/*");
//                intent.setType("video/*");
//                intent.setType("image/*");

                startActivityForResult(intent, FILECHOOSER_RESULTCODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        switch (requestCode) {
            case REQUESTE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: 已经有了存储权限");
                } else {
                    new AlertDialog.Builder(this).
                            setMessage("为了更好的使用该应用，请准予我们使用这些权限。\n" + "请在“设置->应用->TestPermDemo->权限”中开启这些权限。")
                            .setTitle("请允许存储和手机权限")
                            .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            verifyStoragePermissions();
                            for (int i = 0; i < grantResults.length; i++) {
                                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                                    if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i])) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUESTE);
                                        dialog.dismiss();
                                        return;
                                    }
                                }
                            }
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    }).create().show();
                }
                break;
        }
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_read_phone_state = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permission != PackageManager.PERMISSION_GRANTED || permission_read_phone_state != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, REQUESTE);
        } else {
            Log.d(TAG, "verifyStoragePermissions: 已经有了存储权限！");
        }
    }

    public String getPath() {
        File parent = Environment.getExternalStorageDirectory();
        File child = null;
        String brand = SystemUtil.getDeviceBrand();
        Log.w(TAG, "------------brand = " + brand);
        switch (brand) {
            case BRAND_XIAOMI:
                child = new File(parent, "MIUI/sound_recorder");
                break;
            case BRAND_ONEPLUS:
                break;
            case BRAND_MEIZU:
                break;
            case BRAND_HONOR:
            case BRAND_HUAWEI:
                child = new File(parent, "Sounds");
                break;
        }
        if (child != null) {
            Log.d(TAG, "audio recorder files path is : " + child.getPath());
            return child.getPath();
        } else {
            Log.d(TAG, "未覆盖到的机型！");
            return "";
        }

    }

    public static Vector<String> getFileName(String fileAbsolutePath) {
        Vector<String> vecFile = new Vector<String>();
        File file = new File(fileAbsolutePath);
        if (!file.exists()) {
            return null;
        }
        File[] subFile = file.listFiles();
        if (subFile == null) {
            return null;
        }
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            File _file = subFile[iFileLength];
            if (!_file.isDirectory()) {
                String filename = _file.getName();
                Log.e(TAG, "getFileName, 文件名 ： " + filename);
            } else {
                getFileName(_file.getAbsolutePath());
            }
        }
        return vecFile;
    }

    public void getExternalAudioInfo() {
        //存储在sd卡上的音频文件
        Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        int counter = cursor.getCount();
        String title1 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

        Log.w(TAG, "存储在sd卡上的音频文件------------before looping, title = " + title1);
        for (int j = 0; j < counter; j++) {

            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            Log.w(TAG, "title     " + title + "      displayName     " + displayName + "   size     " + size + "    data   " + data);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void geInternalAudioInfo() {
        //存储在手机内部存储器上
        Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        int counter = cursor.getCount();
        String title1 = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

        Log.w(TAG, "存储在手机内部存储器上------------before looping, title = " + title1);
        for (int j = 0; j < counter; j++) {

            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            Log.w(TAG, "title     " + title + "        displayName     " + displayName + "     size     " + size + "   data    " + data);
            cursor.moveToNext();
        }
        cursor.close();
    }
}
