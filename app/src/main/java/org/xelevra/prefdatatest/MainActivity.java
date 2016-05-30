package org.xelevra.prefdatatest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Test test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test = new PrefTest(getSharedPreferences("r", MODE_PRIVATE));

        ((TextView) findViewById(R.id.tv_word)).setText(test.getText());

    }

    @Override
    protected void onDestroy() {
        test.setText("Time: " + System.currentTimeMillis());
        super.onDestroy();
    }
}
