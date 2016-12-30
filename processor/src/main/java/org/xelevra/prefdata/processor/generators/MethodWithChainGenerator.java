package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;

public abstract class MethodWithChainGenerator extends MethodGenerator {
    protected final boolean hasEditor;

    public MethodWithChainGenerator(TypeName base, ProcessingEnvironment processingEnv, boolean hasEditor) {
        super(base, processingEnv);
        this.hasEditor = hasEditor;
    }

    @Override
    public void check(ExecutableElement method) {
        checkReturnVoidOrBase(method);
    }

    private void checkReturnVoidOrBase(ExecutableElement method){
        TypeName returning = TypeName.get(method.getReturnType());
        if (!returning.equals(TypeName.VOID)
                && !returning.equals(base)
                ) {
            error(method, "Invalid returning type. Must be void or " + base.toString());
        }
    }
}
