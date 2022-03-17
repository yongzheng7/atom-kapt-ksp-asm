package com.atom.compiler.ksp

import com.atom.module.aap.annotation.AapImplEntry
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.junit.Test
import java.io.File
import java.io.IOException
//https://square.github.io/javapoet/1.x/javapoet/
// https://github.com/square/javapoet
class TestJavapoet {
    @Test
    fun test() {

        val pageConfigClassName = ClassName.get("com.atom.apt", "app")
        val pageConfigBuilder = TypeSpec.classBuilder(pageConfigClassName)
            .superclass(ClassName.get(AapImplEntry::class.java))
        val builder = JavaFile.builder("com.atom.apt", pageConfigBuilder.build())
        val build = builder.build()
        val file = File("D:\\app_git_android\\demo_asm\\test-kapt-ksp\\test-kapt-ksp\\src\\test\\kotlin")
        try {
            build.writeTo(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}