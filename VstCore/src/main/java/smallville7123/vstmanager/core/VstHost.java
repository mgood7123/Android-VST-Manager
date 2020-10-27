package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class VstHost {
    public final VstScanner vstScanner = new VstScanner();

    public VST getVST(ApplicationInfo applicationInfo) {
        return vstScanner.getVST(applicationInfo);
    }

    public ArrayList<VST> getVstList() {
        return vstScanner.vstList;
    }

    public void scan(Context context, PackageManager packageManager, List<ApplicationInfo> mInstalledApplications) {
        vstScanner.scan(context, packageManager, mInstalledApplications);
    }

    public boolean loadVST(VST vst) {
        return true;
    }
}
