package com.zjtone.androidoffice;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wxiwei.office.IOffice;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IOffice iOffice = new IOffice() {
            @Override
            public Activity getActivity() {
                return MainActivity.this;
            }

            @Override
            public String getAppName() {
                return "ioffice";
            }

            @Override
            public File getTemporaryDirectory() {
                return null;
            }

            @Override
            public void openFileFinish() {
            }
        };
    }
}