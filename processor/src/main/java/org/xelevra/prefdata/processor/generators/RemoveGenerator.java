package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class RemoveGenerator extends MethodGenerator {

    public RemoveGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {

    }

    public void check(ExecutableElement method) {
        if (method.getSimpleName().toString().equals("remove")) {    // just "remove()"
            error(method, "Remove what?");
        }

        switch (method.getParameters().size()){
            case 1:
//                checkIsPrefix(method.getParameters().get(0));
            case 0:
                break;
            default:
                error(method, "Wrong params number");
        }

        TypeName returning = TypeName.get(method.getReturnType());
        if (!returning.equals(TypeName.VOID)
                && !returning.equals(generatedTypename)
                ) {
            error(method, "Invalid returning type. Must be void or " + generatedTypename.toString());
        }

//        super.check(method);
    }

    public MethodSpec create(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returning);

//        if (hasEditor) {
//            builder.beginControlFlow("if(editor == null)");
//        }

        VariableElement prefix = null;
        if (!method.getParameters().isEmpty()) {
            VariableElement p = method.getParameters().get(0);
            prefix = p;
            builder.addParameter(TypeName.get(p.asType()), "prefix");
        }

//        String keyLiteral = getKeyLiteral(method, prefix != null, "remove".length());
//        builder.addStatement("preferences.edit().remove($L).apply()", keyLiteral);

//        if (hasEditor) {
//            builder.nextControlFlow("else");
//            builder.addStatement("editor.remove($L)", keyLiteral);
//            builder.endControlFlow();
//        }

        if (!returning.equals(TypeName.VOID)) {
            builder.addStatement("return this");
        }

        return builder.build();
    }
}
