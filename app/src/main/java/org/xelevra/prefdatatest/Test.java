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
    String name;

    @Keyword("AGE_OF_EMPIRE")
    int age;

    @Keyword("ДЖЕГУРДА")
    float ggurda;

    @Prefixed
    int childAge;

    boolean man;
}
