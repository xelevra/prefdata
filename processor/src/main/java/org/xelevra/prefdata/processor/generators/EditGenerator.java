package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class EditGenerator extends MethodGenerator{
    public EditGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {

    }

    public void check(ExecutableElement method) {
        if(!method.getParameters().isEmpty()){
            error(method, "edit method must have no parameters");
        }

        TypeName returning = TypeName.get(method.getReturnType());
        if(!returning.equals(TypeName.VOID)
                && !returning.equals(generatedTypename)
                ){
            error(method, "Invalid returning type. Must be void or " + generatedTypename.toString());
        }
    }

    public MethodSpec create(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("edit")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returning)
                .addStatement("editor = preferences.edit()");

        if(!returning.equals(TypeName.VOID)){
            builder.addStatement("return this");
        }

        return builder.build();
    }
}
