package com.androidufo.demo.get;

import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.androidufo.demo.R;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.api.model.QueryParams;
import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.model.Error;

public class GetActivity extends AppCompatActivity {

    @Autowired
    private GetApi getApi;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get);
        textView = findViewById(R.id.tvHtml);
        QueryParams params =
                new QueryParams.Builder()
                        .param("fr", "mhd_card")
                        .build();
        getApi.getBaiduHtml(params)
                .bindLifecycle(this)
                .execute(new ResultListener<String>() {
                    @Override
                    public void onError(Error error) {
                        textView.setText("访问失败：" + error.getDesc());
                    }

                    @Override
                    public void onResult(String result) {
                        textView.setText(result);
                    }
                });
    }
}
