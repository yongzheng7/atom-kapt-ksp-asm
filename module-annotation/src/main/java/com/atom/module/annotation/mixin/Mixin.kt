package com.atom.module.annotation.mixin

/**
 * Created by benny.
 */
@Target(AnnotationTarget.CLASS)
annotation class Mixin(
    val packageName: String,
    val name: String
)