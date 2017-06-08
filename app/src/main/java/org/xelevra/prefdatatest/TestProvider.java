package org.xelevra.prefdatatest;

import android.content.Context;
import android.util.Log;

import org.xelevra.prefdata.annotations.Exporter;
import org.xelevra.prefdata.provider.PreferencesContentProvider;

public class TestProvider extends PreferencesContentProvider {
    @Override
    protected Exporter getExporter() {
        return new PrefTest(getContext().getSharedPreferences("r", Context.MODE_PRIVATE));
    }
}
