package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class ExportFieldsGenerator extends MethodGenerator {
    public ExportFieldsGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        throw new UnsupportedOperationException();
    }

    public void generateMembers(List<VariableElement> fields){
        generateGetKeysMethod(fields);
        generateGetTypeMethod(fields);
        generateGetValueMethod(fields);
    }

    private void generateGetKeysMethod(List<VariableElement> fields){
        ParameterizedTypeName listType = ParameterizedTypeName.get(List.class, String.class);
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getKeys")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(listType);

        CodeBlock.Builder fieldsArrayBuilder = CodeBlock.builder();

        fieldsArrayBuilder.add("return $T.asList(", Arrays.class);
        for (int i = 0; i < fields.size(); i++) {
            fieldsArrayBuilder.add("$S", fields.get(i));
            if(i < fields.size() - 1) fieldsArrayBuilder.add(",");
        }
        fieldsArrayBuilder.add(");\n");


        builder.addCode(fieldsArrayBuilder.build());

        classBuilder.addMethod(builder.build());
    }

    private void generateGetTypeMethod(List<VariableElement> fields){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getFieldType")
                .addParameter(String.class, "key")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Type.class);
        builder.beginControlFlow("switch($L)", "key");
        for (int i = 0; i < fields.size(); i++) {
            builder.addStatement("case $S: return $L.class", fields.get(i).getSimpleName(), fields.get(i).asType());
        }
        builder.addStatement("default: throw new $T($S + $L)", IllegalArgumentException.class, "Invalid key ", "key");
        builder.endControlFlow();

        classBuilder.addMethod(builder.build());
    }

    private void generateGetValueMethod(List<VariableElement> fields){
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getValue")
                .addParameter(String.class, "key")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Object.class);
        builder.beginControlFlow("switch($L)", "key");
        for (int i = 0; i < fields.size(); i++) {
            builder.addStatement("case $S: return $L()", fields.get(i).getSimpleName(), generateMethodName(fields.get(i), "get"));
        }
        builder.addStatement("default: throw new $T($S + $L)", IllegalArgumentException.class, "Invalid key ", "key");
        builder.endControlFlow();

        classBuilder.addMethod(builder.build());
    }
}
