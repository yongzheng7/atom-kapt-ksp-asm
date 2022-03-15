package com.atom.compiler.test.ksp.source

/**
 * Created by benny at 2022/1/7 10:28 AM.
 */
interface ModuleInfoLoader {
    fun loadSourceModuleInfos(): Collection<SourceModuleInfo>

    fun loadExpectModuleInfos(): Collection<ExpectModuleInfo>
}