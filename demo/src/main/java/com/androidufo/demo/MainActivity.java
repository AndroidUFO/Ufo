package com.androidufo.demo;

import android.Manifest;
import android.content.Intent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.androidufo.demo.configs.ConfigsActivity;
import com.androidufo.demo.download.DownloadActivity;
import com.androidufo.demo.get.GetActivity;
import com.androidufo.demo.post.PostActivity;
import com.androidufo.demo.upload.UploadActivity;
import com.androidufo.demo.urlenv.MultipleUrlEnvActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnGet).setOnClickListener(this);
        findViewById(R.id.btnGet).setOnClickListener(this);
        findViewById(R.id.btnPost).setOnClickListener(this);
        findViewById(R.id.btnDownload).setOnClickListener(this);
        findViewById(R.id.btnUpload).setOnClickListener(this);
        findViewById(R.id.btnMultipleUrlEnv).setOnClickListener(this);
        findViewById(R.id.btnHttpConfigs).setOnClickListener(this);
        // 注意：获取sdcard权限，这里为了让代码看起来更加简单，就不特别处理是否授予权限，要使用demo就必须授权
        ActivityCompat.requestPermissions(
                this,
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                CODE
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGet:
                goGet();
                break;
            case R.id.btnPost:
                goPost();
                break;
            case R.id.btnDownload:
                goDownload();
                break;
            case R.id.btnUpload:
                goUpload();
                break;
            case R.id.btnMultipleUrlEnv:
                goMultipleUrlEnv();
                break;
            case R.id.btnHttpConfigs:
                goHttpConfigs();
                break;
        }
    }

    private void goHttpConfigs() {
        go(ConfigsActivity.class);
    }

    private void goMultipleUrlEnv() {
        go(MultipleUrlEnvActivity.class);
    }

    private void goUpload() {
        go(UploadActivity.class);
    }

    private void goDownload() {
        go(DownloadActivity.class);
    }

    private void goPost() {
        go(PostActivity.class);
    }

    private void goGet() {
        go(GetActivity.class);
    }

    private void go(Class<?> act) {
        startActivity(new Intent(this, act));
    }
}
