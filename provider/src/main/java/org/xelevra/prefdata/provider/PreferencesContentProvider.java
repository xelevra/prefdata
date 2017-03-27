package org.xelevra.prefdata.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import org.xelevra.prefdata.annotations.Exporter;

public abstract class PreferencesContentProvider extends ContentProvider {
    public static final String ALL = "all";
    public static final String NAME = "name";
    private static final int SELECT_ALL = 1;
    private static final int SELECT_NAME = 2;

    private UriMatcher uriMatcher;

    protected abstract Exporter getExporter();

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ALL, ALL, SELECT_ALL);
        uriMatcher.addURI(ALL, NAME, SELECT_NAME);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case SELECT_NAME:
                return getName();
            case SELECT_ALL:
                return new ExportedFieldsCursor(getExporter());
            default:
                return ExportedFieldsCursor.empty();
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private Cursor getName() {
        ApplicationInfo applicationInfo = getContext().getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getContext().getString(stringId);
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"name"});
        matrixCursor.addRow(new Object[]{appName});
        return matrixCursor;
    }
}
