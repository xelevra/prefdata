package org.xelevra.prefdata.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Use {
    String[] value();
    boolean asGetter() default true;
    boolean asSetter() default true;
}
