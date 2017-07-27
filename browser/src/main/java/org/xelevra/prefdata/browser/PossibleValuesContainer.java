package org.xelevra.prefdata.browser;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PossibleValuesContainer {
    private final ContentResolver contentResolver;
    private final Map<Pair<String, String>, List<String>> cachedValues = new HashMap<>();

    public PossibleValuesContainer(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    //todo execute it out of ui thread
    public List<String> get(String baseUri, KeyValueType keyValueType) {
        Pair<String, String> cacheKey = Pair.create(baseUri, keyValueType.key);
        if (!cachedValues.containsKey(cacheKey)) {
            cachedValues.put(cacheKey, retrievePossibleValues(baseUri, keyValueType));
        }
        return cachedValues.get(cacheKey);
    }

    private List<String> retrievePossibleValues(String baseUri, KeyValueType keyValueType) {
        Cursor cursor = contentResolver.query(Uri.parse(baseUri + "values"), null, "name = ?", new String[] {keyValueType.key}, null);

        if (cursor == null) {
            return new ArrayList<>();
        } else {
            List<String> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                result.add(cursor.getString(1));
            }
            cursor.close();
            return result;
        }
    }
}
