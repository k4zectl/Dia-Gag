package dev.k4ze.diagag;

import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Dia-Gag — Xposed module for the My Dialog app (net.omobio.dialogsc).
 * Copyright (C) 2026 K4ZE DEV. Licensed under GNU GPL v3.0.
 *
 * Neutralises the anti-tamper of My Dialog 18.6.1 so it runs on rooted devices:
 *   - security.SecurityManager facade + tracker.* / RootInfoCallable detectors;
 *   - MainActivity$4.run() root/emulator/debug/hook detector set (N1,O1,...);
 *   - MainActivity block handlers A1/U1 (toast + finish);
 *   - RNDeviceInfo emulator report;
 *   - a null-guard for the react-native-os NetworkInterface crash at RN init.
 */
public class Main implements IXposedHookLoadPackage {

    static final String TAG = "Dia-Gag";
    static final String TARGET = "net.omobio.dialogsc";
    static final String P = TARGET + ".";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        // Phone process: unblock device-identifier reads for the target app so it
        // reads its OWN real IMSI via the normal API (Android 10+ hides it).
        if ("com.android.phone".equals(lpparam.packageName)) {
            allowImsiReads(lpparam.classLoader);
            return;
        }
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

        // --- MainActivity native/Java detectors (18.6.1) ---
        // MainActivity$4.run() evaluates these booleans on a background thread;
        // any true -> B1()/A1() -> finish(). Force the whole set to false.
        for (String m : new String[]{"N1", "O1", "J1", "E1", "L1", "D1",
                "F1", "M1", "G1", "I1", "w1"}) {
            force(cl, P + "MainActivity", m, false);
        }
        // Utils.e(Context) is part of the same detector set.
        force(cl, P + "Utils", "e", false);

        // --- MainActivity block handler (18.6.1) ---
        // Both toast+exit paths (root, "security standards") funnel through
        // MainActivity.A1(int) = show toast + finish(); U1(int) shows the toast.
        // Neutralise both -> no block toast, no finish.
        noop(cl, P + "MainActivity", "A1");
        noop(cl, P + "MainActivity", "U1");

        // --- RN layer (18.6.1) ---
        // react-native-device-info emulator report -> false.
        force(cl, "com.learnium.RNDeviceInfo.RNDeviceModule", "isEmulatorSync", false);

        // Crash guard: react-native-os RNOS.getConstants() iterates
        // NetworkInterface.getNetworkInterfaces(), which returns null on this
        // device -> NPE that kills RN init. Return an empty enumeration instead.
        try {
            XposedHelpers.findAndHookMethod(NetworkInterface.class, "getNetworkInterfaces",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (param.getResult() == null) {
                                param.setResult(Collections.emptyEnumeration());
                            }
                        }
                    });
        } catch (Throwable t) {
            Log.w(TAG, "NetworkInterface guard failed", t);
        }
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

    /**
     * In the phone process, make TelephonyPermissions grant the target app
     * read access to device identifiers (IMSI/IMEI), so getSubscriberId returns
     * the real value instead of null. Scoped strictly to net.omobio.dialogsc.
     */
    static void allowImsiReads(ClassLoader cl) {
        final Class<?> tp;
        try {
            tp = XposedHelpers.findClass(
                    "com.android.internal.telephony.TelephonyPermissions", cl);
        } catch (Throwable t) {
            Log.w(TAG, "TelephonyPermissions not found", t);
            return;
        }
        XC_MethodHook grant = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                for (Object a : param.args) {
                    if (TARGET.equals(a)) {          // callingPackage == our app
                        param.setResult(Boolean.TRUE);
                        return;
                    }
                }
            }
        };
        for (String m : new String[]{
                "checkCallingOrSelfReadDeviceIdentifiers",
                "checkCallingOrSelfReadSubscriberIdentifiers"}) {
            try {
                XposedBridge.hookAllMethods(tp, m, grant);
            } catch (Throwable t) {
                Log.w(TAG, "grant hook failed: " + m, t);
            }
        }
        Log.d(TAG, "IMSI reads allowed for " + TARGET);
    }

    /** Turn every overload of {@code clazz#method} into a no-op. */
    static void noop(ClassLoader cl, String clazz, String method) {
        try {
            Class<?> c = XposedHelpers.findClass(clazz, cl);
            XposedBridge.hookAllMethods(c, method, XC_MethodReplacement.DO_NOTHING);
        } catch (Throwable t) {
            Log.w(TAG, "noop failed: " + clazz + "#" + method, t);
        }
    }
}
