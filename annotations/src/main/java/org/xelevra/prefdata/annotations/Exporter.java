package org.xelevra.prefdata.annotations;

import java.util.List;

public interface Exporter {
    List<String> getKeys();
    List<String> getPossibleValues(String key);
    Class getFieldType(String key);
    Object getValue(String key);
    void setValue(String key, Object value);
}
