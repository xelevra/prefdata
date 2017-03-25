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
        MethodSpec.Builder builder = MethodSpec.methodBuilder("edit")
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedTypename)
                .addStatement("editor = preferences.edit()");

        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }
}
