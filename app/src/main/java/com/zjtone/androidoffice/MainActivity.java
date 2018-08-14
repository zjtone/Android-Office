package com.zjtone.androidoffice;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.wxiwei.office.IOffice;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private IOffice iOffice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RelativeLayout relativeLayout = findViewById(R.id.outlayout);
        Button button = findViewById(R.id.button);
        iOffice = new IOffice() {
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
                File file = MainActivity.this.getExternalFilesDir(null);
                if (file != null) {
                    return file;
                } else {
                    return MainActivity.this.getFilesDir();
                }
            }

            @Override
            public void openFileFinish() {
                relativeLayout.addView(getView(),
                        new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.MATCH_PARENT
                        ));
            }
        };
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri uri = data.getData();
                    if (uri != null) {
                        iOffice.openFile("" + uri.getPath());
                    }
                    break;
            }
        }
    }
}