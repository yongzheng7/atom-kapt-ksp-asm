package result.test.apt

import com.atom.compiler.apt.common.JavaInterfaceClass1
import com.atom.compiler.apt.common.KotlinInterfaceClass0
import com.atom.compiler.apt.common.KotlinInterfaceClass1
import com.atom.module.`annotation`.aap.AapImplEntry

public class testJavaAndKotlinInterface : AapImplEntry(), KotlinInterfaceClass0,
    KotlinInterfaceClass1, JavaInterfaceClass1
