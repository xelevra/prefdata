package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.Exportable;
import org.xelevra.prefdata.annotations.GenerateRemove;
import org.xelevra.prefdata.annotations.Keyword;
import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.annotations.Prefixed;

@PrefData
@GenerateRemove
@Exportable
public abstract class Test {
    protected String name;

    @Keyword("AGE_OF_EMPIRE")
    protected int age;

    @Prefixed
    protected int childAge;
}
