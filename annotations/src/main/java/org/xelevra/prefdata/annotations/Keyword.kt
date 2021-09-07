package org.xelevra.prefdata.annotations

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY
)
annotation class Keyword(val value: String)