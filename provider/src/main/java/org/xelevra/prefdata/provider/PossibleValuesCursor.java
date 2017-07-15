package org.xelevra.prefdata.provider;

import android.database.MatrixCursor;

import org.xelevra.prefdata.annotations.Exporter;

public class PossibleValuesCursor extends MatrixCursor {
    private final Exporter exporter;
    private static final String[] columns = new String[]{"name", "value"};

    public PossibleValuesCursor(Exporter exporter) {
        super(columns);
        this.exporter = exporter;

        for (String key : exporter.getKeys()) {
//            for (String value : exporter.getPossibleValues(key)) {
//                addRow(new Object[]{key, value});
//            }
        }
    }
}
