package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Prefixed;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class SetterGenerator extends MethodGenerator {
    public SetterGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        if (!check(field)) return;
        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateMethodName(field, "set"))
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedTypename);

        TypeName paramTypeName = TypeName.get(field.asType());

        boolean prefixed = field.getAnnotation(Prefixed.class) != null;
        if (prefixed) {
            builder.addParameter(ParameterSpec.builder(String.class, "prefix", Modifier.FINAL).build());
        }

        builder.addParameter(paramTypeName, "value");


        String keyLiteral = getKeyword(field);
        builder.beginControlFlow("if(editor == null)");

        addStatementSwitch(paramTypeName.toString(), builder, "preferences.edit()", keyLiteral, ".apply()", prefixed);
        builder.nextControlFlow("else");
        addStatementSwitch(paramTypeName.toString(), builder, "editor", keyLiteral, "", prefixed);
        builder.endControlFlow();


        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, String editorSource, String keyLiteral, String editorClose, boolean prefixed) {
        String invoke;
        switch (paramTypeString) {
            case "java.lang.Integer":
            case "int":
                invoke = "putInt";
                break;
            case "java.lang.Float":
            case "float":
                invoke = "putFloat";
                break;
            case "java.lang.Long":
            case "long":
                invoke = "putLong";
                break;
            case "java.lang.Boolean":
            case "boolean":
                invoke = "putBoolean";
                break;
            case "java.lang.String":
                invoke = "putString";
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + paramTypeString);
        }

        if (prefixed) {
            builder.addStatement("$L.$L($L + $S, $L)$L", editorSource, invoke, "prefix", keyLiteral, "value", editorClose);
        } else {
            builder.addStatement("$L.$L($S, $L)$L", editorSource, invoke, keyLiteral, "value", editorClose);
        }
    }
}
