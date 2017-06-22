package org.xelevra.prefdata.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.Exportable;
import org.xelevra.prefdata.annotations.Exporter;
import org.xelevra.prefdata.annotations.GenerateRemove;
import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.annotations.Prefixed;
import org.xelevra.prefdata.annotations.Use;
import org.xelevra.prefdata.processor.generators.ClearGenerator;
import org.xelevra.prefdata.processor.generators.CommitApplyGenerator;
import org.xelevra.prefdata.processor.generators.EditGenerator;
import org.xelevra.prefdata.processor.generators.ExportFieldsGenerator;
import org.xelevra.prefdata.processor.generators.GetterGenerator;
import org.xelevra.prefdata.processor.generators.MethodGenerator;
import org.xelevra.prefdata.processor.generators.RemoveGenerator;
import org.xelevra.prefdata.processor.generators.SetterGenerator;
import org.xelevra.prefdata.processor.generators.TopLevelMethodsOverrider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PrefDataProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(PrefData.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean checking = true;
        for (Element element : roundEnv.getElementsAnnotatedWith(PrefData.class)) {
            checking &= checkAbstractClass(element);
        }

        if (!checking) return false;

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefData.class)) {
            processElement(element);
        }

        return true;
    }

    private void processElement(Element element) {
        final ClassName className = ClassName.bestGuess("Pref" + element.getSimpleName());
        TypeName sharedPreferences = ClassName.bestGuess("android.content.SharedPreferences");
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .superclass(TypeName.get(element.asType()))
                .addModifiers(Modifier.PUBLIC)
                .addField(sharedPreferences, "preferences", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(sharedPreferences, "sharedPreferences")
                                .addStatement("preferences = sharedPreferences")
                                .build()
                );


        builder.addField(FieldSpec.builder(
                ClassName.bestGuess("android.content.SharedPreferences.Editor"),
                "editor",
                Modifier.PRIVATE
        ).build());

        boolean generateRemoves = element.getAnnotation(GenerateRemove.class) != null;
        boolean exportable = element.getAnnotation(Exportable.class) != null;

        GetterGenerator getterGenerator = new GetterGenerator(processingEnv, builder);
        SetterGenerator setterGenerator = new SetterGenerator(processingEnv, builder);
        RemoveGenerator removeGenerator = new RemoveGenerator(processingEnv, builder);

        VariableElement field;

        List<VariableElement> exportableFields = new ArrayList<>();
        List<VariableElement> processingFields = new ArrayList<>();
        List<ExecutableElement> processingMethods = new ArrayList<>();

        for (Element el : element.getEnclosedElements()) {
            if (el instanceof VariableElement) {
                field = (VariableElement) el;
                processingFields.add(field);
                if((exportable || field.getAnnotation(Exportable.class) != null) && field.getAnnotation(Prefixed.class) == null){
                    exportableFields.add(field);
                }
            } else if (el instanceof ExecutableElement && el.getAnnotation(Use.class) != null){
                processingMethods.add((ExecutableElement) el);
            }
        }

        for (VariableElement el : processingFields){
            getterGenerator.processField(el);
            setterGenerator.processField(el);
            if(generateRemoves || el.getAnnotation(GenerateRemove.class) != null) {
                removeGenerator.processField(el);
            }
        }

        TopLevelMethodsOverrider topLevelMethodsOverrider = new TopLevelMethodsOverrider(processingEnv, builder);
        for (ExecutableElement el : processingMethods){
            topLevelMethodsOverrider.processMethod(
                    el,
                    processingFields,
                    el.getReturnType().equals(element.asType())
            );
        }

        new EditGenerator(processingEnv, builder).processField(null);
        new CommitApplyGenerator(processingEnv, builder).processField(null);
        new ClearGenerator(processingEnv, builder).processField(null);

        if(!exportableFields.isEmpty()) {
            builder.addSuperinterface(Exporter.class);
            new ExportFieldsGenerator(processingEnv, builder).generateMembers(exportableFields);
        }

        try {
            JavaFile javaFile = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(element).toString(), builder.build())
                    .build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkAbstractClass(Element element) {
        boolean result = element.getKind().isClass();
        if (!result) error(element, "must be an class");
        if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
            error(element, "must be abstract");
            result = false;
        }

        return result;
    }

    private void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }


    private void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }

}
