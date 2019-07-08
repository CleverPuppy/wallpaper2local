package com.dph.wallpaper2local;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    WallpaperManager wallpaperManager;
    Drawable wallpaper;
    final int MY_PERMISSION_REQUEST_CODE = 1001;
    Button btn_apply;
    ImageView imageView;


    public Activity getCurrentActivity()
    {
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(checkPermission() == false)
        {
            applyForPermission();
            Toast _t = Toast.makeText(this,"请求文件读写权限",Toast.LENGTH_LONG);
            _t.show();
        }
        else {
            btn_apply = (Button) this.findViewById(R.id.apply_btn);
            imageView = (ImageView) findViewById(R.id.image_view);
            final Toast t = (Toast) Toast.makeText(this, "没有权限", Toast.LENGTH_LONG);
            wallpaperManager = (WallpaperManager) WallpaperManager.getInstance(getApplicationContext());
            wallpaper = wallpaperManager.getDrawable();
            imageView.setImageDrawable(this.wallpaper);

//        检查存储器状态
            String ESState = Environment.getExternalStorageState();
            if (!ESState.equals(Environment.MEDIA_MOUNTED)) {
                Log.e("ERROR", "onCreate: ESSstate error");
                return;
            }

//        保存路径
            String subdir = "/wallpaper2local/";
            final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + subdir;
//        保存文件名
            final String filename = UUID.randomUUID().toString();// 随机命名

            btn_apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bitmap bmp = ((BitmapDrawable) wallpaper).getBitmap();

                    if (checkPermission() == false) {
                        t.show();
                        applyForPermission();
                    } else {
//                保存到本地文件
                        String path = dir + filename + ".jpg";
                        Uri _path_uri = Uri.parse(path);
                        try {
                            File file = new File(_path_uri.getPath());
                            if (!file.exists()) {
                                file.getParentFile().mkdirs();
                                file.createNewFile();
                            }

                            FileOutputStream out = new FileOutputStream(file);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

//                    发送广播通知系统更新数据库
                            Uri uri = Uri.fromFile(file);
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

                            Toast _t = Toast.makeText(getBaseContext(), "file saved to " + uri.getPath().toString() + "\n，程序关闭", Toast.LENGTH_LONG);
                            _t.show();

//                    保存完毕后退出APP
                            getCurrentActivity().finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
    }
    private boolean checkPermission(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }
    private void applyForPermission(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        },MY_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.recreate();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
