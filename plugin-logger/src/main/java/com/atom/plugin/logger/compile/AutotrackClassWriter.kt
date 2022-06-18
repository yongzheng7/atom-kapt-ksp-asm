package com.atom.plugin.logger.compile

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class AutotrackClassWriter : ClassWriter {

    constructor(classReader: ClassReader?, flags: Int) : super(classReader, flags)

    fun getApi(): Int {
        return api
    }
}