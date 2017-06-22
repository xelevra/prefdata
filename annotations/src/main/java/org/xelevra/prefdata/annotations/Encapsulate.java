package org.xelevra.prefdata.annotations;

public @interface Encapsulate {
    boolean getter() default true;
    boolean setter() default true;
    boolean remove() default true;
}
