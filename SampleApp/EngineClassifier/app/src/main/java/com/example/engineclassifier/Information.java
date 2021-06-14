package com.example.engineclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class Information extends AppCompatActivity {

    WebView infoWebView;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        infoWebView = findViewById(R.id.web_view);
        url = getIntent().getExtras().getString("url");
        if(url.equals("---")){
            infoWebView.loadUrl("google.com");
        }
        else{
            infoWebView.loadUrl(url);

        }

    }
}