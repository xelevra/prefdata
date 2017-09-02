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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class SetterGenerator extends MethodGenerator {
    public SetterGenerator(ProcessingEnvironment processingEnv, TypeSpec.Builder builder) {
        super(processingEnv, builder);
    }

    @Override
    public void processField(VariableElement field) {
        if (!check(field)) return;

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

        String keyLiteral = getKeyword(field);
        builder.beginControlFlow("if(editor == null)");

        addStatementSwitch(paramTypeName.toString(), builder, "preferences.edit()", keyLiteral, ".apply()", prefixed);
        builder.nextControlFlow("else");
        addStatementSwitch(paramTypeName.toString(), builder, "editor", keyLiteral, "", prefixed);
        builder.endControlFlow();


        builder.addStatement("return this");

        classBuilder.addMethod(builder.build());
    }


    private void addValidationBlockIfNeeded(MethodSpec.Builder builder, VariableElement field){
        Belongs annotation = field.getAnnotation(Belongs.class);
        if (annotation == null || !annotation.validation()) return;

        Class boxed = mapFromPrimitive(field.asType());

        CodeBlock.Builder initializer = CodeBlock.builder();
        initializer.add("$T.asList(", Arrays.class);

        String formatter;
        if(boxed == String.class) formatter = "$S";
        else if(boxed == Float.class) formatter = "$LF";
        else if(boxed == Long.class) formatter = "$LL";
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


    private Class mapFromPrimitive(TypeMirror typeMirror) {
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
                throw new IllegalArgumentException("Incorrect type: " + typeMirror);
        }
    }

    private void addStatementSwitch(String paramTypeString, MethodSpec.Builder builder, String editorSource, String keyLiteral, String editorClose, boolean prefixed) {
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
                throw new IllegalArgumentException("Unsupported type " + paramTypeString);
        }

        if (prefixed) {
            builder.addStatement("$L.$L($L + $S, $L)$L", editorSource, invoke, "prefix", keyLiteral, "value", editorClose);
        } else {
            builder.addStatement("$L.$L($S, $L)$L", editorSource, invoke, keyLiteral, "value", editorClose);
        }
    }
}
