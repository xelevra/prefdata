package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class EditGenerator extends MethodGenerator {
    public EditGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public boolean checkMethod(ExecutableElement method) {
        if(!super.checkMethod(method)) return false;

        if (!method.getParameters().isEmpty()) {
            error(method, "edit method must have no parameters");
            return false;
        }

        TypeName returning = TypeName.get(method.getReturnType());
        if (!returning.equals(TypeName.VOID) && !returning.equals(baseTypename)) {
            error(method, "Invalid returning type. Must be void or " + baseTypename.toString());
            return false;
        }

        return true;
    }

    @Override
    public void processField(VariableElement field) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("edit")
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedTypename)
                .addStatement("editor = preferences.edit()");

        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }

    @Override
    public void processMethod(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("edit")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returning)
                .addStatement("editor = preferences.edit()");

        if (!returning.equals(TypeName.VOID)) {
            builder.addStatement("return this");
        }

        classBuilder.addMethod(builder.build());
    }
}
