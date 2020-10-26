package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by xudshen@hotmail.com on 14/11/13.
 */
public class MultiDexHelper {
    private static final String TAG = "MultiDexHelper";

    public static ArrayList<DexFile> findAllDexFiles(ClassLoader classLoader) {
        ArrayList<DexFile> dexFiles = new ArrayList<>();
        try {
            Field pathListField = findField(classLoader, "pathList");
            Object pathList = pathListField.get(classLoader);
            Field dexElementsField = findField(pathList, "dexElements");
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);
            Field dexFileField = findField(dexElements[0], "dexFile");

            for (Object dexElement : dexElements) {
                Object dexFile = dexFileField.get(dexElement);
                dexFiles.add((DexFile) dexFile);
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
}