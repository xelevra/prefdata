package org.xelevra.prefdata.processor;

import org.jetbrains.annotations.Nullable;
import org.xelevra.prefdata.annotations.Keyword;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class KotlinKeywordDetector implements KeywordDetector {
    private Map<String, String> keyWords = new HashMap<>();

    public void registerElement(Element element) {
        Keyword annotation = element.getAnnotation(Keyword.class);
        if(annotation == null) return;

        if(element instanceof VariableElement) {
            keyWords.put(element.getSimpleName().toString(), annotation.value());
        } else if(element instanceof ExecutableElement
                && element.getModifiers().contains(Modifier.STATIC)
                && element.getSimpleName().toString().contains("$annotations")) {
            String literal = element.getSimpleName().toString();
            literal = literal.replace("$annotations", "");
            literal = Character.toLowerCase(literal.charAt(3)) + literal.substring(4); // remove "get" bean

            keyWords.put(literal, annotation.value());
        }
    }

    @Override
    @Nullable
    public String getKeyword(String literal) {
        return keyWords.get(literal);
    }
}
