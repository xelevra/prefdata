package org.xelevra.prefdata.browser;

import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.xelevra.prefdata.browser.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private List<KeyValueType> list;
    private List<ProviderInfo> providers = new ArrayList<>();

    private ProviderInfo connectedProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.bAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exploreProviders();
            }
        });

        if (savedInstanceState != null)
            connectedProvider = savedInstanceState.getParcelable("connected");

        if (connectedProvider == null) exploreProviders();
        else bindProvider(connectedProvider);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("connected", connectedProvider);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (connectedProvider != null) exploreProviders();
        else super.onBackPressed();
    }

    private void exploreProviders() {
        binding.bAction.hide();
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(R.string.app_name);
        connectedProvider = null;
        providers.clear();
        binding.lvContent.setAdapter(new DataBindingListAdapter<>(providers, R.layout.item_provider, BR.provider));
        binding.lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProviderInfo provider = providers.get(position);
                bindProvider(provider);
            }
        });
        for (PackageInfo pack : getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS)) {
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                for (ProviderInfo provider : providers) {
                    if (provider.authority.equals("org.xelevra.prefdata." + provider.packageName)) {
                        addProvider(provider);
                    }
                }
            }
        }
    }

    private void addProvider(ProviderInfo provider) {
        providers.add(provider);
        ((DataBindingListAdapter) binding.lvContent.getAdapter()).notifyDataSetChanged();
    }

    private void bindProvider(ProviderInfo providerInfo) {
        binding.bAction.show();
        connectedProvider = providerInfo;
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(
                " " + getPackageManager().getApplicationLabel(connectedProvider.applicationInfo)
        );
        try {
            getSupportActionBar().setLogo(
                    scaleIcon(getPackageManager().getApplicationIcon(connectedProvider.packageName))
            );
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Browser", Log.getStackTraceString(e));
        }

        binding.lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(list.get(position));
            }
        });

        update();
    }


    private void update() {
        Cursor cursor = getContentResolver().query(Uri.parse(baseUri() + "fields"), null, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
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
        switch (keyValueType.type) {
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

    void updateField(String field, String value) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put("value", value);
        if (getContentResolver().update(Uri.parse(baseUri() + "/fields/" + field), contentValues, null, null) == 0) {
            Toast.makeText(this, "Data error", Toast.LENGTH_SHORT).show();
        } else {
            update();
        }
    }

    private String baseUri() {
        return "content://" + connectedProvider.authority + "/";
    }

    @BindingAdapter("bind:appIcon")
    public static void setIconFromPackage(ImageView view, String pack) {
        try {
            view.setImageDrawable(view.getContext().getPackageManager().getApplicationIcon(pack));
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException("Cannot found icon for package", e);
        }
    }


    @BindingAdapter("bind:appName")
    public static void setAppNameFromAppInfo(TextView view, ApplicationInfo applicationInfo) {
        view.setText(view.getContext().getPackageManager().getApplicationLabel(applicationInfo));
    }

    private Drawable scaleIcon(Drawable source) {
        int size = getResources().getDimensionPixelSize(R.dimen.logo_size);
        return new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                ((BitmapDrawable) source).getBitmap(),
                size,
                size,
                true
        ));
    }
}
