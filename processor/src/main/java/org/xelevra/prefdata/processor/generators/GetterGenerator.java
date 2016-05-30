package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

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
        if(method.getSimpleName().toString().length() == 3){    // just "get()"
            error(method, "Wrong getter bean");
        }

        if(TypeName.get(method.getReturnType()).equals(TypeName.VOID)){
            error(method, "Getter can't be void");
        }

        switch (method.getParameters().size()){
            case 0:
                break;
            case 1:
                if(!method.getReturnType().equals(method.getParameters().get(0).asType())){
                    error(method, "Type of default value must be same as the method returning type");
                }
                break;
            default:
                error(method, "Wrong params number");
        }
    }

    @Override
    public MethodSpec create(ExecutableElement method) {
        TypeName typeName = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName);
        String def;
        if(method.getParameters().isEmpty()){
            def = null;
        } else {
            VariableElement parameter = method.getParameters().get(0);
            def = "defaultValue";
            builder.addParameter(TypeName.get(parameter.asType()), "defaultValue");
        }
        String key = getKey(method);
        switch (typeName.toString()){
            case "java.lang.Integer":
            case "int":
                builder.addStatement("return preferences.getInt($S, $L)", key, def == null ? "0" : def);
                break;
            case "java.lang.Float":
            case "float":
                builder.addStatement("return preferences.getFloat($S, $L)", key, def == null ? "0f" : def);
                break;
            case "java.lang.Long":
            case "long":
                builder.addStatement("return preferences.getLong($S, $L)", key, def == null ? "0l" : def);
                break;
            case "java.lang.Boolean":
            case "boolean":
                builder.addStatement("return preferences.getBoolean($S, $L)", key, def == null ? "false" : def);
                break;
            case "java.lang.String":
                builder.addStatement("return preferences.getString($S, $L)", key, def == null ? "null" : def);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + typeName);
        }

        return builder.build();
    }
}
