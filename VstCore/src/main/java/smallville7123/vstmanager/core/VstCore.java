package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import smallville7123.reflectionutils.ReflectionUtils;
import smallville7123.taggable.Taggable;

public class VstCore {
    public String TAG = Taggable.getTag(this);
    ReflectionUtils reflectionUtils = new ReflectionUtils();
    public boolean verifyVST(Context context, PackageManager mPackageManager, ApplicationInfo mApplicationInfo) {
        Context mContext = null;
        try {
            mContext = context.createPackageContext(
                    mApplicationInfo.packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (mContext == null) {
            Log.e(TAG, "verifyVST: package context is null");
        } else {
            ArrayList<DexFile> dexList = MultiDexHelper.findAllDexFiles(mContext.getClassLoader());
            Log.d(TAG, "dexList.size() = [" + dexList.size() + "]");;
            for (int i = 0; i < dexList.size(); i++) {
                Log.d(TAG, "listing dex " + i + "");
                Enumeration<String> entries = dexList.get(i).entries();
                while(entries.hasMoreElements()) {
                    String className = entries.nextElement();
                    if (className.startsWith(mApplicationInfo.packageName))
                    Log.d(TAG, "entries.nextElement() = [" + className + "]");
                }
            }
        }
        return false;
    }
}
