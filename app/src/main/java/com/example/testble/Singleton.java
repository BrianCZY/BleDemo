package com.example.testble;

import android.content.Context;

public class Singleton {
    private Singleton() {
    }

    static Context context;

    public static Singleton getSingleton(Context context1) {
        context = context1;
        return SingletonHolder.singleton;
    }

    static private class SingletonHolder {
        private static Singleton singleton = new Singleton();
    }
}
