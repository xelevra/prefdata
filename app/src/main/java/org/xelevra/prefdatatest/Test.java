package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.PrefData;
import org.xelevra.prefdata.annotations.Prefix;

@PrefData
public interface Test {
    String getText();
    Test setText(String text);

    int getValue(int val, @Prefix String v);

    String getAge(@Prefix String name);

    boolean isBoo();
    void setBoo(boolean boo);

    Boolean isValue();

    Test setAge(String age, @Prefix String name);
    Test edit();

    void commit();

    Test removeBoo(@Prefix String value);
    Test removeWater();
}
