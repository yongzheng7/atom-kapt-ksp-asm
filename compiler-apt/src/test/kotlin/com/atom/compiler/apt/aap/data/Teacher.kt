package com.atom.compiler.apt.aap.data

import com.atom.module.annotation.aap.AapImpl
import kotlin.jvm.Throws

@AapImpl(Person::class, name = "Teacher" , version = 2)
class Teacher : Person {

    constructor()

    constructor(name: String)

    @Throws(Exception::class)
    constructor(name: String, ago: Int)

    private constructor(name: String, ago: Int, address: String)

    protected constructor(name: String, ago: Int, address: String, phone: String)
}