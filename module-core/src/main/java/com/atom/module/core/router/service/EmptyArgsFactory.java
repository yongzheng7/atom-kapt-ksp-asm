package com.atom.module.core.router.service;

import androidx.annotation.NonNull;

/**
 * 无参数构造
 *
 * Created by jzj on 2018/3/30.
 */

public class EmptyArgsFactory implements IFactory {

    public static final EmptyArgsFactory INSTANCE = new EmptyArgsFactory();

    private EmptyArgsFactory() {

    }

    @NonNull
    @Override
    public <T> T create(@NonNull Class<T> clazz) throws Exception {
        return clazz.newInstance();
    }
}
