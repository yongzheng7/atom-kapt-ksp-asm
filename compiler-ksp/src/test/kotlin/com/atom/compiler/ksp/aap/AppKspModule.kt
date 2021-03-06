package com.atom.apt.proxy

import com.atom.compiler.ksp.aap.`data`.ManStudent
import com.atom.compiler.ksp.aap.`data`.Person
import com.atom.compiler.ksp.aap.`data`.Teacher
import com.atom.compiler.ksp.aap.`data`.Teacher2
import com.atom.compiler.ksp.aap.`data`.Teacher3
import com.atom.module.`annotation`.aap.AapAutoClass
import com.atom.module.`annotation`.aap.AapImplEntry
import java.lang.SuppressWarnings

/**
 * <p>This is a class automatically generated by API annotation processor.</p>
 * @date 2022-06-08T23:27:13.351+0800
 */
@AapAutoClass(
  value = ["AapSymbolProcessorProvider"],
  data = "2022-06-08T23:27:13.351+0800",
)
@SuppressWarnings(value = ["all"])
public class AppKspModule : AapImplEntry {
  public constructor() : super() {
    add( "ManStudent" , Person::class.java , ManStudent::class.java , 3)
    add( "Teacher" , Person::class.java , Teacher::class.java , 2)
    add( "Teacher" , Person::class.java , Teacher2::class.java , 2)
    add( "Teacher" , Person::class.java , Teacher3::class.java , 2)
  }
}
