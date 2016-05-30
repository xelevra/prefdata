package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class CommitApplyGenerator extends MethodGenerator{
    public CommitApplyGenerator(TypeName base, ProcessingEnvironment processingEnv) {
        super(base, processingEnv);
    }

    @Override
    public void check(ExecutableElement method) {
        if(!method.getParameters().isEmpty()){
            error(method, "edit method must have no parameters");
        }

        if(!TypeName.get(method.getReturnType()).equals(TypeName.VOID)){
            error(method, "Invalid returning type. Must be void");
        }
    }

    @Override
    public MethodSpec create(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("editor.$L()", methodName)
                .addStatement("editor = null")
                .build();
    }
}
