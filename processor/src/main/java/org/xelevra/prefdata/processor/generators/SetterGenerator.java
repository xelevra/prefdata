package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class SetterGenerator extends MethodGenerator {
    public SetterGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        if(!check(field)) return;
        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateName(field, "set"))
                .addModifiers(Modifier.PUBLIC)
                .returns(generatedTypename);

        TypeName paramTypeName = TypeName.get(field.asType());

        builder.beginControlFlow("if(editor == null)");
        builder.addParameter(paramTypeName, "value");

        String keyLiteral = field.getSimpleName().toString();
        addStatementSwitch(paramTypeName.toString(), builder, "preferences.edit()", keyLiteral, ".apply()");
        builder.nextControlFlow("else");
        addStatementSwitch(paramTypeName.toString(), builder, "editor", keyLiteral, "");
        builder.endControlFlow();


        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, String editorSource, String keyLiteral, String editorClose) {
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

        builder.addStatement("$L.$L($S, $L)$L", editorSource, invoke, keyLiteral, "value", editorClose);
    }
}
