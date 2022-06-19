package com.atom.bytecode;

import android.app.Application;

import com.atom.module.logger.Logger;

public class App extends Application {
    static {
        Logger.init(true , false , "");
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
