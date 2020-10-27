package smallville7123.vstmanager.core;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

public class VstScanner {
    public interface PackageBeingScannedRunnable {
        void run(String packageName);
    }

    public interface PackageScanRunnable {
        void run(int progress, ApplicationInfo applicationInfo, int max);
    }

    public interface ClassScanRunnable {
        void run(int progress, String ClassName, int max);
    }

    public interface RunOnUiThreadRunnable {
        void run(Runnable runnable);
    }

    public interface SetMaxRunnable {
        void run(int max);
    }

    RunOnUiThreadRunnable runOnUiThread = runnable -> {};

    public void setRunOnUiThread(RunOnUiThreadRunnable runOnUiThreadRunnable) {
        runOnUiThread = runOnUiThreadRunnable;
    }


    VstCore core = new VstCore(this);
    ArrayList<VST> vstList = new ArrayList<>();
    Runnable onScanStarted = () -> {};
    Runnable onScanComplete = () -> {};

    PackageBeingScannedRunnable onPackageBeingScanned = (name) -> {};

    PackageScanRunnable onPackageScanned = (progress, applicationInfo, max) -> {};
    SetMaxRunnable onPackageScannedSetMax = max -> {};
    PackageScanRunnable onPackageSkipped = (progress, applicationInfo, max) -> {};

    SetMaxRunnable onClassTreeDepth = count -> {};
    SetMaxRunnable onDexFileFound = count -> {};
    SetMaxRunnable onEmptyDexFileFound = count -> {};
    SetMaxRunnable onDexClassFound = count -> {};

    ClassScanRunnable onClassSkipped = (progress, className, max) -> {};
    SetMaxRunnable onClassSkippedSetMax = max -> {};

    ClassScanRunnable onClassQuickScanned = (progress, className, max) -> {};
    SetMaxRunnable onClassQuickScannedSetMax = max -> {};

    ClassScanRunnable onClassFullyScanned = (progress, className, max) -> {};
    SetMaxRunnable onClassFullyScannedSetMax = max -> {};

    ClassScanRunnable onVstFound = (progress, applicationInfo, max) -> {};

    public void setOnScanStarted(Runnable onScanStarted) {
        this.onScanStarted = onScanStarted;
    }

    public void setOnScanComplete(Runnable onScanComplete) {
        this.onScanComplete = onScanComplete;
    }

    public void setOnClassTreeDepth(SetMaxRunnable onClassTreeDepth) {
        this.onClassTreeDepth = onClassTreeDepth;
    }

    public void setOnPackageBeingScanned(PackageBeingScannedRunnable onPackageBeingScanned) {
        this.onPackageBeingScanned = onPackageBeingScanned;
    }

    public void setOnPackageScanned(PackageScanRunnable onPackageScanned) {
        this.onPackageScanned = onPackageScanned;
    }

    public void setOnPackageScannedSetMax(SetMaxRunnable onPackageScannedSetMax) {
        this.onPackageScannedSetMax = onPackageScannedSetMax;
    }

    public void setOnPackageSkipped(PackageScanRunnable onPackageSkipped) {
        this.onPackageSkipped = onPackageSkipped;
    }

    public void setOnDexFileFound(SetMaxRunnable onDexFileFound) {
        this.onDexFileFound = onDexFileFound;
    }

    public void setOnEmptyDexFileFound(SetMaxRunnable onEmptyDexFileFound) {
        this.onEmptyDexFileFound = onEmptyDexFileFound;
    }

    public void setOnDexClassFound(SetMaxRunnable onDexClassFound) {
        this.onDexClassFound = onDexClassFound;
    }

    public void setOnClassQuickScanned(ClassScanRunnable onClassQuickScanned) {
        this.onClassQuickScanned = onClassQuickScanned;
    }

    public void setOnClassQuickScannedSetMax(SetMaxRunnable onClassQuickScannedSetMax) {
        this.onClassQuickScannedSetMax = onClassQuickScannedSetMax;
    }

    public void setOnClassFullyScanned(ClassScanRunnable onClassFullyScanned) {
        this.onClassFullyScanned = onClassFullyScanned;
    }

    public void setOnClassFullyScannedSetMax(SetMaxRunnable onClassFullyScannedSetMax) {
        this.onClassFullyScannedSetMax = onClassFullyScannedSetMax;
    }

    public void setOnClassSkipped(ClassScanRunnable onClassSkipped) {
        this.onClassSkipped = onClassSkipped;
    }

    public void setOnClassSkippedSetMax(SetMaxRunnable onClassSkippedSetMax) {
        this.onClassSkippedSetMax = onClassSkippedSetMax;
    }

    public void setOnVstFound(ClassScanRunnable onVstFound) {
        this.onVstFound = onVstFound;
    }

    public VST verifyVST(Context context, PackageManager packageManager, ApplicationInfo applicationInfo) {
        VST vst = new VST(core);
        vst.scanner = this;
        if (vst.verify(context, packageManager, applicationInfo)) {
            vstList.add(vst);
            return vst;
        }
        return null;
    }

    final Object scanLock = new Object();
    boolean isScanning = false;
    int vstCount = 0;

    public void scan(Context context, PackageManager packageManager, List<ApplicationInfo> mInstalledApplications) {
        if (isScanning) {
            throw new RuntimeException("cannot scan twice");
        }
        int size = mInstalledApplications.size();
        onPackageScannedSetMax.run(size);
        new Thread(() -> {
            synchronized (scanLock) {
                isScanning = true;
                vstCount = 0;
                runOnUiThread.run(() -> onVstFound.run(vstCount, null, -1));
                runOnUiThread.run(() -> onScanStarted.run());
                for (int i = 0; i < size; i++) {
                    ApplicationInfo applicationInfo = mInstalledApplications.get(i);
                    int finalI = i;
                    runOnUiThread.run(() -> onPackageBeingScanned.run(applicationInfo.packageName));
                    verifyVST(context, packageManager, applicationInfo);
                    runOnUiThread.run(() -> onPackageScanned.run(finalI, applicationInfo, size));
                    try {
                        Thread.sleep(0, 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread.run(() -> onPackageScanned.run(size, null, size));
                runOnUiThread.run(() -> onScanComplete.run());
                isScanning = false;
            }
        }).start();
    }

    public VST getVST(ApplicationInfo applicationInfo) {
        for (VST vst : vstList) {
            if (vst.mApplicationInfo == applicationInfo) return vst;
        }
        return null;
    }
}
