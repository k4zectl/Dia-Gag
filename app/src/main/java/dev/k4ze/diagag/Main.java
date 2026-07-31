package dev.k4ze.diagag;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Dia-Gag — Xposed module for the My Dialog app (net.omobio.dialogsc).
 * Copyright (C) 2026 K4ZE DEV. Licensed under GNU GPL v3.0.
 *
 * Neutralises the app's two enforcement paths (mapped against 18.6.1):
 *   A) security.SecurityManager.performComprehensiveSecurityCheck facade, and
 *   B) the tracker.* / RootInfoCallable detectors called from MainActivity/Utils.
 */
public class Main implements IXposedHookLoadPackage {

    static final String TAG = "Dia-Gag";
    static final String TARGET = "net.omobio.dialogsc";
    static final String P = TARGET + ".";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;
        final ClassLoader cl = lpparam.classLoader;
        Log.d(TAG, "Attached to " + lpparam.packageName);

        // --- Path A: security facade (true = SAFE) ---
        force(cl, P + "security.SecurityManager", "performComprehensiveSecurityCheck", true);
        force(cl, P + "security.SecurityValidator", "isSecure", true);

        // --- Path B: MainActivity tracker detectors (true = THREAT -> force false) ---
        force(cl, P + "tracker.debugger.FindDebugger", "a", false);
        force(cl, P + "tracker.debugger.FindDebugger", "b", false);
        force(cl, P + "tracker.debugger.FindDebugger", "c", false);

        for (String m : new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "k"}) {
            force(cl, P + "tracker.emulator.FindEmulator", m, false);
        }

        force(cl, P + "tracker.monkey.FindMonkey", "a", false);

        force(cl, P + "tracker.taint.FindTaint", "a", false);
        force(cl, P + "tracker.taint.FindTaint", "b", false);
        force(cl, P + "tracker.taint.FindTaint", "hasTaintClass", false);

        // RootInfoCallable.a() returns Boolean; false = not rooted.
        force(cl, P + "support.lib.RootInfoCallable", "a", false);
    }

    /**
     * Force every overload of {@code clazz#method} to return {@code value}.
     * Uses hookAllMethods so Context/no-arg variants are all covered by name.
     * {@code value} autoboxes for methods returning Boolean.
     */
    static void force(ClassLoader cl, String clazz, String method, boolean value) {
        try {
            Class<?> c = XposedHelpers.findClass(clazz, cl);
            XposedBridge.hookAllMethods(c, method, XC_MethodReplacement.returnConstant(value));
        } catch (Throwable t) {
            Log.w(TAG, "hook failed: " + clazz + "#" + method, t);
        }
    }
}
