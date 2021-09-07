package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;
import org.xelevra.prefdata.processor.KeywordDetector;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public abstract class MethodGenerator {
    private static String[] SUPPORTED_OPERATORS = {"get", "is", "set", "remove"};

    protected final TypeName generatedTypename;
    protected final TypeName baseTypename;
    protected final TypeSpec.Builder classBuilder;
    private final ProcessingEnvironment processingEnv;
    private KeywordDetector keywordDetector;

    public MethodGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        this.processingEnv = processingEnv;
        classBuilder = builder;
        TypeSpec tmpTypeSpec = builder.build();
        this.generatedTypename = ClassName.bestGuess(tmpTypeSpec.name);
        baseTypename = tmpTypeSpec.superclass != null ? tmpTypeSpec.superclass : tmpTypeSpec.superinterfaces.get(0);
    }

    public abstract void processField(VariableElement field);

    public abstract void processMethod(ExecutableElement method);

    public final <T extends MethodGenerator> T setKeywordDetector(KeywordDetector keywordDetector) {
        this.keywordDetector = keywordDetector;
        return (T) this;
    }

    protected boolean checkField(VariableElement field) {
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

    protected final String generateMethodName(VariableElement field, String method){
        if("get".equals(method) && (field.asType().toString().equals("boolean"))){
            method = "is";
        }

        String fieldName = field.getSimpleName().toString();
        return method + (Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1, fieldName.length()));

    }

    /**
     * @return String key for preferences
     */
    @NotNull
    protected final String getKeyword(Element element) {
        String keyLiteral = getKeyLiteral(element);
        String keyWord = keywordDetector.getKeyword(keyLiteral);
        return keyWord == null ? keyLiteral : keyWord;
    }

    /**
     * @return backing field name
     */
    protected final String getKeyLiteral(Element element){
        String name = element.getSimpleName().toString();
        if(element instanceof VariableElement) return name;

        for (String bean : SUPPORTED_OPERATORS) {
            if(!name.startsWith(bean)) continue;
            return Character.toLowerCase(name.charAt(bean.length())) + name.substring(bean.length() + 1);
        }

        error(element, "Undefined method prefix");
        throw new IllegalArgumentException("Undefined method prefix");
    }


    protected void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    protected void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }
}
