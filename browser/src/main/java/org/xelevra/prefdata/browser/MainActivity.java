package org.xelevra.prefdata.browser;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cursor cursor = getContentResolver().query(Uri.parse("content://all/all"), null, null, null, null);
        startManagingCursor(cursor);

        Toast.makeText(this, "Size " + cursor.getCount(), Toast.LENGTH_SHORT).show();


    }
}
