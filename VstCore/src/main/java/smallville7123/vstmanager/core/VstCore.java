package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;

@SuppressWarnings("deprecation")
public class VstCore {
    public String TAG = "VstCore";
    boolean debug = true;
    ArrayList<String> ommitedClassPrefixes = new ArrayList<>();
    ArrayList<String> ommitedPackagePrefixes = new ArrayList<>();
    ArrayList<Boolean> printedPackages = new ArrayList();
    VstScanner scanner;
    int classTreeDepth = 0;

    public VstCore() {
        ommitedClassPrefixes.add("androidx"); // android
        ommitedClassPrefixes.add("android"); // android
        ommitedClassPrefixes.add("com.google.android.material"); // android
        ommitedPackagePrefixes.add("android"); // android
        ommitedPackagePrefixes.add("com.android"); // android
        ommitedPackagePrefixes.add("com.google"); // google apps
        ommitedPackagePrefixes.add("com.opengapps"); // google apps
        ommitedPackagePrefixes.add("com.samsung"); // samsung packages
        ommitedPackagePrefixes.add("com.sec.android"); // additional android packages
        ommitedPackagePrefixes.add("org.mozilla.firefox"); // Firefox
        ommitedPackagePrefixes.add("lynx.remix"); // Lynx Remix, a modded kik
        ommitedPackagePrefixes.add("org.telegram.messenger"); // Telegram
        ommitedPackagePrefixes.add("org.fdroid.fdroid"); // F-Droid
        ommitedPackagePrefixes.add("com.crunchyroll.crunchyroid"); // Crunchyroll
        ommitedPackagePrefixes.add("com.topjohnwu.magisk"); // Magisk Manager

        // only print what we are skipping once
        int size = ommitedPackagePrefixes.size();
        for (int i1 = 0; i1 < size; i1++) printedPackages.add(false);
    }

    public VstCore(VstScanner scanner) {
        this();
        this.scanner = scanner;
    }

