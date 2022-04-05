package com.atom.compiler.apt.poet

import com.atom.compiler.apt.aap.AapOptions
import com.atom.compiler.apt.ext.createFile
import com.atom.compiler.apt.ext.getPath
import com.atom.compiler.apt.poet.common.JavaInterfaceClass0
import com.atom.compiler.apt.poet.common.KotlinInterfaceClass0
import com.atom.module.annotation.aap.AapImplEntry
import com.squareup.kotlinpoet.*
import org.junit.Before
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

    lateinit var savePath : String
    @Before
    fun before(){
        val projectPath = "D:\\app_git_android\\demo_asm\\test-plugin-compiler\\"
        savePath = projectPath+"compiler-apt\\src\\test\\kotlin\\com\\atom\\compiler\\apt\\poet\\data"
    }
    @Test
    fun `create class`(){
        val builder = FileSpec.builder(AapOptions.AAP_PACKET, "aap")
        val build = builder.build()
        try {
            build.writeTo(savePath.createFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Test
    fun `create class extend super`(){
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile (classPacket , className){
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className))
            typeSuper.superclass(ClassName.bestGuess(AapImplEntry::class.qualifiedName!!))
            typeSuper.addSuperinterface(ClassName.bestGuess(KotlinInterfaceClass0::class.qualifiedName!!))
            typeSuper.addSuperinterface(ClassName("com.atom.compiler.apt.poet.common" , "JavaInterfaceClass0"))
            //type.superclass(AapImplEntry::class)
            //type.superclass(ClassName("com.atom.module.annotation.aap", "AapImplEntry"))
            it.addType(typeSuper.build())
        }
    }


    fun createFile(packet:String =AapOptions.AAP_PACKET , name : String = "aap" , block : (FileSpec.Builder)->Unit){
        val builder = FileSpec.builder(packet, name)
        block.invoke(builder)
        val build = builder.build()
        try {
            build.writeTo(savePath.createFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}