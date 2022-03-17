package com.atom.plugin.core.test.aap

import java.io.File

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: ScanSetting
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/2/15
 * @Copyright: 2022/2/15 www.rongdasoft.com Inc. All rights reserved.
 */
class ScanSetting {
    companion object {
        val PLUGIN_NAME = "com.atom.api"

        /**
         * 路由表的注册代码将生成插入到该类AbstractApiImplContext（路由中心）中
         */
        val GENERATE_TO_CLASS_NAME = "com/atom/core/AtomApi"

        /**
         * 路由表的注册代码将生成插入的类文件名
         */
        val GENERATE_TO_CLASS_FILE_NAME = "$GENERATE_TO_CLASS_NAME.class"

        /**
         * 注册代码将动态生成到loadRouterMap方法中
         */
        val GENERATE_TO_METHOD_NAME = "loadProxyClass"

        /**
         * annotationProcessor自动生成路由代码的包名
         */
        val ROUTER_CLASS_PACKAGE_NAME = "com/atom/apt"

        /**
         * register method name in class: {@link #GENERATE_TO_CLASS_NAME}
         */
        val REGISTER_METHOD_NAME = "registerClass"

        /**
         * 包含LogisticsCenter类的jar包文件 {@link #GENERATE_TO_CLASS_NAME}
         */
        var fileContainsInitClass: File? = null
    }
}

