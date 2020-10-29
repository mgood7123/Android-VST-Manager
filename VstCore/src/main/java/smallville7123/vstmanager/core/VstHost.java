package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.ViewGroup;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

import static smallville7123.vstmanager.core.ReflectionHelpers.newInstance;

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
                URL x = PathClassLoader.getSystemResource(vst.mApplicationInfo.publicSourceDir);
                //
                // ClassLoaderFactory creates the class loaders returned by Context.getClassLoader()
                // ClassLoaderFactory is @Hide
                // sources/android-30/com/android/internal/os/ClassLoaderFactory.java
                // return new PathClassLoader(dexPath, librarySearchPath, parent, arrayOfSharedLibraries);
                //
                // dexPath is zip
                //
                // zip is apk path
                //
                // zip, librarySearchPath, parent
                //
                //         /*
                //         * With all the combination done (if necessary, actually create the java class
                //         * loader and set up JIT profiling support if necessary.
                //         *
                //         * In many cases this is a single APK, so try to avoid the StringBuilder in TextUtils.
                //         */
                //        final String zip = (zipPaths.size() == 1) ? zipPaths.get(0) :
                //                TextUtils.join(File.pathSeparator, zipPaths);
                //
                // parent is BootClassLoader, this is the system classloader
                //
                ClassLoader z = new PathClassLoader(vst.mApplicationInfo.publicSourceDir, vst.mApplicationInfo.nativeLibraryDir, ClassLoader.getSystemClassLoader());
                try {
                    Log.d(TAG, "z.loadClass(EventThread.class.getName()) = [" + z.loadClass(EventThread.class.getName()) + "]");;
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "launchVst: invalid classloader", e);
                }
                // ResourcesManager itself is annotated by @Hide
                // mResourcesManager = ResourcesManager.getInstance()
                // mResources = {Resources@9969}
                //     mClassLoader = {PathClassLoader@9614} "dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/smallville7123.vstmanager.app-vxhTqf-N9zvzOiVSfEi3ig==/base.apk"],nativeLibraryDirectories=[/data/app/smallville7123.vstmanager.app-vxhTqf-N9zvzOiVSfEi3ig==/lib/arm64, /system/lib64, /system/product/lib64, /system/vendor/lib64]]]"
                ClassLoader xx = URLClassLoader.newInstance(new URL[]{x});
                ClassLoader classLoader = vst.applicationContext.getClassLoader();
//                ClassLoader classLoader = z;
                ReflectionActivity.ClassCaller.loadClass(classLoader, EventThread.class);
                ReflectionActivity.ClassCaller.loadClass(classLoader, String.class);
                ReflectionActivity.ClassCaller.loadClass(classLoader, Context.class);
                EventThread eventThread = new EventThread();
                Object instance = newInstance(callback);
                SingletonInterface singletonInterface = AbsoluteSingleton.getInstance(null);
                singletonInterface.setValue("hello");
                singletonInterface.setRET(new RET());
                Log.d(TAG, "singletonInterface.getValue() = [" + singletonInterface.getValue() + "]");
                Log.d(TAG, "singletonInterface.getRET() = [" + singletonInterface.getRET() + "]");
//        proxy = (SingletonInterface) Proxy.newProxyInstance(classLoader, new Class[]{SingletonInterface.class}, new PassThroughProxyHandler(this.instance));
//        proxy.setValue("hello");
                ReflectionHelpers.callInstanceMethod(instance, "setEventThread",
                        ReflectionActivity.ClassCaller.from(classLoader, String.class, packageName),
                        ReflectionActivity.ClassCaller.from(classLoader, Context.class, vst.applicationContext),
                        ReflectionActivity.ClassCaller.fromNewInstance(classLoader, EventThread.class)
                );
//                VSTs.add(new ReflectionActivity(packageName, vst.applicationContext, callback, contentRoot));
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
