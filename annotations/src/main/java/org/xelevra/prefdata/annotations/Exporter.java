package org.xelevra.prefdata.annotations;

import java.lang.reflect.Type;
import java.util.ArrayList;

public interface Exporter {
    ArrayList<String> getKeys();
    Type getFieldType(String key);
    Object getValue(String key);
}
