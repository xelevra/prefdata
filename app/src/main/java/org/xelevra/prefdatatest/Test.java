package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.annotations.Prefix;

@PrefData
public interface Test {
    String getText();
    Test setText(String text);

    int getValue(int val, @Prefix String v);

    String getAge(@Prefix String name);


    Test setAge(String age, @Prefix String name);

    Test edit();
    void commit();
}
