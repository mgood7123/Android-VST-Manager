package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;

public class VstHost {
    ArrayList<VST> vstList = new ArrayList<>();

    VstCore core = new VstCore();

    public VST verifyVST(Context context, PackageManager packageManager, ApplicationInfo applicationInfo) {
        VST vst = new VST(core);
        if (vst.verify(context, packageManager, applicationInfo)) {
            vstList.add(vst);
            return vst;
        }
        return null;
    }

    public VST getVST(ApplicationInfo applicationInfo) {
        for (VST vst : vstList) {
            if (vst.mApplicationInfo == applicationInfo) return vst;
        }
        return null;
    }

    public ArrayList<VST> getVstList() {
        return vstList;
    }
}
