package org.xelevra.prefdata.annotations;

import java.util.List;

public interface Exporter {
    List<String> getKeys();
    Class getFieldType(String key);
    Object getValue(String key);
    void setValue(String key, Object value);
}
