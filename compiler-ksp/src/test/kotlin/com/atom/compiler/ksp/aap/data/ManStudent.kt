package com.atom.compiler.ksp.aap.data

import com.atom.module.aap.annotation.AapImpl

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AbstractStudent
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/7
 * @Copyright: 2022/3/7 www.rongdasoft.com Inc. All rights reserved.
 */
@AapImpl(Person::class, name = "ManStudent" , version = 3)
class ManStudent() : Person {

    constructor(name: String) : this()

    constructor(name: String, ago: Int) : this()

    private constructor(name: String, ago: Int, address: String) : this()

    protected constructor(name: String, ago: Int, address: String, phone: String) : this()
}