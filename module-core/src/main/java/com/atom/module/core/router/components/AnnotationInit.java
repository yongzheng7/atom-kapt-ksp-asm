package com.atom.module.core.router.components;

import com.atom.module.core.router.core.UriHandler;

/**
 * Created by jzj on 2018/4/28.
 */

public interface AnnotationInit<T extends UriHandler> {

    void init(T handler);
}
