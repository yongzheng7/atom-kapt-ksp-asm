package com.atom.compiler.ksp.aap.data

import com.atom.module.annotation.aap.AapImpl

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AbstractStudent
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/7
 * @Copyright: 2022/3/7 www.rongdasoft.com Inc. All rights reserved.
 */
@AapImpl(Person::class , name = "AbstractStudent" , version = 1)
abstract class AbstractStudent : Person {
}