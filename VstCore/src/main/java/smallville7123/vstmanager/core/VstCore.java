package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;
import smallville7123.reflectionutils.ReflectionUtils;

public class VstCore {
    public String TAG = "VstCore";
    ReflectionUtils reflectionUtils = new ReflectionUtils();
    boolean debug = true;
    ArrayList<String> ommitedClassPrefixes = new ArrayList<>();
    ArrayList<String> ommitedPackagePrefixes = new ArrayList<>();
    ArrayList<Boolean> printedPackages = new ArrayList();

    public VstCore() {
        ommitedClassPrefixes.add("androidx");
        ommitedClassPrefixes.add("android");
        ommitedClassPrefixes.add("com.google.android.material");
        ommitedPackagePrefixes.add("android");
        ommitedPackagePrefixes.add("com.android");
        ommitedPackagePrefixes.add("com.google");
        ommitedPackagePrefixes.add("com.opengapps");

        // only print what we are skipping once
        int size = ommitedPackagePrefixes.size();
        for (int i1 = 0; i1 < size; i1++) printedPackages.add(false);
    }


    public static ArrayList<DexFile> findAllDexFiles(ClassLoader classLoader) {
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
                                    if (dexFile != null) dexFiles.add((DexFile) dexFile);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dexFiles;
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    Context createContextForPackage(Context context, ApplicationInfo applicationInfo) {
        if (context == null) {
            Log.d(TAG, "VstCore: createContextForPackage: null Context supplied");
            return null;
        }
        if (applicationInfo == null) {
            Log.d(TAG, "VstCore: createContextForPackage: null ApplicationInfo supplied");
            return null;
        }

        for (int i1 = 0; i1 < ommitedPackagePrefixes.size(); i1++) {
            String ommitedPackagePrefix = ommitedPackagePrefixes.get(i1);
            if (applicationInfo.packageName.startsWith(ommitedPackagePrefix)) {
                if (!printedPackages.get(i1)) {
                    if (debug) Log.d(TAG, "VstCore: createContextForPackage: skipping package prefix: " + ommitedPackagePrefix);
                    printedPackages.set(i1, true);
                }
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
            return null;
        }
        if (debug) {
            if (mContext == null) {
                Log.d(TAG, "VstCore: createContextForPackage: unknown error: could not create context for package: " + applicationInfo.packageName);
            } else {
                Log.d(TAG, "VstCore: createContextForPackage: successfully created context for package: " + applicationInfo.packageName);
            }
        }
        return mContext;
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
        for (int i = 0; i < dexListSize; i++) {
            Enumeration<String> entries = dexList.get(i).entries();
            mainLoop:
            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                for (int i1 = 0; i1 < size; i1++) {
                    String ommitedClassPrefix = ommitedClassPrefixes.get(i1);
                    if (className.startsWith(ommitedClassPrefix)) {
                        if (!printed.get(i1)) {
                            if (debug) Log.d(TAG, "VstCore: getClassFiles: skipping class prefix: " + ommitedClassPrefix);
                            printed.set(i1, true);
                        }
                        continue mainLoop;
                    }

                }
                list.add(className);
            }
        }
        list.sort((object1, object2) -> object1.compareTo(object2));
        return list;
    }

    boolean hasVstCallback(ArrayList<String> classFiles, CharSequence callbackClassName) {
        if (debug) Log.d(TAG, "VstCore: hasVstCallback: searching for [" + callbackClassName + "]");
        for (String className : classFiles) {
            if (className.contentEquals(callbackClassName)) {
                if (debug) Log.d(TAG, "VstCore: hasVstCallback: found [" + callbackClassName + "]");
                return true;
            }
        }
        if (debug) Log.d(TAG, "VstCore: hasVstCallback: failed to find [" + callbackClassName + "]");
        return false;
    }

    ArrayList<String> getCallbacks(ClassLoader classLoader, ArrayList<String> classFiles, String callbackClassName) {
        ArrayList<String> callbacks = new ArrayList<>();
        for (String className : classFiles) {
            Class c = null;
            try {
                if (debug) Log.d(TAG, "VstCore: getCallbacks: loading class: " + className);
                c = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                if (debug) Log.d(TAG, "VstCore: getCallbacks: class could not be loaded: " + className);
                continue;
            }
            Class[] interfaces = c.getInterfaces();
            for (Class anInterface : interfaces) {
                if (anInterface.getName().contentEquals(callbackClassName)) {
                    if (debug) Log.d(TAG, "VstCore: getCallbacks: found callback: " + className);
                    callbacks.add(className);
                }
            }
        }
        return callbacks;
    }
}
