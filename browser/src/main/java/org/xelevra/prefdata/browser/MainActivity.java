package org.xelevra.prefdata.browser;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.xelevra.prefdata.browser.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Cursor cursor = getContentResolver().query(Uri.parse("content://all/all"), null, null, null, null);
        startManagingCursor(cursor);

        Toast.makeText(this, "Size " + cursor.getCount(), Toast.LENGTH_SHORT).show();

        List<KeyValue> list = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()){
            list.add(new KeyValue(cursor.getString(0), cursor.getString(1)));
        }

        binding.lvContent.setAdapter(new DataBindingListAdapter<>(list, R.layout.item_content, BR.entity));
    }
}
