package org.xelevra.prefdata.provider;

import android.database.MatrixCursor;
import android.net.Uri;

import org.xelevra.prefdata.annotations.Exporter;

import java.util.List;

class PossibleValuesCursor extends MatrixCursor {
    private final Exporter exporter;
    private static final String[] columns = new String[]{"name", "value"};

    public PossibleValuesCursor(Exporter exporter, Uri uri) {
        super(columns);
        this.exporter = exporter;

        List<String> uriSegments = uri.getPathSegments();
        String key = uriSegments.get(uriSegments.size() - 2);
        for (String value : exporter.getPossibleValues(key)) {
            addRow(new Object[]{key, value});
        }
    }
}
