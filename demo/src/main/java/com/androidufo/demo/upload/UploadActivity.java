package com.androidufo.demo.upload;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.androidufo.demo.R;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.api.model.UploadFileParams;
import com.androidufo.ufo.enums.State;
import com.androidufo.ufo.listener.UploadListener;
import com.androidufo.ufo.model.Error;
import com.androidufo.ufo.model.Progress;
import com.androidufo.ufo.utils.Logger;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener {

    @Autowired
    private UploadApi uploadApi;
    private TextView tvTips;
    private ProgressBar progressBar;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        startBtn = findViewById(R.id.btnStart);
        startBtn.setOnClickListener(this);
        tvTips = findViewById(R.id.tvTips);
        progressBar = findViewById(R.id.pb);
    }

    @Override
    public void onClick(View v) {
        startBtn.setVisibility(View.INVISIBLE);
        // 将上传的文件修改为你测试的文件路径
        UploadFileParams fileParams =
                new UploadFileParams.Builder()
                        .file("/sdcard/AndroidUFO/Download/202008221055039ea697d6d51340b1b797d14a306d5fde.mp4")
                        .build();
        // 由于这里上传文件随便使用了一个url地址，会出现上传错误413，请忽略，使用正确的上传地址没有问题
        uploadApi.uploadFileToDouYin(fileParams)
                .bindLifecycle(this)
                .execute(new UploadListener<String>() {
                    @Override
                    public void onUploading(State uploadState, Progress progress) {
                        switch (uploadState) {
                            case START:
                                tvTips.setText("开始上传");
                                break;
                            case IN_PROGRESS:
                                tvTips.setText("当前上传进度:" + progress.getMBProgress()
                                        + "/" + progress.getMBTotal() + " MB");
                                progressBar.setProgress(progress.percent);
                                break;
                            case COMPLETE:
                                tvTips.setText("上传完成");
                                startBtn.setVisibility(View.VISIBLE);
                                break;
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        tvTips.setText("上传出错：" + error.getDesc());
                        Logger.debug("onError " + error.getDesc());
                        startBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResult(String result) {
                        Logger.debug("onResult " + result);
                    }
                });
    }
}
