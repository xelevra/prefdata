package org.xelevra.prefdata.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import org.xelevra.prefdata.annotations.Exporter;

public abstract class PreferencesContentProvider extends ContentProvider {
    public static final String FIELDS = "fields";
    public static final String NAME = "name";
    private static final int SELECT_ALL = 1;
    private static final int SELECT_NAME = 2;
    private static final int SELECT_FIELD = 3;

    private UriMatcher uriMatcher;

    protected abstract Exporter getExporter();

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("all", FIELDS, SELECT_ALL);
        uriMatcher.addURI("all", NAME, SELECT_NAME);
        uriMatcher.addURI("all", FIELDS + "/#",  SELECT_FIELD);
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
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(uriMatcher.match(uri) != SELECT_FIELD) return 0;
        String key = uri.getLastPathSegment();
        String stringValue = (String) values.get("value");
        if(stringValue == null){
            getExporter().setValue(key, null);
            return 1;
        }

        Object value;
        switch (getExporter().getFieldType(key).toString()){
            case "int":
                value = Integer.valueOf(stringValue);
                break;
            case "float":
                value = Float.valueOf(stringValue);
                break;
            case "long":
                value = Long.valueOf(stringValue);
                break;
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
