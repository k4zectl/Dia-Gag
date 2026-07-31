package dev.k4ze.diagag;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Dia-Gag — Xposed module for the My Dialog app (net.omobio.dialogsc).
 * Copyright (C) 2026 K4ZE DEV. Licensed under GNU GPL v3.0.
 */
public class Main implements IXposedHookLoadPackage {

    static final String TAG = "Dia-Gag";
    static final String TARGET = "net.omobio.dialogsc";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;
        Log.d(TAG, "Attached to " + lpparam.packageName);

        // TODO: hooks for My Dialog 18.6.1 go here, derived from the
        //       decompiled/net.omobio.dialogsc smali (root/debug/integrity checks).
    }

    /** Force a method to return a fixed boolean regardless of args. */
    static void forceBoolean(ClassLoader cl, String clazz, String method, final boolean value) {
        try {
            XposedHelpers.findAndHookMethod(clazz, cl, method,
                    XC_MethodReplacement.returnConstant(value));
        } catch (Throwable t) {
            Log.w(TAG, "forceBoolean failed: " + clazz + "#" + method, t);
        }
    }
}
