package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.xelevra.prefdata.annotations.Prefix;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class SetterGenerator extends MethodGenerator {
    private final boolean hasEditor;

    public SetterGenerator(TypeName base, ProcessingEnvironment processingEnv, boolean hasEditor) {
        super(base, processingEnv);
        this.hasEditor = hasEditor;
    }

    @Override
    public void check(ExecutableElement method) {
        if (method.getSimpleName().toString().length() == 3) {    // just "set()"
            error(method, "Wrong setter bean");
        }

        switch (method.getParameters().size()) {
            case 1:
                if (method.getParameters().get(0).getAnnotation(Prefix.class) != null) {
                    error(method, "No value to set");
                }
                break;
            case 2:
                VariableElement parameter = null;
                VariableElement parameterPrefix = null;
                for (int i = 0; i < 2; i++) {
                    VariableElement p = method.getParameters().get(i);
                    if (p.getAnnotation(Prefix.class) != null) {
                        parameterPrefix = p;
                    } else {
                        parameter = p;
                    }
                }

                if (parameter == null) {
                    error(method, "Two prefixes are not allowed");
                } else if (parameterPrefix == null) {
                    error(method, "Two values are not allowed");
                }

                checkIsPrefix(parameterPrefix);
                break;
            default:
                error(method, "Wrong params number");
        }

        TypeName returning = TypeName.get(method.getReturnType());
        if (!returning.equals(TypeName.VOID)
                && !returning.equals(base)
                ) {
            error(method, "Invalid returning type. Must be void or " + base.toString());
        }
    }

    @Override
    public MethodSpec create(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returning);

        if (hasEditor) {
            builder.beginControlFlow("if(editor == null)");
        }

        TypeName paramTypeName = null;
        VariableElement prefix = null;
        int prefixIndex;
        for (prefixIndex = 0; prefixIndex < method.getParameters().size(); prefixIndex++) {
            VariableElement p = method.getParameters().get(prefixIndex);
            if (p.getAnnotation(Prefix.class) != null) {
                prefix = p;
                builder.addParameter(TypeName.get(p.asType()), "prefix");
            } else {
                paramTypeName = TypeName.get(p.asType());
                builder.addParameter(paramTypeName, "value");
            }
        }


        String keyLiteral = getKeyLiteral(method, prefix != null);
        addStatementSwitch(paramTypeName.toString(), builder, "preferences.edit()", keyLiteral, ".apply()");
        if (hasEditor) {
            builder.nextControlFlow("else");
            addStatementSwitch(paramTypeName.toString(), builder, "editor", keyLiteral, "");
            builder.endControlFlow();
        }

        if (!returning.equals(TypeName.VOID)) {
            builder.addStatement("return this");
        }

        return builder.build();
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

        builder.addStatement("$L.$L($L, $L)$L", editorSource, invoke, keyLiteral, "value", editorClose);
    }

}
