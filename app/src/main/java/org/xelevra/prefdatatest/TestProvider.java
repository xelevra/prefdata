package org.xelevra.prefdatatest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.xelevra.prefdata.annotations.Exporter;
import org.xelevra.prefdata.provider.PreferencesContentProvider;

public class TestProvider extends PreferencesContentProvider {
    @Override
    protected Exporter getExporter() {
        Log.d("#####", "Батрачит" );
        return new PrefTest(getContext().getSharedPreferences("r", Context.MODE_PRIVATE));
    }
}
