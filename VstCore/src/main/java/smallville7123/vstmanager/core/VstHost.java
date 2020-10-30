package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.ViewGroup;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

public class VstHost {
    public String TAG = "VstHost";
    public final VstScanner vstScanner = new VstScanner();

    ArrayList<ReflectionActivity> VSTs = new ArrayList<>();

    public VST getVST(ApplicationInfo applicationInfo) {
        return vstScanner.getVST(applicationInfo);
    }

    public ArrayList<VST> getVstList() {
        return vstScanner.vstList;
    }

    public void scan(Context context, PackageManager packageManager, List<ApplicationInfo> mInstalledApplications) {
        vstScanner.scan(context, packageManager, mInstalledApplications);
    }

    public void launchVst(Context context, String packageName, VST vst, ViewGroup contentRoot) {
        for (Class callback : vst.callbacks) {
            if (ReflectionHelpers.classAextendsB(callback, ReflectionActivity.class)) {
                Log.d(TAG, "launchVst: callback [" + callback + "] extends ReflectionActivity");
                URL x = PathClassLoader.getSystemResource(vst.mApplicationInfo.publicSourceDir);
                VSTs.add(new ReflectionActivity(context, packageName, vst, callback, contentRoot));
            } else {
                Log.d(TAG, "launchVst: callback [" + callback + "] does not extend ReflectionActivity");
            }
        }
    }

    public boolean loadVST(Context context, String packageName, VST vst, ViewGroup contentRoot) {
        launchVst(context, packageName, vst, contentRoot);
        return true;
    }

    public boolean loadVST(Context context, String packageName, VST vst) {
        launchVst(context, packageName, vst, contentRoot);
        return true;
    }

    ViewGroup contentRoot = null;

    public void setContentRoot(ViewGroup viewGroup) {
        contentRoot = viewGroup;
    }
}
