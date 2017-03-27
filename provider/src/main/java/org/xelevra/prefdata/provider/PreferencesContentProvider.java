package org.xelevra.prefdata.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import org.xelevra.prefdata.annotations.Exporter;

public abstract class PreferencesContentProvider extends ContentProvider {
    public static final String ALL = "all";
    private static final int SELECT_ALL = 1;

    private UriMatcher uriMatcher;

    protected abstract Exporter getExporter();

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ALL, ALL, SELECT_ALL);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(uriMatcher.match(uri) != SELECT_ALL) return ExportedFieldsCursor.empty();
        return new ExportedFieldsCursor(getExporter());
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
}
