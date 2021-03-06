package com.atom.compiler.test.ksp.result

/**
 * Created by benny at 2022/1/9 11:19 AM.
 */
class ResultCollector {
    private val expect = StringBuilder()
    private val actual = StringBuilder()

    fun collect(value: Any?) {
        collect(value, value)
    }

    fun collect(expect: Any?, actual: Any?) {
        this.expect.append(expect)
        this.actual.append(actual)
    }

    fun collectLine(value: Any?) {
        collectLine(value, value)
    }

    fun collectLine(expect: Any?, actual: Any?) {
        this.expect.append(expect).appendLine()
        this.actual.append(actual).appendLine()
    }

    fun collectModule(moduleName: String) {
        collectLine("// MODULE: $moduleName")
    }

    fun collectFile(fileName: String) {
        collectLine("// FILE: $fileName")
    }

    fun apply() {
        val apply = expect.toString() == actual.toString()
    }
}