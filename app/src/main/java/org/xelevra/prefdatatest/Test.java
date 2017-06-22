package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.Encapsulate;
import org.xelevra.prefdata.annotations.Exportable;
import org.xelevra.prefdata.annotations.Keyword;
import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.annotations.Prefixed;
import org.xelevra.prefdata.annotations.Use;

@PrefData
@Exportable
public abstract class Test {
    String name;

    @Keyword("AGE_OF_EMPIRE")
    int age;

    @Keyword("ДЖЕГУРДА")
    float ggurda;

    @Prefixed
    int childAge;

    boolean man;

    @Encapsulate
    int number;


    @Use(value = {"number", "man"}, asSetter = false)
    public String getManNumber(){
        return "Is man=" + man + ", " + number;
    }

    @Use(value = {"number", "man"})
    public String getManNumber2(){
        return "Is man=" + man + ", " + number;
    }

    @Use(value = {"number", "man"}, asGetter = false)
    public Test setData(String numberString){
        number = Integer.parseInt(numberString);
        man = true;
        return this;
    }
}
