package com.miku.toast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view){
        ToastCompat.makeText(this,"Hello",ToastCompat.LENGTH_SHORT).show();
    }
}
