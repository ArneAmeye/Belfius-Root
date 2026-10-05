package be.belfius.directmobile.android.root;

import static de.robv.android.xposed.XposedBridge.log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** @noinspection unused*/
public class BelfiusRoot implements IXposedHookLoadPackage {
    private static final String BELFIUS_PKG = "be.belfius.directmobile.android";
    private static final Boolean HAS_ROOT = false;

    private static final Class<?>[] NO_ARGS = new Class<?>[0];
    private static final Class<?>[] OBJECT_ARRAY_ARGS = new Class<?>[] { Object[].class };

    private static final String[] CLASS_NAMES = new String[] {
            "o.bsN",
            "o.bsS",
            "be.belfius.android.widget.security.security.utils.RootUtils$isDeviceRooted$2",
            "be.belfius.android.security.utils.RootUtils$isDeviceRooted$2"
    };

    private static final String[] METHOD_NAMES = new String[] {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };

    /**
     * Exact detection methods of the known obfuscated classes, verified against the .dex files.
     * The forced result is the value an honest, non-rooted, non-emulator, non-debugged device returns.
     */
    private static final Target[] DETECTION_TARGETS = new Target[] {
            // Belfius v26.1.0
            new Target("o.bPE", "INotificationSideChannelStub", NO_ARGS, HAS_ROOT),
            new Target("o.bPE", "cancel", NO_ARGS, HAS_ROOT),
            new Target("o.bPE", "cancelAll", NO_ARGS, HAS_ROOT),
            new Target("o.bPE", "cancel", OBJECT_ARRAY_ARGS, 0),

            // Belfius v26.3.0
            new Target("o.ckg", "IconCompatParcelizer", NO_ARGS, HAS_ROOT),
            new Target("o.ckg", "RemoteActionCompatParcelizer", NO_ARGS, HAS_ROOT),
            new Target("o.ckg", "read", NO_ARGS, HAS_ROOT),
            new Target("o.ckg", "AudioAttributesCompatParcelizer", NO_ARGS, 0)
    };

    /**
     * Platform level hooks, these method names are fixed by Android itself and therefore survive
     * any renaming the Belfius app applies to its own detection code.
     */
    private static final Target[] PLATFORM_TARGETS = new Target[] {
            new Target("android.os.Debug", "isDebuggerConnected", NO_ARGS, HAS_ROOT),
            new Target("android.os.Debug", "waitingForDebugger", NO_ARGS, HAS_ROOT)
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!BELFIUS_PKG.equals(loadPackageParam.packageName)) return;

        ClassLoader classLoader = loadPackageParam.classLoader;

        for (Target target : PLATFORM_TARGETS) {
            hook(classLoader, target);
        }

        int detectionHooks = 0;
        for (Target target : DETECTION_TARGETS) {
            if (hook(classLoader, target)) detectionHooks++;
        }

        // The brute force is only a fallback for the app versions predating the exact targets above,
        // running it on a newer version risks hooking an unrelated method that happens to match
        if (detectionHooks > 0) {
            log("Hooked " + detectionHooks + " known detection method(s), skipping fallback scan!");
            return;
        }

        // Loop through all possible class + method names, to attempt to find the method to hook
        for (String className : CLASS_NAMES) {
            for (String methodName : METHOD_NAMES) {
                try {
                    // Initialize method name string helper for logging
                    String methodNameStr = className + "." + methodName + "()";

                    // Attempt to hook the method
                    XposedHelpers.findAndHookMethod(
                            className,
                            classLoader,
                            methodName,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) {
                                    param.setResult(HAS_ROOT);
                                }
                            });

                    // Log success + abort loops
                    log("Successfully hooked " + methodNameStr + "!");
                    return;

                } catch (Throwable ignored) {}
            }
        }

        log("Failed to hook, no viable method found...");
    }

    /**
     * Forces the return value in beforeHookedMethod instead of afterHookedMethod on purpose,
     * the debugger checks of v26.x guard their own body and throw when they detect a hook,
     * afterHookedMethod would never run on such a method.
     */
    private boolean hook(ClassLoader classLoader, Target target) {
        try {
            XposedHelpers.findAndHookMethod(
                    target.className,
                    classLoader,
                    target.methodName,
                    target.parameterTypes,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(target.result);
                        }
                    });

            log("Successfully hooked " + target + "!");
            return true;

        } catch (Throwable ignored) {
            log("Skipped " + target + ", not present in this version!");
            return false;
        }
    }

    private static final class Target {
        private final String className;
        private final String methodName;
        private final Class<?>[] parameterTypes;
        private final Object result;

        private Target(String className, String methodName, Class<?>[] parameterTypes, Object result) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.result = result;
        }

        @Override
        public String toString() {
            StringBuilder args = new StringBuilder("(");
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) args.append(", ");
                args.append(parameterTypes[i].getSimpleName());
            }
            return className + "." + methodName + args.append(")").toString();
        }
    }
}
