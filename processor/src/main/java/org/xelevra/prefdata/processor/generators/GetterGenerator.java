package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import org.xelevra.prefdata.annotations.Prefix;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class GetterGenerator extends MethodGenerator{
    public GetterGenerator(TypeName base, ProcessingEnvironment processingEnv) {
        super(base, processingEnv);
    }

    @Override
    public void check(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();

        // just "is()" or "get()"
        if("is".equals(methodName)){
            error(method, "Is what?");
        } else if("get".equals(methodName)){
            error(method, "Get what?");
        }

        if(TypeName.get(method.getReturnType()).equals(TypeName.VOID)){
            error(method, "Getter can't be void");
        }

        if(methodName.startsWith("is")
                && !(TypeName.get(method.getReturnType()).equals(TypeName.BOOLEAN)
                    || TypeName.get(method.getReturnType()).equals(TypeName.get(Boolean.class)))
        ){
            error(method, "This method must return boolean");
        }

        switch (method.getParameters().size()){
            case 0:
                break;
            case 1:
                VariableElement parameter = method.getParameters().get(0);
                if (parameter.getAnnotation(Prefix.class) == null) {
                    checkIsReturningSameType(method, parameter);
                } else {
                    checkIsPrefix(parameter);
                }
                break;
            case 2:
                parameter = null;
                VariableElement parameterPrefix = null;
                for (int i = 0; i < 2; i++){
                    VariableElement p = method.getParameters().get(i);
                    if(p.getAnnotation(Prefix.class) != null){
                        parameterPrefix = p;
                    } else {
                        parameter = p;
                    }
                }

                if(parameter == null){
                    error(method, "Two prefixes are not allowed");
                } else if(parameterPrefix == null){
                    error(method, "Two default values are not allowed");
                }

                checkIsPrefix(parameterPrefix);
                checkIsReturningSameType(method, parameter);
                break;
            default:
                error(method, "Wrong params number");
        }
    }

    private void checkIsReturningSameType(ExecutableElement method, VariableElement parameter){
        if (!method.getReturnType().equals(parameter.asType())) {
            error(parameter, "Type of default value must be same as the method returning type");
        }
    }

    @Override
    public MethodSpec create(ExecutableElement method) {
        boolean startWithIs = method.getSimpleName().toString().startsWith("is");
        TypeName typeName = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(typeName);

        String def = null;
        VariableElement prefix = null;
        if (!method.getParameters().isEmpty()) {
            int prefixIndex;
            for (prefixIndex = 0; prefixIndex < method.getParameters().size(); prefixIndex++){
                VariableElement p = method.getParameters().get(prefixIndex);
                if(p.getAnnotation(Prefix.class) != null){
                    prefix = p;
                    builder.addParameter(TypeName.get(p.asType()), "prefix");
                } else {
                    def = "defaultValue";
                    builder.addParameter(TypeName.get(p.asType()), "defaultValue");
                }
            }
        }

        String keyLiteral = getKeyLiteral(method, prefix != null, startWithIs ? 2 : 3);

        switch (typeName.toString()){
            case "java.lang.Integer":
            case "int":
                builder.addStatement("return preferences.getInt($L, $L)", keyLiteral, def == null ? "0" : def);
                break;
            case "java.lang.Float":
            case "float":
                builder.addStatement("return preferences.getFloat($L, $L)", keyLiteral, def == null ? "0f" : def);
                break;
            case "java.lang.Long":
            case "long":
                builder.addStatement("return preferences.getLong($L, $L)", keyLiteral, def == null ? "0l" : def);
                break;
            case "java.lang.Boolean":
            case "boolean":
                builder.addStatement("return preferences.getBoolean($L, $L)", keyLiteral, def == null ? "false" : def);
                break;
            case "java.lang.String":
                builder.addStatement("return preferences.getString($L, $L)", keyLiteral, def == null ? "null" : def);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + typeName);
        }

        return builder.build();
    }
}
