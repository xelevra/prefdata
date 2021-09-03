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

public class RemoveGenerator extends MethodGenerator {

    public RemoveGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    /**
     * Prefixes not supported for abstract methods
     */
    @Override
    public boolean checkMethod(ExecutableElement method) {
        if (!super.checkMethod(method)) return false;

        if (method.getSimpleName().toString().equals("remove")) {    // just "remove()"
            error(method, "Remove what?");
            return false;
        }

        if (method.getParameters().size() != 0) {
            error(method, "Wrong params number");
            return false;
        }

        TypeName returning = TypeName.get(method.getReturnType());
        if (!returning.equals(TypeName.VOID) && !returning.equals(baseTypename)) {
            error(method, "Invalid returning type. Must be void or " + baseTypename.toString());
            return false;
        }

        return true;
    }


    /**
     * Prefixes not supported for abstract methods
     */
    @Override
    public void processMethod(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returning);

        builder.beginControlFlow("if(editor == null)");

        String keyLiteral = getKeyLiteral(method, "remove".length());

        builder.addStatement("preferences.edit().remove($L).apply()", keyLiteral);
        builder.nextControlFlow("else");
        builder.addStatement("editor.remove($L)", keyLiteral);
        builder.endControlFlow();


        if (!returning.equals(TypeName.VOID)) {
            builder.addStatement("return this");
        }

        classBuilder.addMethod(builder.build());
    }

    @Override
    public void processField(VariableElement field) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateMethodName(field, "remove")).returns(generatedTypename);

        Encapsulate encapsulate = field.getAnnotation(Encapsulate.class);
        if (encapsulate != null && encapsulate.remove()) return; // not usable
        else builder.addModifiers(Modifier.PUBLIC);

        boolean prefixed = field.getAnnotation(Prefixed.class) != null;
        if (prefixed) {
            builder.addParameter(ParameterSpec.builder(String.class, "prefix", Modifier.FINAL).build());
        }

        builder.beginControlFlow("if(editor == null)");
        if (prefixed) {
            builder.addStatement("preferences.edit().remove($L + $S).apply()", "prefix", getKeyword(field));
        } else {
            builder.addStatement("preferences.edit().remove($S).apply()", getKeyword(field));
        }

        builder.nextControlFlow("else");
        if (prefixed) {
            builder.addStatement("editor.remove($L + $S)", "prefix", getKeyword(field));
        } else {
            builder.addStatement("editor.remove($S)", getKeyword(field));
        }
        builder.endControlFlow();

        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }
}
