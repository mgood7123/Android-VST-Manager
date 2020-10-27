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

public class VstManager {
    public String TAG = Taggable.getTag(this);
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

        //
        // As of Android 11, this method no longer returns information about all apps;
        // see https://g.co/dev/packagevisibility for details
        //
        mInstalledApplications = mPackageManager.getInstalledApplications(GET_META_DATA);

        mInstalledApplications.sort((object1, object2) -> object1.packageName.compareTo(object2.packageName));
        for (ApplicationInfo applicationInfo : mInstalledApplications)
            mVstHost.verifyVST(mContext, mPackageManager, applicationInfo);
    }

    public void showList() {
        PackageViewerFragment packageViewerFragment = new PackageViewerFragment(mOrigin, this);
        mOrigin.getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, packageViewerFragment)
                .addToBackStack("tag")
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();
    }

    public String[] findPackage() {
        return null;
    }

    public void onBackPressed() {
        mOrigin.getSupportFragmentManager().popBackStack();
    }


    public boolean shouldAdd() {
        return true;
    }

    public boolean processObject(ObjectInfo packageObject) {
        VST vst = mVstHost.verifyVST(mContext, mPackageManager, packageObject.mApplicationInfo);
        String valid = vst != null ? "valid" : "invalid";
        String text = "selected package: " + packageObject.mPackageName + " is " + valid;
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
        mToast.show();
        return vst != null;
    }
}
