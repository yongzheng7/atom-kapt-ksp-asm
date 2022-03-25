package com.atom.compiler.ksp

import com.atom.module.annotation.aap.AapImplEntry
import com.squareup.kotlinpoet.*
import org.junit.Test
import java.io.File
import java.io.IOException

//https://square.github.io/kotlinpoet/
class TestKotlinpoet {
    @Test
    fun test() {
        println("----------------------------------------------------------------------------------")
        val pageConfigClassName = ClassName("com.atom.apt", "aap")

        val builder = FileSpec.builder("com.atom.apt", "aap")
        builder.addType(
            TypeSpec.classBuilder(ClassName("com.atom.apt", "aap"))
                .superclass(ClassName.bestGuess(AapImplEntry::class.qualifiedName!!))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("name", String::class)
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("name", String::class)
                        .initializer("name")
                        .build()
                )
                .addFunction(
                    FunSpec.builder("greet")
                        .addStatement("println(%P)", "Hello, \$name")
                        .build()
                )
                .build()
        )
        builder.addFunction(
            FunSpec.builder("main")
                .addParameter("args", String::class, KModifier.VARARG)
                .addStatement("%T(args[0]).greet()", pageConfigClassName)
                .build()
        )

        val build = builder.build()
        val result =
            File("D:\\app_git_android\\demo_asm\\test-kapt-ksp\\test-kapt-ksp\\src\\test\\kotlin")
        try {
            build.writeTo(System.out)
            build.writeTo(result)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        println("----------------------------------------------------------------------------------")
    }
}