package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public abstract class MethodGenerator {
    protected final TypeName base;
    private final ProcessingEnvironment processingEnv;

    public MethodGenerator(TypeName base, ProcessingEnvironment processingEnv) {
        this.base = base;
        this.processingEnv = processingEnv;
    }

    public abstract void check(ExecutableElement method);

    public abstract MethodSpec create(ExecutableElement method);

    protected String getKeyLiteral(ExecutableElement method, boolean hasPrefix){
        return getKeyLiteral(method, hasPrefix, 3);
    }

    protected String getKeyLiteral(ExecutableElement method, boolean hasPrefix, int beanLength){
        String name = method.getSimpleName().toString();
        return (hasPrefix ? "prefix + " : "")
                + "\"" + Character.toLowerCase(name.charAt(beanLength)) + name.substring(beanLength + 1) + "\"";
    }

    protected void checkIsPrefix(VariableElement parameter){
        if(!TypeName.get(String.class).equals(TypeName.get(parameter.asType()))){
            error(parameter, "Prefix must be a String");
        }
    }

    protected void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    protected void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }
}
