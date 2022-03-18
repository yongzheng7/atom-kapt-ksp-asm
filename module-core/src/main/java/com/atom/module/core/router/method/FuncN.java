package com.atom.module.core.router.method;

public interface FuncN<R> extends Function {
    R call(Object... args);
}
