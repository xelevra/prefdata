package org.xelevra.prefdatatest;

import org.xelevra.prefdata.annotations.PrefData;

@PrefData
public interface Test {
    String getText();
    Test setText(String text);
    int getNumber();
    void setNumber(int number);

    Test edit();
    void apply();
    void commit();
}
