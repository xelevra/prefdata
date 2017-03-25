package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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
        if(!field.getModifiers().contains(Modifier.PROTECTED)){
            error(field, "must be protected");
        }
        switch (field.asType().toString()){
            case "java.lang.Integer":
            case "int":
            case "java.lang.Float":
            case "float":
            case "java.lang.Long":
            case "long":
            case "java.lang.Boolean":
            case "boolean":
            case "java.lang.String":
                return true;
            default:
                error(field, "Unsupported type " + field.asType().toString());
                return false;
        }
    }

    public String generateName(VariableElement field, boolean isSetter){
        String prefix;
        if(isSetter){
            prefix = "set";
        } else {
            boolean startWithIs = field.asType().toString().equals("java.lang.Boolean") || field.asType().toString().equals("boolean");
            prefix = startWithIs ? "is" : "get";
        }

        String fieldName = field.getSimpleName().toString();
        return prefix + (Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1, fieldName.length()));

    }

    protected void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    protected void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }
}
