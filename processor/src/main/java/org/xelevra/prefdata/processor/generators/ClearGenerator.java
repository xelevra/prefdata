package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class ClearGenerator extends MethodGenerator {

    public ClearGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public boolean checkMethod(ExecutableElement method) {
        if (!super.checkMethod(method)) return false;

        if (!method.getParameters().isEmpty()) {
            error(method, "edit method must have no parameters");
            return false;
        }

        if (!TypeName.get(method.getReturnType()).equals(TypeName.VOID)) {
            error(method, "Invalid returning type. Must be void");
            return false;
        }

        return true;
    }

    @Override
    public void processField(VariableElement field) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC);

        builder.addStatement("preferences.edit().clear().apply()");

        classBuilder.addMethod(builder.build());
    }

    @Override
    public void processMethod(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("editor.$L()", methodName)
                .addStatement("editor = null");

        classBuilder.addMethod(builder.build());
    }
}
