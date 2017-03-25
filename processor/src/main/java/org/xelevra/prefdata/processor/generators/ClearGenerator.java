package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class ClearGenerator extends MethodGenerator {

    public ClearGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC);

        builder.addStatement("preferences.edit().clear().apply()");

        classBuilder.addMethod(builder.build());
    }
}
