package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

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

        switch (typeName.toString()){
            case "java.lang.Integer":
            case "int":
                builder.addStatement("return preferences.getInt($S, $L)", fieldName, fieldName);
                break;
            case "java.lang.Float":
            case "float":
                builder.addStatement("return preferences.getFloat($S, $L)", fieldName, fieldName);
                break;
            case "java.lang.Long":
            case "long":
                builder.addStatement("return preferences.getLong($S, $L)", fieldName, fieldName);
                break;
            case "java.lang.Boolean":
            case "boolean":
                builder.addStatement("return preferences.getBoolean($S, $L)", fieldName, fieldName);
                break;
            case "java.lang.String":
                builder.addStatement("return preferences.getString($S, $L)", fieldName, fieldName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type " + typeName);
        }

        classBuilder.addMethod(builder.build());
    }
}
