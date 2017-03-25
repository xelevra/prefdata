package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Prefixed;

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

        boolean prefixed = field.getAnnotation(Prefixed.class) != null;
        if(prefixed){
            builder.addParameter(ParameterSpec.builder(String.class, "prefix", Modifier.FINAL).build());
        }

        builder.beginControlFlow("if(editor == null)");
        if(prefixed){
            builder.addStatement("preferences.edit().remove($L + $S).apply()", "prefix", field.getSimpleName());
        } else {
            builder.addStatement("preferences.edit().remove($S).apply()", field.getSimpleName());
        }

        builder.nextControlFlow("else");
        if(prefixed) {
            builder.addStatement("editor.remove($L + $S)", "prefix", field.getSimpleName());
        } else {
            builder.addStatement("editor.remove($S)", field.getSimpleName());
        }
        builder.endControlFlow();

        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }
}
