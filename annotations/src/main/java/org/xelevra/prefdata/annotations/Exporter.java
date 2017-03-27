package org.xelevra.prefdata.annotations;

import java.lang.reflect.Type;
import java.util.List;

public interface Exporter {
    List<String> getKeys();
    Type getFieldType(String key);
    Object getValue(String key);
    void setValue(String key, Object value);
}
