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
import smallville7123.vstmanager.core.VstCore;

import static android.content.pm.PackageManager.GET_META_DATA;

public class VstManager extends VstCore {
    public String TAG = Taggable.getTag(this);
    Context mContext;
    PackageManager mPackageManager;
    final List<ApplicationInfo> mInstalledApplications;
    FragmentActivity mOrigin;

    VstManager(FragmentActivity fragmentActivity) {
        mOrigin = fragmentActivity;
        mContext = mOrigin;
        mPackageManager = mContext.getPackageManager();
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

    public String[] findPackage() {
        return null;
    }

    public void onBackPressed() {
        mOrigin.getSupportFragmentManager().popBackStack();
    }

    @Nullable
    Toast currentToast;

    public boolean processObject(ObjectInfo packageObject) {
        if (verifyVST(mContext, mPackageManager, packageObject.mApplicationInfo)) {
            String text = "selected package: " + packageObject.mPackageName + " is valid";
            if (currentToast != null) currentToast.cancel();
            currentToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
            currentToast.show();
            return true;
        } else {
            String text = "selected package: " + packageObject.mPackageName + " is invalid";
            if (currentToast != null) currentToast.cancel();
            currentToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
            currentToast.show();
            return false;
        }
    }
}
