package com.atom.apt.proxy

import com.atom.compiler.apt.aap.`data`.Man
import com.atom.compiler.apt.aap.`data`.ManStudent
import com.atom.compiler.apt.aap.`data`.Person
import com.atom.compiler.apt.aap.`data`.Teacher
import com.atom.module.`annotation`.aap.AapAutoClass
import com.atom.module.`annotation`.aap.AapImplEntry
import java.lang.SuppressWarnings

/**
 * <p>This is a class automatically generated by API annotation processor.</p>
 * @date 2022-06-08T18:11:34.276+0800
 */
@AapAutoClass(
  value = ["AapProcessor"],
  data = "2022-06-08T18:11:34.276+0800",
)
@SuppressWarnings(value = ["all"])
public class App : AapImplEntry {
  public constructor() : super() {
    add( "Teacher" , Man::class.java , Teacher::class.java , 2)
    add( "ManStudent" , Person::class.java , ManStudent::class.java , 3)
  }
}