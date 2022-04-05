package com.atom.apt.proxy

import com.atom.module.`annotation`.aap.AapImplEntry
import kotlin.String
import kotlin.Unit

public class aap(
  public val name: String
) : AapImplEntry() {
  public fun greet(): Unit {
    println("""Hello, $name""")
  }
}

public fun main(vararg args: String): Unit {
  aap(args[0]).greet()
}
