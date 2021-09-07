package org.xelevra.prefdata.processor.generators;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Belongs;
import org.xelevra.prefdata.annotations.Encapsulate;
import org.xelevra.prefdata.annotations.Prefixed;

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class SetterGenerator extends MethodGenerator {
    public SetterGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    /**
     * Prefixes not supported for abstract methods
     */
    @Override
    public boolean checkMethod(ExecutableElement method) {
        if (!super.checkMethod(method)) return false;

        if (method.getSimpleName().toString().equals("set")) {    // just "set()"
            error(method, "Set what?");
            return false;
        }

        if (method.getParameters().size() != 1) {
            error(method, "Wrong params number");
            return false;
        }

        return true;
    }


    @Override
    public void processField(VariableElement field) {
        if (!checkField(field)) return;

        MethodSpec.Builder builder = MethodSpec.methodBuilder(generateMethodName(field, "set")).returns(generatedTypename);


        Encapsulate encapsulate = field.getAnnotation(Encapsulate.class);
        builder.addModifiers(
                encapsulate != null && encapsulate.setter() ? Modifier.PRIVATE : Modifier.PUBLIC
        );

        TypeName paramTypeName = TypeName.get(field.asType());

        boolean prefixed = field.getAnnotation(Prefixed.class) != null;
        if (prefixed) {
            builder.addParameter(ParameterSpec.builder(String.class, "prefix", Modifier.FINAL).build());
        }

        builder.addParameter(paramTypeName, "value");

        addValidationBlockIfNeeded(builder, field);

        String keyword = getKeyword(field);

        builder.beginControlFlow("if(editor == null)");
        addStatementSwitch(field, paramTypeName.toString(), builder, "preferences.edit()", keyword, ".apply()", prefixed);
        builder.nextControlFlow("else");
        addStatementSwitch(field, paramTypeName.toString(), builder, "editor", keyword, "", prefixed);
        builder.endControlFlow();


        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }

    /**
     * Prefixes not supported for abstract methods
     * Belongs not supported for abstract methods
     */
    @Override
    public void processMethod(ExecutableElement method) {
        TypeName returning = TypeName.get(method.getReturnType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(returning);


        TypeName paramTypeName = TypeName.get(method.getParameters().get(0).asType());
        builder.addParameter(paramTypeName, "value");

        String keyword = getKeyword(method);

        builder.beginControlFlow("if(editor == null)");
        addStatementSwitch(method, paramTypeName.toString(), builder, "preferences.edit()", keyword, ".apply()", false);
        builder.nextControlFlow("else");
        addStatementSwitch(method, paramTypeName.toString(), builder, "editor", keyword, "", false);
        builder.endControlFlow();

        if (!returning.equals(TypeName.VOID)) {
            builder.addStatement("return this");
        }

        classBuilder.addMethod(builder.build());
    }


    private void addValidationBlockIfNeeded(MethodSpec.Builder builder, VariableElement field) {
        Belongs annotation = field.getAnnotation(Belongs.class);
        if (annotation == null || !annotation.validation()) return;

        Class boxed = mapFromPrimitive(field, field.asType());

        CodeBlock.Builder initializer = CodeBlock.builder();
        initializer.add("$T.asList(", Arrays.class);

        String formatter;
        if (boxed == String.class) formatter = "$S";
        else if (boxed == Float.class) formatter = "$LF";
        else if (boxed == Long.class) formatter = "$LL";
        else formatter = "$L";

        for (int i = 0; i < annotation.value().length; i++) {
            initializer.add(formatter, annotation.value()[i]);
            if (i < annotation.value().length - 1) initializer.add(", ");
        }

        initializer.add(")");

        classBuilder.addField(
                FieldSpec.builder(
                        ParameterizedTypeName.get(List.class, boxed),
                        field.getSimpleName() + "ValidationSet",
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL
                ).initializer(initializer.build())
                        .build()
        );


        builder.beginControlFlow("if(!$L$L.contains($L))", field.getSimpleName(), "ValidationSet", "value");
        builder.addStatement(
                "throw new $T( $S + $L + $S + $S + $L$L)",
                IllegalArgumentException.class,
                "'", "value", "'",
                " found, but expected ",
                field.getSimpleName(), "ValidationSet"
        );
        builder.endControlFlow();
    }


    private Class mapFromPrimitive(Element element, TypeMirror typeMirror) {
        switch (typeMirror.toString()) {
            case "int":
                return Integer.class;
            case "float":
                return Float.class;
            case "long":
                return Long.class;
            case "boolean":
                return Boolean.class;
            case "java.lang.String":
                return String.class;
            default:
                String message = "Incorrect type: " + typeMirror;
                error(element, message);
                throw new IllegalArgumentException(message);
        }
    }

    private void addStatementSwitch(Element element, String paramTypeString, MethodSpec.Builder builder, String editorSource, String keyword, String editorClose, boolean prefixed) {
        String invoke;
        switch (paramTypeString) {
            case "int":
                invoke = "putInt";
                break;
            case "float":
                invoke = "putFloat";
                break;
            case "long":
                invoke = "putLong";
                break;
            case "boolean":
                invoke = "putBoolean";
                break;
            case "java.lang.String":
                invoke = "putString";
                break;
            default:
                String message = "Unsupported type " + paramTypeString;
                error(element, message);
                throw new IllegalArgumentException(message);
        }

        if (prefixed) {
            builder.addStatement("$L.$L($L + $S, $L)$L", editorSource, invoke, "prefix", keyword, "value", editorClose);
        } else {
            builder.addStatement("$L.$L($S, $L)$L", editorSource, invoke, keyword, "value", editorClose);
        }
    }
}
