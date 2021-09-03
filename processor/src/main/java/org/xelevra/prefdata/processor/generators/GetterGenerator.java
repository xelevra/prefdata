package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Encapsulate;
import org.xelevra.prefdata.annotations.Prefixed;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class GetterGenerator extends MethodGenerator{
    public GetterGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    /**
     * Prefixes not supported for abstract methods
     */
    @Override
    public boolean checkMethod(ExecutableElement method) {
        if(!super.checkMethod(method)) return false;

        String methodName = method.getSimpleName().toString();

        // just "is()" or "get()"
        if("is".equals(methodName)){
            error(method, "Is what?");
            return false;
        } else if("get".equals(methodName)){
            error(method, "Get what?");
            return false;
        }

        if(TypeName.get(method.getReturnType()).equals(TypeName.VOID)){
            error(method, "Getter can't be void");
            return false;
        }

        if(methodName.startsWith("is")
                && !(TypeName.get(method.getReturnType()).equals(TypeName.BOOLEAN)
                || TypeName.get(method.getReturnType()).equals(TypeName.get(Boolean.class)))
        ){
            error(method, "This method must return boolean");
            return false;
        }

        switch (method.getParameters().size()){
            case 0:
                break;
            case 1:
                if(!checkIsReturningSameType(method, method.getParameters().get(0))) return false;
                else break;
            default:
                error(method, "Wrong params number");
                return false;
        }

        return true;
    }

    private boolean checkIsReturningSameType(ExecutableElement method, VariableElement parameter){
        if (!method.getReturnType().equals(parameter.asType())) {
            error(parameter, "Type of default value must be same as the method returning type");
            return false;
        }
        return true;
    }

    @Override
    public void processField(VariableElement field) {
        if(!checkField(field)) return;

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

        String defaultVal = field.getSimpleName().toString(); // for java abstract classes
        addStatementSwitch(field.asType().toString(), builder, getKeyword(field), defaultVal, prefixed);

        classBuilder.addMethod(builder.build());
    }

    /**
     * Prefixes not supported for abstract methods
     * Default values not supported
     */
    @Override
    public void processMethod(ExecutableElement method) {
        TypeName typeName = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(typeName);

        addStatementSwitch(method.getReturnType().toString(), builder, getKeyword(method), null, false);

        classBuilder.addMethod(builder.build());
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, String keyWord, String defaultVal, boolean prefixed) {
        String invoke;
        String calculatedDefaultValue;
        switch (paramTypeString) {
            case "int":
                invoke = "getInt";
                calculatedDefaultValue = "0";
                break;
            case "float":
                invoke = "getFloat";
                calculatedDefaultValue = "0f";
                break;
            case "long":
                invoke = "getLong";
                calculatedDefaultValue = "0";
                break;
            case "boolean":
                invoke = "getBoolean";
                calculatedDefaultValue = "false";
                break;
            case "java.lang.String":
                invoke = "getString";
                calculatedDefaultValue = "null";
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + paramTypeString);
        }

        if (defaultVal == null) defaultVal = calculatedDefaultValue;

        if(prefixed){
            builder.addStatement("return preferences.$L($L + $S, $L)", invoke, "prefix", keyWord, defaultVal);
        } else {
            builder.addStatement("return preferences.$L($S, $L)", invoke, keyWord, defaultVal);
        }
    }
}
