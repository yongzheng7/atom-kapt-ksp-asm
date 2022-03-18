package com.atom.module.core.router.common;

import androidx.annotation.NonNull;

import com.atom.module.core.router.core.UriCallback;
import com.atom.module.core.router.core.UriHandler;
import com.atom.module.core.router.core.UriRequest;
import com.atom.module.core.router.core.UriResult;

/**
 * 不支持的跳转链接，返回 {@link UriResult#CODE_NOT_FOUND}
 *
 * Created by jzj on 2018/3/22.
 */

public class NotFoundHandler extends UriHandler {

    public static final NotFoundHandler INSTANCE = new NotFoundHandler();

    @Override
    public boolean shouldHandle(@NonNull UriRequest request) {
        return true;
    }

    @Override
    protected void handleInternal(@NonNull UriRequest request, @NonNull UriCallback callback) {
        callback.onComplete(UriResult.CODE_NOT_FOUND);
    }

    @Override
    public String toString() {
        return "NotFoundHandler";
    }
}
