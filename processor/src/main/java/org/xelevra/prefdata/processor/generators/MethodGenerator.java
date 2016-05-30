package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
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

    protected String getKey(ExecutableElement method){
        return method.getSimpleName().toString().substring(3).toLowerCase();
    }

    protected void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    protected void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }
}
