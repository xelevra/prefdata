package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.Exportable;
import org.xelevra.prefdata.annotations.GenerateRemove;
import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.annotations.Prefixed;

@PrefData
@GenerateRemove
@Exportable
public abstract class Test {
    protected String name;
    protected int age;

    @Prefixed
    protected int childAge;


    abstract int getAge();
}
