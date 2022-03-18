package com.atom.module.core.router.fragment;
/*
 * Copyright (C) 2005-2018 Meituan Inc.All Rights Reserved.
 * Description：
 * History：
 *
 * @desc
 * @author chenmeng06
 * @date 2019/3/7
 */

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.atom.module.core.router.core.Debugger;
import com.atom.module.core.router.core.UriRequest;

/**
 * 通过Uri 创建 Fragment的Request
 * 通过 FragmentBuildUriRequest.FRAGMENT 获取返回的Fragment
 */
public class FragmentBuildUriRequest extends AbsFragmentUriRequest {
    public final static String FRAGMENT = "CUSTOM_FRAGMENT_OBJ";

    public FragmentBuildUriRequest(@NonNull Context context, String uri) {
        super(context, uri);
    }

    @Override
    protected StartFragmentAction getStartFragmentAction() {
        return new StartFragmentAction() {
            @Override
            public boolean startFragment(@NonNull UriRequest request, @NonNull Bundle bundle) throws ActivityNotFoundException, SecurityException {
                String fragmentClassName = request.getStringField(FragmentTransactionHandler.FRAGMENT_CLASS_NAME);
                if (TextUtils.isEmpty(fragmentClassName)) {
                    Debugger.fatal("FragmentBuildUriRequest.handleInternal()应返回的带有ClassName");
                    return false;
                }
                try {
                    Fragment fragment = Fragment.instantiate(request.getContext(), fragmentClassName, bundle);
                    if (fragment == null) {
                        return false;
                    }
                    //自定义处理不做transaction，直接放在request里面回调
                    request.putField(FRAGMENT,fragment);
                    return true;
                } catch (Exception e) {
                    Debugger.e(e);
                    return false;
                }
            }
        };
    }
}
