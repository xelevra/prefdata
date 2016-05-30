package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

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
        if(method.getSimpleName().toString().length() == 3){    // just "set()"
            error(method, "Wrong setter bean");
        }

        switch (method.getParameters().size()){
            case 1:
                TypeName returning = TypeName.get(method.getReturnType());
                if(!returning.equals(TypeName.VOID)
                        && !returning.equals(base)
                        ){
                    error(method, "Invalid returning type. Must be void or " + base.toString());
                }
                break;
            default:
                error(method, "Wrong params number");
        }
    }

    @Override
    public MethodSpec create(ExecutableElement method) {
        VariableElement param = method.getParameters().get(0);
        TypeName paramTypeName = TypeName.get(param.asType());
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(paramTypeName, "value", Modifier.FINAL)
                .returns(returning);

        if(hasEditor) {
            builder.beginControlFlow("if(editor == null)");
        }
        addStatementSwitch(paramTypeName.toString(), builder, "preferences.edit()", getKey(method), ".apply()");
        if(hasEditor) {
            builder.nextControlFlow("else");
            addStatementSwitch(paramTypeName.toString(), builder, "editor", getKey(method), "");
            builder.endControlFlow();
        }

        if(!returning.equals(TypeName.VOID)){
            builder.addStatement("return this");
        }

        return builder.build();
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, String editorSource, String key, String editorClose){
        String invoke;
        switch (paramTypeString){
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

        builder.addStatement("$L.$L($S, $L)$L", editorSource, invoke, key, "value", editorClose);
    }

}
