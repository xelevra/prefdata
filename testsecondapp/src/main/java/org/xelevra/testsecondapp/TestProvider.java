package org.xelevra.testsecondapp;

import android.content.Context;

import org.xelevra.prefdata.annotations.Exporter;
import org.xelevra.prefdata.provider.PreferencesContentProvider;

public class TestProvider extends PreferencesContentProvider {
    @Override
    protected Exporter getExporter() {
        return new PrefTTT(getContext().getSharedPreferences("r", Context.MODE_PRIVATE));
    }
}
