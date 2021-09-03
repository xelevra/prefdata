package org.xelevra.prefdatatest

import org.xelevra.prefdata.annotations.PrefData

@PrefData
abstract class TestOnKotlin {
    abstract var name: String
    abstract val age: Int
}

@PrefData
interface TestOnKotlinInterface {
    val one: Int
    var two: String

    fun edit(): TestOnKotlinInterface

    fun apply()

    fun removeOne()
}