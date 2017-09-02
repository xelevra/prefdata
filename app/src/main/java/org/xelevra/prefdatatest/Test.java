package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.Belongs;
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

    @Belongs({"0", "1", "5", "666", "303"})
    int buratinoMoneyCount;

    @Belongs({"Bazilio the Cat", "Alisa the Fox", "Karabas Barabas"})
    String enemy;

    @Belongs({"12.4", "11.2"})
    float noseLength;

    @Belongs({"11222222222", "11222222223"})
    long poleChudesCoinHarvest;

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
