package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Encapsulate;
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

        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateMethodName(field, "get")).returns(typeName);

        Encapsulate encapsulate = field.getAnnotation(Encapsulate.class);
        builder.addModifiers(
                encapsulate != null && encapsulate.getter() ? Modifier.PRIVATE : Modifier.PUBLIC
        );

        boolean prefixed = field.getAnnotation(Prefixed.class) != null;
        if(prefixed){
            builder.addParameter(ParameterSpec.builder(String.class, "prefix", Modifier.FINAL).build());
        }

        addStatementSwitch(field.asType().toString(), builder, field, prefixed);

        classBuilder.addMethod(builder.build());
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, VariableElement field, boolean prefixed) {
        String invoke;
        switch (paramTypeString) {
            case "int":
                invoke = "getInt";
                break;
            case "float":
                invoke = "getFloat";
                break;
            case "long":
                invoke = "getLong";
                break;
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
            builder.addStatement("return preferences.$L($L + $S, $L)", invoke, "prefix", getKeyword(field), field.getSimpleName());
        } else {
            builder.addStatement("return preferences.$L($S, $L)", invoke, getKeyword(field), field.getSimpleName());
        }
    }
}
