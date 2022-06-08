package com.atom.compiler.ksp.aap.data

import com.atom.module.annotation.aap.AapImpl
import kotlin.jvm.Throws

@AapImpl(api = Person::class, name = "Teacher" , version = 2)
class Teacher : Person {

    constructor()

    protected constructor(name: String)

    @Throws(Exception::class)
    public constructor(name: String, ago: Int)

    private constructor(name: String, ago: Int, address: String)

    protected constructor(name: String, ago: Int, address: String, phone: String)
}