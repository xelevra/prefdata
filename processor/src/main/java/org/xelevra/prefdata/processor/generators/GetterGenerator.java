package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Prefixed;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class GetterGenerator extends MethodGenerator{
    public GetterGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        if(!check(field)) return;

        TypeName typeName = TypeName.get(field.asType());
        String fieldName = field.getSimpleName().toString();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateName(field, "get"))
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName);

        boolean prefixed = field.getAnnotation(Prefixed.class) != null;
        if(prefixed){
            builder.addParameter(ParameterSpec.builder(String.class, "prefix", Modifier.FINAL).build());
        }

        addStatementSwitch(field.asType().toString(), builder, fieldName, prefixed);

        classBuilder.addMethod(builder.build());
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, String keyLiteral, boolean prefixed) {
        String invoke;
        switch (paramTypeString) {
            case "java.lang.Integer":
            case "int":
                invoke = "getInt";
                break;
            case "java.lang.Float":
            case "float":
                invoke = "getFloat";
                break;
            case "java.lang.Long":
            case "long":
                invoke = "getLong";
                break;
            case "java.lang.Boolean":
            case "boolean":
                invoke = "getBoolean";
                break;
            case "java.lang.String":
                invoke = "getString";
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + paramTypeString);
        }

        if(prefixed){
            builder.addStatement("return preferences.$L($L + $S, $L)", invoke, "prefix", keyLiteral, keyLiteral);
        } else {
            builder.addStatement("return preferences.$L($S, $L)", invoke, keyLiteral, keyLiteral);
        }
    }
}
