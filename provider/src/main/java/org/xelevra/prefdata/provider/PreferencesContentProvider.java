package org.xelevra.prefdata.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import org.xelevra.prefdata.annotations.Exporter;

public abstract class PreferencesContentProvider extends ContentProvider {
    public static final String FIELDS = "fields";
    private static final int SELECT_ALL = 1;
    private static final int SELECT_FIELD = 3;

    private UriMatcher uriMatcher;

    protected abstract Exporter getExporter();

    @Override
    public final boolean onCreate() {
        final String authority = "org.xelevra.prefdata." + getContext().getPackageName();
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(authority, FIELDS, SELECT_ALL);
        uriMatcher.addURI(authority, FIELDS + "/*", SELECT_FIELD);
        return true;
    }

    @Override
    public final Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case SELECT_ALL:
                return new ExportedFieldsCursor(getExporter());
            default:
                return ExportedFieldsCursor.empty();
        }
    }

    @Override
    public final String getType(Uri uri) {
        return null;
    }

    @Override
    public final Uri insert(Uri uri, ContentValues values) {
        return uri;
    }

    @Override
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) != SELECT_FIELD) return 0;
        String key = uri.getLastPathSegment();
        String stringValue = (String) values.get("value");
        if (stringValue == null) {
            getExporter().setValue(key, null);
            return 1;
        }

        Object value;
        try {
            switch (getExporter().getFieldType(key).getName()) {
                case "java.lang.Integer":
                case "int":
                    value = Integer.valueOf(stringValue);
                    break;
                case "java.lang.Float":
                case "float":
                    value = Float.valueOf(stringValue);
                    break;
                case "java.lang.Long":
                case "long":
                    value = Long.valueOf(stringValue);
                    break;
                case "java.lang.Boolean":
                case "boolean":
                    value = Boolean.valueOf(stringValue);
                    break;
                case "java.lang.String":
                    value = stringValue;
                    break;
                default:
                    value = null;
            }

            getExporter().setValue(key, value);
            return 1;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
