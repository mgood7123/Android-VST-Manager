package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

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

    public void launchVst(String packageName, VST vst, ViewGroup contentRoot) {
        for (Class callback : vst.callbacks) {
            if (ReflectionHelpers.classAextendsB(callback, ReflectionActivity.class)) {
                Log.d(TAG, "launchVst: callback [" + callback + "] extends ReflectionActivity");
                VSTs.add(new ReflectionActivity(packageName, vst.applicationContext, callback, contentRoot));
            } else {
                Log.d(TAG, "launchVst: callback [" + callback + "] does not extend ReflectionActivity");
            }
        }
    }

    public boolean loadVST(String packageName, VST vst, ViewGroup contentRoot) {
        launchVst(packageName, vst, contentRoot);
        return true;
    }

    public boolean loadVST(String packageName, VST vst) {
        launchVst(packageName, vst, contentRoot);
        return true;
    }

    ViewGroup contentRoot = null;

    public void setContentRoot(ViewGroup viewGroup) {
        contentRoot = viewGroup;
    }
}
