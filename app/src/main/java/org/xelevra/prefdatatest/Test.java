package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.PrefData;

@PrefData
public abstract class Test {
    protected String name;
    protected int age;


    abstract int getAge();
}
