package org.xelevra.prefdata.provider;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.xelevra.prefdata.annotations.Exporter;

public class ExportedFieldsCursor extends MatrixCursor {
    private final Exporter exporter;
    private static final String[] columns = new String[]{"name", "value"};

    public static Cursor empty(){
        return new MatrixCursor(columns);
    }

    public ExportedFieldsCursor(Exporter exporter) {
        super(columns);
        this.exporter = exporter;

        for (String key : exporter.getKeys()) {
            addRow(new Object[]{key, exporter.getValue(key)});
        }
    }
}
