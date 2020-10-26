package smallville7123.vstmanager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import smallville7123.taggable.Taggable;

import static android.content.pm.PackageManager.GET_META_DATA;

public class VstManager {
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
}
