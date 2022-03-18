package com.atom.module.core.router.service;

import androidx.annotation.NonNull;

import com.atom.module.core.router.utils.ProviderPool;

public class ProviderFactory implements IFactory {

    public static final IFactory INSTANCE = new ProviderFactory();

    private ProviderFactory() {

    }

    @NonNull
    @Override
    public <T> T create(@NonNull Class<T> clazz) throws Exception {
        return ProviderPool.create(clazz);
    }
}
