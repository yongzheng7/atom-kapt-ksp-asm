package com.atom.compiler.ksp.aap.data

import com.atom.module.annotation.aap.AapImpl
import kotlin.jvm.Throws

@AapImpl(api = Person::class, name = "Teacher" , version = 2)
class Teacher2() : Person {

}