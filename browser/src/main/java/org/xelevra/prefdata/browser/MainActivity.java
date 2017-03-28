package org.xelevra.prefdata.browser;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.xelevra.prefdata.browser.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private List<KeyValueType> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(list.get(position));
            }
        });
        update();
    }

    private void update() {
        Cursor name = getContentResolver().query(Uri.parse("content://all/name"), null, null, null, null);
        if (name.getCount() != 0) {
            name.moveToFirst();
            getSupportActionBar().setTitle(name.getString(0));
        }
        name.close();

        Cursor cursor = getContentResolver().query(Uri.parse("content://all/fields"), null, null, null, null);

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No exportable data found", Toast.LENGTH_SHORT).show();
        }
        list = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            list.add(new KeyValueType(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();

        binding.lvContent.setAdapter(new DataBindingListAdapter<>(list, R.layout.item_content, BR.entity));
    }

    void showEditDialog(final KeyValueType keyValueType) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(keyValueType.key);
        switch (keyValueType.type){
            case "java.lang.Boolean":
            case "boolean":
                builder.checkBoxPrompt(
                        "Set",
                        "true".equals(keyValueType.value),
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                updateField(keyValueType.key, isChecked ? "true" : "false");
                            }
                        }
                ).show();
                return;
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
                builder.inputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "java.lang.Float":
            case "float":
                builder.inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            break;
            case "java.lang.String":
                builder.inputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
        builder.input(null, keyValueType.value, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        updateField(keyValueType.key, input.toString().trim());
                    }
                }).show();
    }

    void updateField(String field, String value){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("value", value);
        if(getContentResolver().update(Uri.parse("content://all/fields/" + field), contentValues, null, null) == 0){
            Toast.makeText(this, "Data error", Toast.LENGTH_SHORT).show();
        } else {
            update();
        }
    }
}
