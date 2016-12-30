package org.xelevra.prefdata.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.processor.generators.CommitApplyGenerator;
import org.xelevra.prefdata.processor.generators.EditGenerator;
import org.xelevra.prefdata.processor.generators.GetterGenerator;
import org.xelevra.prefdata.processor.generators.MethodGenerator;
import org.xelevra.prefdata.processor.generators.SetterGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

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
        for (Element element : roundEnv.getElementsAnnotatedWith(PrefData.class)){
            checking &= checkInterface(element);
        }

        if(!checking) return false;

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefData.class)) {
            processElement(element, roundEnv);
        }

        return true;
    }

    private void processElement(Element element, RoundEnvironment roundEnv){
        final ClassName className = ClassName.bestGuess("Pref"+ element.getSimpleName());
        TypeName sharedPreferences = ClassName.bestGuess("android.content.SharedPreferences");
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addField(sharedPreferences, "preferences", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(sharedPreferences, "sharedPreferences")
                                .addStatement("preferences = sharedPreferences")
                                .build()
                ).addSuperinterface(TypeName.get(element.asType()));

        boolean hasChain = false;
        for (Element el : element.getEnclosedElements()){
            if(el instanceof ExecutableElement && el.getSimpleName().toString().equals("edit")){
                hasChain = true;
                builder.addField(FieldSpec.builder(
                        ClassName.bestGuess("android.content.SharedPreferences.Editor"),
                        "editor",
                        Modifier.PRIVATE
                ).build());
                break;
            }
        }

        GetterGenerator getterGenerator = new GetterGenerator(TypeName.get(element.asType()), processingEnv);
        SetterGenerator setterGenerator = new SetterGenerator(TypeName.get(element.asType()), processingEnv, hasChain);


        for (Element el : element.getEnclosedElements()){
            if(el instanceof ExecutableElement) {
                ExecutableElement method = (ExecutableElement) el;
                String name = el.getSimpleName().toString();
                if(name.startsWith("get") || name.startsWith("is")){
                    getterGenerator.check(method);
                    builder.addMethod(getterGenerator.create(method));
                } else if(name.startsWith("set")){
                    setterGenerator.check(method);
                    builder.addMethod(setterGenerator.create(method));
                } else if(name.equals("edit")){
                    MethodGenerator editGenerator = new EditGenerator(TypeName.get(element.asType()), processingEnv);
                    editGenerator.check(method);
                    builder.addMethod(editGenerator.create(method));
                } else if(name.equals("apply") || name.equals("commit")){
                    MethodGenerator applyGenerator = new CommitApplyGenerator(TypeName.get(element.asType()), processingEnv);
                    applyGenerator.check(method);
                    builder.addMethod(applyGenerator.create(method));
                }
            }
        }

        try {
            JavaFile javaFile = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(element).toString(), builder.build())
                    .build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkInterface(Element element){
        boolean result = element.getKind().isInterface();
        if(!result) error(element, "must be an interface");

        boolean hasEdit = false, hasApply = false, hasCommit = false;

        for (Element el : element.getEnclosedElements()){
            if(el instanceof ExecutableElement){
                String name = el.getSimpleName().toString();
                hasEdit |= name.equals("edit");
                hasApply |= name.equals("apply");
                hasCommit |= name.equals("commit");
            }
        }

        if(hasEdit && (!hasApply && !hasCommit)){
            error(element, "edit() without apply()");
        }
        if(hasApply && !hasEdit){
            error(element, "apply() without edit()");
        }
        if(hasCommit && !hasEdit){
            error(element, "commit() without edit()");
        }

        return result;
    }

    void error(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }


    private void warning(Element e, String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, e);
    }

}
