package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class RemoveGenerator extends MethodGenerator {

    public RemoveGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateName(field, "remove"))
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedTypename);

        builder.beginControlFlow("if(editor == null)");
        builder.addStatement("preferences.edit().remove($S).apply()", field.getSimpleName());

        builder.nextControlFlow("else");
        builder.addStatement("editor.remove($S)", field.getSimpleName());
        builder.endControlFlow();

        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }
}
