package com.atom.module.core.router.components;

import android.app.Activity;

import com.atom.module.core.router.activity.ActivityClassNameHandler;
import com.atom.module.core.router.activity.ActivityHandler;
import com.atom.module.core.router.common.NotExportedInterceptor;
import com.atom.module.core.router.core.UriHandler;
import com.atom.module.core.router.core.UriInterceptor;

import java.lang.reflect.Modifier;

/**
 * 跳转目标，支持ActivityClass, ActivityClassName, UriHandler。
 *
 * Created by jzj on 2018/3/26.
 */

public class UriTargetTools {

    public static UriHandler parse(Object target, boolean exported,
                                   UriInterceptor... interceptors) {
        UriHandler handler = toHandler(target);
        if (handler != null) {
            if (!exported) {
                handler.addInterceptor(NotExportedInterceptor.INSTANCE);
            }
            handler.addInterceptors(interceptors);
        }
        return handler;
    }

    private static UriHandler toHandler(Object target) {
        if (target instanceof UriHandler) {
            return (UriHandler) target;
        } else if (target instanceof String) {
            return new ActivityClassNameHandler((String) target);
        } else if (target instanceof Class && isValidActivityClass((Class) target)) {
            //noinspection unchecked
            return new ActivityHandler((Class<? extends Activity>) target);
        } else {
            return null;
        }
    }

    private static boolean isValidActivityClass(Class clazz) {
        return clazz != null && Activity.class.isAssignableFrom(clazz)
                && !Modifier.isAbstract(clazz.getModifiers());
    }
}
