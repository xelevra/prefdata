package org.xelevra.prefdata.processor;

import org.xelevra.prefdata.annotations.Belongs;

import java.util.function.Predicate;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public class BelongsFieldValidator {
    private final ProcessingEnvironment processingEnv;

    public BelongsFieldValidator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void validateField(VariableElement field) {
        String[] possibleValues = getPossibleValues(field);
        Predicate<String> typePredicate = pickUpTypePredicate(field);
        validateType(field, possibleValues, typePredicate);
    }

    private String[] getPossibleValues(VariableElement field) {
        Belongs annotation = field.getAnnotation(Belongs.class);
        if (annotation == null) return new String[]{};
        return annotation.value();
    }

    private Predicate<String> pickUpTypePredicate(VariableElement field) {
        switch (field.asType().toString()) {
            case "int":
                return this::isInt;
            case "float":
                return this::isFloat;
            case "long":
                return this::isLong;
            case "boolean":
                return this::isBoolean;
            case "java.lang.String":
                return value -> true;
            default:
                error(field, "Unsupported type " + field.asType().toString());
                return value -> false;
        }
    }

    private void validateType(VariableElement field, String[] possibleValues, Predicate<String> typePredicate) {
        for (String value : possibleValues) {
            if (!typePredicate.test(value)) {
                error(field, field.getSimpleName() + " is " + field.asType().toString() + ", but one of possible values is " + value + " (" + guessType(value) + ")");
            }
        }
    }

    private String guessType(String value) {
        if (isInt(value)) return "int";
        else if (isLong(value)) return "long";
        else if (isFloat(value)) return "float";
        else if (isBoolean(value)) return "boolean";
        else return "string";
    }

    private boolean isInt(String value) {
        return isParseable(() -> Integer.parseInt(value));
    }

    private boolean isLong(String value) {
        return isParseable(() -> Long.parseLong(value));
    }

    private boolean isFloat(String value) {
        return isParseable(() -> Float.parseFloat(value));
    }

    private boolean isParseable(Runnable parseAction) {
        try {
            parseAction.run();
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean isBoolean(String value) {
        return value.matches("(?i)true|false");
    }

    private void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
