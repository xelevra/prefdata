package org.xelevra.prefdatatest

import org.xelevra.prefdata.annotations.Keyword
import org.xelevra.prefdata.annotations.PrefData

@PrefData
abstract class TestOnKotlin {
    abstract var name: String
    abstract val age: Int

    @Keyword("TOP")
    abstract val top: Int
}