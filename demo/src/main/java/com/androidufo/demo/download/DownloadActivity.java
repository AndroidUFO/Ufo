package com.androidufo.demo.download;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.androidufo.demo.R;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.enums.State;
import com.androidufo.ufo.listener.DownloadListener;
import com.androidufo.ufo.model.Error;
import com.androidufo.ufo.model.Progress;
import com.androidufo.ufo.utils.Logger;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    @Autowired
    private DownloadApi downloadApi;
    private TextView tvTips;
    private ProgressBar progressBar;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        startBtn = findViewById(R.id.btnStart);
        startBtn.setOnClickListener(this);
        tvTips = findViewById(R.id.tvTips);
        progressBar = findViewById(R.id.pb);
    }

    @Override
    public void onClick(View v) {
        startBtn.setVisibility(View.INVISIBLE);
        downloadApi.downloadApk("https://s9.pstatp.com/package/apk/aweme/120801/aweme_aweGW_v120801_41b4_1600163228.apk?v=1600163232")
                .bindLifecycle(this)
                .execute(new DownloadListener() {
                    @Override
                    public void onDownloading(State downloadState, String fileName, Progress progress) {
                        switch (downloadState) {
                            case START:
                                tvTips.setText("开始下载");
                                break;
                            case IN_PROGRESS:
                                tvTips.setText("当前下载进度:" + progress.getMBProgress()
                                        + "/" + progress.getMBTotal() + " MB");
                                progressBar.setProgress(progress.percent);
                                break;
                            case COMPLETE:
                                tvTips.setText("下载完成");
                                startBtn.setVisibility(View.VISIBLE);
                                break;
                        }
                    }


                    @Override
                    public void onDownloadFailed(Error error) {
                        tvTips.setText("下载出错：" + error.getDesc());
                        Logger.debug("onError " + error.getDesc());
                        startBtn.setVisibility(View.VISIBLE);
                    }
                });
    }
}
