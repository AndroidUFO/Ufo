package com.androidufo.demo.post;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.androidufo.demo.R;
import com.androidufo.ufo.api.annos.inject.Autowired;
import com.androidufo.ufo.api.model.BodyParams;
import com.androidufo.ufo.listener.ResultListener;
import com.androidufo.ufo.model.Error;

public class PostActivity extends AppCompatActivity {

    @Autowired
    private PostApi postApi;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        textView = findViewById(R.id.tvHtml);
        BodyParams params =
                new BodyParams.Builder()
                        .param("fr", "mhd_card")
                        .build();
        postApi.getBaiduInfos(params)
                .execute(new ResultListener<String>() {
                    @Override
                    public void onError(Error error) {
                        textView.setText("请求错误：" + error.getDesc());
                    }

                    @Override
                    public void onResult(String result) {
                        textView.setText(result);
                    }
                });
    }
}
