package com.androidufo.demo.configs;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.androidufo.demo.R;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.model.Error;
import com.androidufo.ufo.utils.Logger;

public class ConfigsActivity extends AppCompatActivity {

    @Autowired
    private ConfigsApi configsApi;
    private TextView tvHtml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configs);
        tvHtml = findViewById(R.id.tvHtml);
        configsApi.getBaiduHtml().execute(new ResultListener<String>() {
            @Override
            public void onError(Error error) {
                Logger.debug("onError " + error.getDesc());
                tvHtml.setText("请求错误：" + error.getDesc());
            }

            @Override
            public void onResult(String result) {
                Logger.debug(result);
                tvHtml.setText(result);
            }
        });
    }
}
