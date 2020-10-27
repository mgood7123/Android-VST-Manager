package smallville7123.vstmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import smallville7123.taggable.Taggable;
import smallville7123.vstmanager.core.VST;
import smallville7123.vstmanager.core.VstCore;
import smallville7123.vstmanager.core.VstHost;

import static android.content.pm.PackageManager.GET_META_DATA;
import static smallville7123.taggable.Taggable.getTag;

public class VstManager {
    public static String TAG = "VstManager";
    Context mContext;
    PackageManager mPackageManager;
    final List<ApplicationInfo> mInstalledApplications;
    FragmentActivity mOrigin;
    // host contains an internal list of valid vst's
    VstHost mVstHost = new VstHost();
    @Nullable Toast mToast;


    VstManager(FragmentActivity fragmentActivity) {
        mOrigin = fragmentActivity;
        mContext = mOrigin;
        mPackageManager = mContext.getPackageManager();
        mVstHost.vstScanner.setRunOnUiThread(runnable -> mOrigin.runOnUiThread(runnable));

        //
        // As of Android 11, this method no longer returns information about all apps;
        // see https://g.co/dev/packagevisibility for details
        //
        mInstalledApplications = mPackageManager.getInstalledApplications(GET_META_DATA);
        mInstalledApplications.sort((object1, object2) -> object1.packageName.compareTo(object2.packageName));
    }

    public void showList() {
        PackageViewerFragment packageViewerFragment = new PackageViewerFragment(mOrigin, this);
        mOrigin.getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, packageViewerFragment)
                .addToBackStack("tag")
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();
    }

    public void onBackPressed() {
        mOrigin.getSupportFragmentManager().popBackStack();
    }

    public boolean load(VST selected) {
        return mVstHost.loadVST(selected);
    }
}
