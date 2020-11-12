package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import smallville7123.vstmanager.core.Views.VstView;
import smallville7123.vstmanager.core.Views.WindowView;

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

    public void launchVst(Context context, String packageName, VST vst, VstView contentRoot) {
        for (Class callback : vst.callbacks) {
            if (ReflectionHelpers.classAextendsB(callback, ReflectionActivity.class)) {
                Log.d(TAG, "launchVst: callback [" + callback + "] extends ReflectionActivity");
                WindowView window = contentRoot.requestNewWindow();
                window.setTitle(vst.label);
                window.setIcon(vst.icon);
                ReflectionActivity reflectionActivity = new ReflectionActivity(
                        context, packageName, vst.applicationContext, callback, window
                );
                reflectionActivity.callOnCreate(null);
                VSTs.add(reflectionActivity);
            } else {
                Log.d(TAG, "launchVst: callback [" + callback + "] does not extend ReflectionActivity");
            }
        }
    }

    public boolean loadVST(Context context, String packageName, VST vst, VstView contentRoot) {
        launchVst(context, packageName, vst, contentRoot);
        return true;
    }

    public boolean loadVST(Context context, String packageName, VST vst) {
        launchVst(context, packageName, vst, contentRoot);
        return true;
    }

    VstView contentRoot = null;

    public void setContentRoot(VstView viewGroup) {
        contentRoot = viewGroup;
    }

    public void setWindows(PackageManager mPackageManager, ApplicationInfo mApplicationInfo) {
        if (contentRoot != null) {
            int childCount = contentRoot.getChildCount();
            if (childCount != 0) {
                for (int count = childCount; count > -1; count--) {
                    View child = contentRoot.getChildAt(count);
                    if (child instanceof WindowView) {
                        WindowView window = ((WindowView) child);
                        window.setTitle(mPackageManager.getApplicationLabel(mApplicationInfo));
                        window.setIcon(mPackageManager.getApplicationIcon(mApplicationInfo));
                    }
                }
            }
        }
    }
}
