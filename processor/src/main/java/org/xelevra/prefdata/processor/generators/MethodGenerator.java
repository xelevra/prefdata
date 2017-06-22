package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Keyword;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public abstract class MethodGenerator {
    protected final TypeName generatedTypename;
    protected final TypeSpec.Builder classBuilder;
    private final ProcessingEnvironment processingEnv;

    public MethodGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        this.processingEnv = processingEnv;
        classBuilder = builder;
        this.generatedTypename = ClassName.bestGuess(builder.build().name);
    }

    public abstract void processField(VariableElement field);

    protected boolean check(VariableElement field) {
        if(field.getModifiers().contains(Modifier.PRIVATE) || field.getModifiers().contains(Modifier.PUBLIC)){
            error(field, "must be protected or package private");
        }
        switch (field.asType().toString()){
            case "int":
            case "float":
            case "long":
            case "boolean":
            case "java.lang.String":
                return true;
            default:
                error(field, "Unsupported type " + field.asType().toString());
                return false;
        }
    }

    protected String generateMethodName(VariableElement field, String method){
        if("get".equals(method) && (field.asType().toString().equals("boolean"))){
            method = "is";
        }

        String fieldName = field.getSimpleName().toString();
        return method + (Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1, fieldName.length()));

    }

    protected String getKeyword(VariableElement field){
        Keyword annotation = field.getAnnotation(Keyword.class);
        if(annotation != null) return annotation.value();
        return field.getSimpleName().toString();
    }

    protected void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    protected void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }
}