    Context createContextForPackage(Context context, ApplicationInfo applicationInfo) {
        if (context == null) {
            if (debug) Log.d(TAG, "VstCore: createContextForPackage: null Context supplied");
            scanner.runOnUiThread.run(() -> scanner.onPackageSkipped.run(++scanner.skipped));
            return null;
        }
        if (applicationInfo == null) {
            if (debug) Log.d(TAG, "VstCore: createContextForPackage: null ApplicationInfo supplied");
            scanner.runOnUiThread.run(() -> scanner.onPackageSkipped.run(++scanner.skipped));
            return null;
        }

        // reset counts on new package context
        classTreeDepth = 0;
        scanner.runOnUiThread.run(() -> scanner.resetPackageStats());
        for (int i1 = 0; i1 < ommitedPackagePrefixes.size(); i1++) {
            String ommitedPackagePrefix = ommitedPackagePrefixes.get(i1);
            if (applicationInfo.packageName.startsWith(ommitedPackagePrefix)) {
                if (!printedPackages.get(i1)) {
                    if (debug) Log.d(TAG, "VstCore: createContextForPackage: skipping package prefix: " + ommitedPackagePrefix);
                    printedPackages.set(i1, true);
                }
                scanner.runOnUiThread.run(() -> scanner.onPackageSkipped.run(++scanner.skipped));
                return null;
            }
        }

        if (debug) Log.d(TAG, "VstCore: createContextForPackage: creating context for package: " + applicationInfo.packageName);
        Context mContext;
        try {
            mContext = context.createPackageContext(
                    applicationInfo.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException e) {
            if (debug) Log.d(TAG, "VstCore: createContextForPackage: could not create context: package not found: " + applicationInfo.packageName);
            scanner.runOnUiThread.run(() -> scanner.onPackageSkipped.run(++scanner.skipped));
            return null;
        }
        if (debug) {
            if (mContext == null) {
                if (debug) Log.d(TAG, "VstCore: createContextForPackage: unknown error: could not create context for package: " + applicationInfo.packageName);
                scanner.runOnUiThread.run(() -> scanner.onPackageSkipped.run(++scanner.skipped));
            } else {
                if (debug) Log.d(TAG, "VstCore: createContextForPackage: successfully created context for package: " + applicationInfo.packageName);
            }
        }
        return mContext;
    }

    public ArrayList<DexFile> findAllDexFiles(ClassLoader classLoader) {
        int count = 0;
        int nullCount = 0;
        ArrayList<DexFile> dexFiles = new ArrayList<>();
        try {
            Field pathListField = findField(classLoader, "pathList");
            if (pathListField != null) {
                Object pathList = pathListField.get(classLoader);
                if (pathList != null) {
                    Field dexElementsField = findField(pathList, "dexElements");
                    if (dexElementsField != null) {
                        Object[] dexElements = (Object[]) dexElementsField.get(pathList);
                        if (dexElements.length != 0) {
                            Field dexFileField = findField(dexElements[0], "dexFile");
                            if (dexElementsField != null) {
                                for (Object dexElement : dexElements) {
                                    Object dexFile = dexFileField.get(dexElement);
                                    // dexFile can be null
                                    if (dexFile != null) {
                                        int finalCount = ++count;
                                        scanner.runOnUiThread.run(() -> scanner.onDexFileFound.run(finalCount));
                                        dexFiles.add((DexFile) dexFile);
                                    } else {
                                        int finalCount = ++nullCount;
                                        scanner.runOnUiThread.run(() -> scanner.onEmptyDexFileFound.run(finalCount));
                                        try {
                                            Thread.sleep(0, 1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                int finalCount = ++nullCount;
                                scanner.runOnUiThread.run(() -> scanner.onEmptyDexFileFound.run(finalCount));
                                try {
                                    Thread.sleep(0, 1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            int finalCount = ++nullCount;
                            scanner.runOnUiThread.run(() -> scanner.onEmptyDexFileFound.run(finalCount));
                            try {
                                Thread.sleep(0, 1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        int finalCount = ++nullCount;
                        scanner.runOnUiThread.run(() -> scanner.onEmptyDexFileFound.run(finalCount));
                        try {
                            Thread.sleep(0, 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    int finalCount = ++nullCount;
                    scanner.runOnUiThread.run(() -> scanner.onEmptyDexFileFound.run(finalCount));
                    try {
                        Thread.sleep(0, 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                int finalCount = ++nullCount;
                scanner.runOnUiThread.run(() -> scanner.onEmptyDexFileFound.run(finalCount));
                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dexFiles;
    }

    private Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException var4) {
                scanner.runOnUiThread.run(() -> scanner.onClassTreeDepth.run(++classTreeDepth));
                clazz = clazz.getSuperclass();
                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }


    ArrayList<String> getClassFiles(ClassLoader classLoader) {

        ArrayList<DexFile> dexList = findAllDexFiles(classLoader);
        int dexListSize = dexList.size();

        // only print what we are skipping once
        ArrayList<Boolean> printed = new ArrayList();
        int size = ommitedClassPrefixes.size();
        for (int i1 = 0; i1 < size; i1++) printed.add(false);

        // the dex size can be greater than 1 for certain applications such as Google Chrome
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> ommited = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < dexListSize; i++) {
            Enumeration<String> entries = dexList.get(i).entries();
            mainLoop:
            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                int finalCount = ++count;
                scanner.runOnUiThread.run(() -> scanner.onDexClassFound.run(finalCount));
                boolean shouldAdd = true;
                for (int i1 = 0; i1 < size; i1++) {
                    String ommitedClassPrefix = ommitedClassPrefixes.get(i1);
                    if (className.startsWith(ommitedClassPrefix)) {
                        if (!printed.get(i1)) {
                            if (debug) Log.d(TAG, "VstCore: getClassFiles: skipping class prefix: " + ommitedClassPrefix);
                            printed.set(i1, true);
                        }
                        ommited.add(className);
                        shouldAdd = false;
                        break;
                    }
                }
                if (shouldAdd) list.add(className);
                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        int finalSize = ommited.size();
        scanner.runOnUiThread.run(() -> scanner.onClassSkippedSetMax.run(finalSize));
        for (int i = 0; i < finalSize; i++) {
            String className = ommited.get(i);
            int finalI = i+1;
            scanner.runOnUiThread.run(() -> scanner.onClassSkipped.run(finalI, className, finalSize));
            try {
                Thread.sleep(0, 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        list.sort(String::compareTo);
        return list;
    }

    boolean hasVstCallback(ArrayList<String> classFiles, CharSequence callbackClassName) {
        if (debug) Log.d(TAG, "VstCore: hasVstCallback: searching for [" + callbackClassName + "]");
        int size = classFiles.size();
        scanner.runOnUiThread.run(() -> scanner.onClassQuickScannedSetMax.run(size));
        scanner.runOnUiThread.run(() -> scanner.onClassFullyScannedSetMax.run(size));
        for (int i = 0; i < size; i++) {
            String className = classFiles.get(i);
            if (className.contentEquals("smallville7123.vstmanager.core.VstActivity")) {
                if (debug) Log.d(TAG, "VstCore: hasVstCallback: skipping internal callback: [" + callbackClassName + "]");
                continue;
            }
            if (className.contentEquals("smallville7123.vstmanager.core.ReflectionActivity")) {
                if (debug) Log.d(TAG, "VstCore: hasVstCallback: skipping internal callback: [" + callbackClassName + "]");
                continue;
            }
            if (className.contentEquals(callbackClassName)) {
                if (debug) Log.d(TAG, "VstCore: hasVstCallback: found [" + callbackClassName + "]");
                return true;
            }
            int finalI = i+1;
            scanner.runOnUiThread.run(() -> scanner.onClassQuickScanned.run(finalI, className, size));
            try {
                Thread.sleep(0, 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (debug) Log.d(TAG, "VstCore: hasVstCallback: failed to find [" + callbackClassName + "]");
        return false;
    }

    ArrayList<Pair<Class, Integer>> getCallbacks(ClassLoader classLoader, ArrayList<String> classFiles, String callbackClassName) {
        ArrayList<Pair<Class, Integer>> callbacks = new ArrayList<>();
        int size = classFiles.size();
        scanner.runOnUiThread.run(() -> scanner.onClassFullyScannedSetMax.run(size));
        for (int i = 0; i < size; i++) {
            String className = classFiles.get(i);
            if (className.contentEquals("smallville7123.vstmanager.core.VstActivity")) {
                if (debug) Log.d(TAG, "VstCore: hasVstCallback: skipping internal callback: [" + callbackClassName + "]");
                continue;
            }
            if (className.contentEquals("smallville7123.vstmanager.core.ReflectionActivity")) {
                if (debug) Log.d(TAG, "VstCore: hasVstCallback: skipping internal callback: [" + callbackClassName + "]");
                continue;
            }
            Class c = null;
            try {
                if (debug) Log.d(TAG, "VstCore: getCallbacks: loading class: " + className);
                c = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                if (debug) Log.d(TAG, "VstCore: getCallbacks: class could not be loaded: " + className);
                continue;
            }
            if (ReflectionHelpers.containsInterface(c, callbackClassName)) {
                if (debug) Log.d(TAG, "VstCore: getCallbacks: found callback: " + className);
                callbacks.add(new Pair<>(c, i));
                scanner.runOnUiThread.run(() -> scanner.onVstFound.run(++scanner.vstCount, className, -1));
            }
            int finalI = i+1;
            scanner.runOnUiThread.run(() -> scanner.onClassFullyScanned.run(finalI, className, size));
        }
        return callbacks;
    }
}
