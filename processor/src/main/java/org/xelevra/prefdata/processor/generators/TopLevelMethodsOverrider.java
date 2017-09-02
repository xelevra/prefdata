package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Use;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class TopLevelMethodsOverrider extends MethodGenerator {

    public TopLevelMethodsOverrider(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        throw new UnsupportedOperationException();
    }

    public void processMethod(ExecutableElement method, List<VariableElement> elements, boolean returnThis) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(method.getModifiers())
                .addAnnotation(Override.class)
                .returns(returnThis ? generatedTypename : TypeName.get(method.getReturnType()));

        ArrayList<Modifier> modifiers;
        for (VariableElement parameter : method.getParameters()){
            modifiers = new ArrayList<>(parameter.getModifiers());
            builder.addParameter(
                    TypeName.get(parameter.asType()),
                    parameter.getSimpleName().toString(),
                    modifiers.toArray(new Modifier[modifiers.size()])
            );
        }


        Use annotationUse = method.getAnnotation(Use.class);

        List<String> usingNames = Arrays.asList(annotationUse.value());
        List<VariableElement> using = new ArrayList<>();
        for (VariableElement field : elements) {
            if (usingNames.contains(field.getSimpleName().toString())) {
                using.add(field);
            }
        }

        if(annotationUse.asGetter()) addGetters(builder, using);

        boolean voidMethod = "void".equals(method.getReturnType().toString());

        boolean returned = false;

        if(voidMethod || returnThis){
            builder.addStatement("super.$L($L)", method.getSimpleName(), buildParamsString(method));
        } else if(!annotationUse.asSetter()){
            builder.addStatement("return super.$L($L)", method.getSimpleName(), buildParamsString(method));
            returned = true;
        } else {
            builder.addStatement("$T _result = super.$L($L)", method.getReturnType(), method.getSimpleName(), buildParamsString(method));
        }

        if(annotationUse.asSetter()) addSetters(builder, using);

        if (!voidMethod && !returned) {
            builder.addStatement("return $L", returnThis ? "this" : "_result");
        }

        classBuilder.addMethod(builder.build());
    }

    private String buildParamsString(ExecutableElement executableElement){
        StringBuilder stringBuilder = new StringBuilder();
        for (VariableElement parameter : executableElement.getParameters()){
            stringBuilder.append(parameter.getSimpleName()).append(",");
        }
        if(stringBuilder.length() > 0) stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private void addGetters(MethodSpec.Builder builder, List<VariableElement> using){
        for (VariableElement element : using){
            builder.addStatement("this.$L = $L()", element.getSimpleName(), generateMethodName(element, "get"));
        }
    }

    private void addSetters(MethodSpec.Builder builder, List<VariableElement> using){
        for (VariableElement element : using){
            builder.addStatement("$L(this.$L)", generateMethodName(element, "set"), element.getSimpleName());
        }
    }
}
