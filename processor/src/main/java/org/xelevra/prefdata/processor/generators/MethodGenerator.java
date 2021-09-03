package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Keyword;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public abstract class MethodGenerator {
    protected final TypeName generatedTypename;
    protected final TypeName baseTypename;
    protected final TypeSpec.Builder classBuilder;
    private final ProcessingEnvironment processingEnv;

    public MethodGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        this.processingEnv = processingEnv;
        classBuilder = builder;
        TypeSpec tmpTypeSpec = builder.build();
        this.generatedTypename = ClassName.bestGuess(tmpTypeSpec.name);
        baseTypename = tmpTypeSpec.superclass != null ? tmpTypeSpec.superclass : tmpTypeSpec.superinterfaces.get(0);
    }

    public abstract void processField(VariableElement field);

    public abstract void processMethod(ExecutableElement method);

    protected final boolean checkField(VariableElement field) {
        if(field.getModifiers().contains(Modifier.PRIVATE) || field.getModifiers().contains(Modifier.PUBLIC) || field.getModifiers().contains(Modifier.FINAL)){
            error(field, "must not be final, private and public");
            return false;
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

    protected boolean checkMethod(ExecutableElement method) {
        if(method.getModifiers().contains(Modifier.PRIVATE) || method.getModifiers().contains(Modifier.FINAL)){
            error(method, "must not be final or private");
            return false;
        }
        return true;
    }

    protected String generateMethodName(VariableElement field, String method){
        if("get".equals(method) && (field.asType().toString().equals("boolean"))){
            method = "is";
        }

        String fieldName = field.getSimpleName().toString();
        return method + (Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1, fieldName.length()));

    }

    /**
     * @return String key for preferences
     */
    protected String getKeyword(Element element){
        Keyword annotation = element.getAnnotation(Keyword.class);
        if(annotation != null) return annotation.value();
        return element.getSimpleName().toString();
    }

    /**
     * @return backing field name
     */
    protected String getKeyLiteral(ExecutableElement method, int beanLength){
        String name = method.getSimpleName().toString();
        return "\"" + Character.toLowerCase(name.charAt(beanLength)) + name.substring(beanLength + 1) + "\"";
    }


    protected void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    protected void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }
}
