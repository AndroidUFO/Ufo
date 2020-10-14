package com.androidufo.demo.urlenv;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.androidufo.demo.R;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.api.urlenv.UrlEnvConfig;
import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.model.Error;

public class MultipleUrlEnvActivity extends AppCompatActivity {

    public static final String QQ = "qq";
    public static final String QQ_HOME = "https://www.qq.com/";

    @Autowired
    private UrlEnvApi urlEnvApi;
    private TextView tvHtml;
    private Button btnQQ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_url_env);
        tvHtml = findViewById(R.id.tvHtml);
        findViewById(R.id.btnBaidu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change2BaiduAndGetHtml();
            }
        });
        findViewById(R.id.btnSina).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change2SinaAndGetHtml();
            }
        });
        btnQQ = findViewById(R.id.btnQQ);
        btnQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change2QQAndGetHtml();
            }
        });
        findViewById(R.id.btnAddQQ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 动态新增url环境
                urlEnvApi.addUrlEnvConfig(new UrlEnvConfig(QQ, QQ_HOME));
                btnQQ.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
            }
        });
        getWebHtmlText();
    }

    private void change2QQAndGetHtml() {
        try {
            urlEnvApi.switchUrlEnvConfig(QQ);
            getWebHtmlText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void change2SinaAndGetHtml() {
        try {
            urlEnvApi.switchUrlEnvConfig(UrlEnvApi.SINA);
            getWebHtmlText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void change2BaiduAndGetHtml() {
        try {
            urlEnvApi.switchUrlEnvConfig(UrlEnvApi.BAIDU);
            getWebHtmlText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getWebHtmlText() {
        try {
            urlEnvApi.getWebHomeHtml().execute(new ResultListener<String>() {
                @Override
                public void onError(Error error) {
                    tvHtml.setText("请求错误：" + error.getDesc());
                }

                @Override
                public void onResult(String result) {
                    tvHtml.setText(result);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
