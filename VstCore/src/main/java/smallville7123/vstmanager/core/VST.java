package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

public class VST {
    static String TAG = "VST";
    String packageName;
    CharSequence label;
    Drawable icon;
    Drawable logo;
    Drawable banner;
    VstCore core;
    ApplicationInfo mApplicationInfo;
    Context applicationContext;
    ClassLoader classLoader;
    String callbackClassName;
    ArrayList<String> classFiles;
    ArrayList<String> callbacks;

    VST() {
        core = new VstCore();
    }

    VST(VstCore core) {
        this.core = core;
    }

    public String getPackageName() {
        return packageName;
    }

    public CharSequence getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Drawable getLogo() {
        return logo;
    }

    public Drawable getBanner() {
        return banner;
    }

    public boolean verify(Context context, PackageManager packageManager, ApplicationInfo mApplicationInfo) {
        applicationContext = core.createContextForPackage(context, mApplicationInfo);
        if (applicationContext == null) return false;
        packageName = mApplicationInfo.packageName;
        label = packageManager.getApplicationLabel(mApplicationInfo);
        icon = packageManager.getApplicationIcon(mApplicationInfo);
        logo = packageManager.getApplicationLogo(mApplicationInfo);
        banner = packageManager.getApplicationBanner(mApplicationInfo);
        classLoader = applicationContext.getClassLoader();
        callbackClassName = VstCallback.class.getName();
        classFiles = core.getClassFiles(classLoader);
        if (core.hasVstCallback(classFiles, callbackClassName)) {
            callbacks = core.getCallbacks(classLoader, classFiles, callbackClassName);
            for (String callback : callbacks) Log.d(TAG, "vst callback = [" + callback + "]");
            return !callbacks.isEmpty();
        }
        return false;
    }
}
