package com.atom.compiler.apt.poet

import com.atom.compiler.apt.aap.AapOptions
import com.atom.compiler.apt.ext.createFile
import com.atom.compiler.apt.ext.replaceAll
import com.atom.compiler.apt.poet.common.JavaAnnotation
import com.atom.compiler.apt.poet.common.KotlinAnnotation
import com.atom.compiler.apt.poet.common.KotlinInterfaceClass0
import com.atom.module.annotation.aap.AapImplEntry
import com.squareup.kotlinpoet.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.*

//https://square.github.io/kotlinpoet/
class TestKotlinpoet {
    @Test
    fun ` simple code`() {
        val packageName = JavaAnnotation::class.java.packageName
        println(packageName)
        //println(JavaAnnotation::class.java.packageName.replaceAll("[.]", "/"))
    }

    @Test
    fun test() {
        createFile {
            val pageConfigClassName = ClassName(AapOptions.AAP_PACKET, "aap")
            val build = TypeSpec.classBuilder(ClassName(AapOptions.AAP_PACKET, "aap"))
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
            it.addType(build)
            it.addFunction(
                FunSpec.builder("main")
                    .addParameter("args", String::class, KModifier.VARARG)
                    .addStatement("%T(args[0]).greet()", pageConfigClassName)
                    .build()
            )
        }
    }


    fun createFile(
        packet: String = AapOptions.AAP_PACKET,
        name: String = "aap",
        block: (FileSpec.Builder) -> Unit
    ) {
        val builder = FileSpec.builder(packet, name)
        block.invoke(builder)
        val build = builder.build()
        try {
            build.writeTo(savePath.createFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    lateinit var savePath: String

    @Before
    fun before() {
        val projectPath = "D:\\app_git_android\\demo_asm\\test-plugin-compiler\\"
        savePath =
            projectPath + "compiler-apt\\src\\test\\kotlin\\com\\atom\\compiler\\apt\\poet\\data"
    }

    @Test
    fun `create class`() {
        val builder = FileSpec.builder(AapOptions.AAP_PACKET, "aap")
        val build = builder.build()
        try {
            build.writeTo(savePath.createFile())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Test
    fun `create class extend super`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )
            typeSuper.superclass(ClassName.bestGuess(AapImplEntry::class.qualifiedName!!))
            typeSuper.addSuperinterface(ClassName.bestGuess(KotlinInterfaceClass0::class.qualifiedName!!))
            typeSuper.addSuperinterface(
                ClassName(
                    "com.atom.compiler.apt.poet.common",
                    "JavaInterfaceClass0"
                )
            )
            //type.superclass(AapImplEntry::class)
            //type.superclass(ClassName("com.atom.module.annotation.aap", "AapImplEntry"))
            it.addType(typeSuper.build())
        }
    }

    @Test
    fun `create class interface super`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )
            typeSuper.addSuperinterface(ClassName.bestGuess(KotlinInterfaceClass0::class.qualifiedName!!))
            typeSuper.addSuperinterface(
                ClassName(
                    "com.atom.compiler.apt.poet.common",
                    "JavaInterfaceClass0"
                )
            )
            //type.superclass(AapImplEntry::class)
            //type.superclass(ClassName("com.atom.module.annotation.aap", "AapImplEntry"))
            it.addType(typeSuper.build())
        }
    }

    @Test
    fun `create class add code block with doc`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )

            // 代码创建 文档
            val doc: CodeBlock = CodeBlock.builder()
                .add("<p>This is a class automatically generated by API annotation processor, which is used to register the page automatically.</p>")
                .add("\n")
                .add("@date ").add(Date().toString())
                .add("\n")
                .build()

            typeSuper.addKdoc(doc)
            it.addType(typeSuper.build())
        }
    }

    @Test
    fun `create class add code block with annotation`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )

            val builder = AnnotationSpec.builder(
                ClassName(
                    "com.atom.compiler.apt.poet.common",
                    "JavaAnnotation"
                )
            )
            typeSuper.annotationSpecs.add(builder.build())
            typeSuper.addAnnotation(KotlinAnnotation::class)


            it.addType(typeSuper.build())
        }
    }

    @Test
    fun `create class add func constructor`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )
            // ----------------------------
            val flux = FunSpec.constructorBuilder()
                .addParameter("greeting", String::class)
                .addStatement("this.%N = %N", "greeting", "greeting")
                .build()

            typeSuper
                .addProperty("greeting", String::class, KModifier.PRIVATE)
                .addFunction(flux)
                .build()

            it.addType(typeSuper.build())
        }
    }
    @Test
    fun `create class add func primary constructor`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )
            // ----------------------------
            val flux = FunSpec.constructorBuilder()
                .addParameter("greeting", String::class)
                .build()

            typeSuper.primaryConstructor(flux)
                .addProperty(
                    PropertySpec.builder("greeting", String::class)
                        .initializer("greeting")
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                )
                .build()

            it.addType(typeSuper.build())
        }
    }

    @Test
    fun `create class add parameters`() {
        val classPacket = "com.atom.apt"
        val className = "Aap"
        createFile(classPacket, className) {
            val typeSuper = TypeSpec.classBuilder(
                ClassName(classPacket, className)
            )
            // ----------------------------
            val android = ParameterSpec.builder("android", String::class)
                .defaultValue("\"pie\"")
                .build()

            val welcomeOverlords = FunSpec.builder("welcomeOverlords")
                .addParameter(android)
                .addParameter("robot", String::class)
                .build()

            typeSuper.addFunction(welcomeOverlords)

            it.addType(typeSuper.build())
        }
    }


}