package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class EditGenerator extends MethodGenerator{
    public EditGenerator(TypeName base, ProcessingEnvironment processingEnv) {
        super(base, processingEnv);
    }

    @Override
    public void check(ExecutableElement method) {
        if(!method.getParameters().isEmpty()){
            error(method, "edit method must have no parameters");
        }

        TypeName returning = TypeName.get(method.getReturnType());
        if(!returning.equals(TypeName.VOID)
                && !returning.equals(base)
                ){
            error(method, "Invalid returning type. Must be void or " + base.toString());
        }
    }

    @Override
    public MethodSpec create(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder("edit")
                .addModifiers(Modifier.PUBLIC)
                .returns(returning)
                .addStatement("editor = preferences.edit()");

        if(!returning.equals(TypeName.VOID)){
            builder.addStatement("return this");
        }

        return builder.build();
    }
}
