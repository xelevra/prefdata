package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class CommitApplyGenerator extends MethodGenerator{

    public CommitApplyGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {

    }

    public void check(ExecutableElement method) {
        if(!method.getParameters().isEmpty()){
            error(method, "edit method must have no parameters");
        }

        if(!TypeName.get(method.getReturnType()).equals(TypeName.VOID)){
            error(method, "Invalid returning type. Must be void");
        }
    }

    public MethodSpec create(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("editor.$L()", methodName)
                .addStatement("editor = null")
                .build();
    }
}
