package org.xelevra.prefdatatest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("r", MODE_PRIVATE);
        PrefTest test = new PrefTest(prefs);

//        test.edit().setAge("12", "Vana").setAge("14","Vita").commit();
//        ((TextView) findViewById(R.id.tv_word)).setText("Vana: " + test.getAge("Vana") + "Vita" + test.getAge("Vita"));

    }

    @Override
    protected void onDestroy() {
//        test.setText("Time: " + System.currentTimeMillis());
        super.onDestroy();
    }
}
