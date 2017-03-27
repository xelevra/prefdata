package org.xelevra.prefdata.browser;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.xelevra.prefdata.browser.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        update();
    }

    private void update(){
        Cursor name = getContentResolver().query(Uri.parse("content://all/name"), null, null, null, null);
        if(name.getCount() != 0){
            name.moveToFirst();
            getSupportActionBar().setTitle(name.getString(0));
        }
        name.close();

        Cursor cursor = getContentResolver().query(Uri.parse("content://all/all"), null, null, null, null);

        if(cursor.getCount() == 0){
            Toast.makeText(this, "No exportable data found", Toast.LENGTH_SHORT).show();
        }
        List<KeyValue> list = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()){
            list.add(new KeyValue(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();

        binding.lvContent.setAdapter(new DataBindingListAdapter<>(list, R.layout.item_content, BR.entity));
    }
}
