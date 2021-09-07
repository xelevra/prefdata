package org.xelevra.prefdata.processor;

import org.jetbrains.annotations.Nullable;

public interface KeywordDetector {
    @Nullable
    String getKeyword(String literal);
}
