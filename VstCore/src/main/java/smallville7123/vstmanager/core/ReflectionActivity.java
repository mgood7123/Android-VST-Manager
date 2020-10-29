package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;

import java.util.ArrayList;
import java.util.Collections;

import static smallville7123.vstmanager.core.ReflectionHelpers.newInstance;

public class ReflectionActivity extends ContextThemeWrapper {
    private static final String TAG = "ReflectionActivity";
    private Object instance = null;

    public ReflectionActivityController mController = null;

    public EventThread eventThread;

    HandlerThread handlerThread = null;
    Handler mHandler = null;
    Handler UiThread = null;

    ViewGroup mContentRoot = null;
    LayoutInflater layoutInflater = null;
    Context hostContext = null;
    ClassLoader hostClassLoader = null;

    static class ClassCaller {

        static ArrayList<Pair<ClassLoader, ArrayList<Class>>> classLoaderList = new ArrayList<>();

        static public void loadClass(ClassLoader classLoader, Class<? extends Object> clazz) {
            Pair<ClassLoader, ArrayList<Class>> pair = getClassList(classLoader);
            if (pair != null) {
                if (pair.second != null) {
                    if (getClass(pair, clazz) == null) {
                        try {
                            pair.second.add(classLoader.loadClass(clazz.getName()));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } else {
                try {
                    classLoaderList.add(new Pair<>(classLoader, new ArrayList<>(Collections.singleton(classLoader.loadClass(clazz.getName())))));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private static Class<?> getClass(ClassLoader loader, Class<?> clazz) {
            Pair<ClassLoader, ArrayList<Class>> pair = getClassList(loader);
            if (pair == null) return null;
            for (Class aClass : pair.second) {
                if (aClass.getName().contentEquals(clazz.getName())) return aClass;
            }
            return null;
        }

        private static Class<?> getClass(Pair<ClassLoader, ArrayList<Class>> pair, Class<?> clazz) {
            for (Class aClass : pair.second) {
                if (aClass.getName().contentEquals(clazz.getName())) return aClass;
            }
            return null;
        }

        private static Pair<ClassLoader, ArrayList<Class>> getClassList(ClassLoader classLoader) {
            for (Pair<ClassLoader, ArrayList<Class>> pair : classLoaderList) {
                if (pair.first.equals(classLoader)) return pair;
            }
            return null;
        }

        public static ReflectionHelpers.ClassParameter fromNull(ClassLoader classLoader, Class<?> clazz) {
            Class c = ClassCaller.getClass(classLoader, clazz);
            if (c == null) return null;
            return ReflectionHelpers.ClassParameter.from(c, null);
        }

        public static ReflectionHelpers.ClassParameter fromNewInstance(ClassLoader classLoader, Class<?> clazz) {
            Class c = ClassCaller.getClass(classLoader, clazz);
            if (c == null) return null;
            return ReflectionHelpers.ClassParameter.from(c, newInstance(c));
        }

        @SuppressWarnings("unchecked")
        public static <V> ReflectionHelpers.ClassParameter<V> from(ClassLoader classLoader, Class<? extends V> clazz, V val) {
            Class c = ClassCaller.getClass(classLoader, clazz);
            if (c == null) return null;
            return ReflectionHelpers.ClassParameter.from(c, val);
        }
    }

    public void setEventThread(String hostPackageName, Context context, EventThread eventThread) {
        Log.d(TAG, "setEventThread() called with: hostPackageName = [" + hostPackageName + "], eventThread = [" + eventThread + "]");
        this.eventThread = eventThread;
        if (getBaseContext() == null) attachBaseContext(context);
        try {
            hostContext = context.createPackageContext(
                    hostPackageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        singletonInterface = AbsoluteSingleton.getInstance(hostContext.getClassLoader());
        Log.d(TAG, "singletonInterface.getRET() = [" + singletonInterface.getRET() + "]");
//        Log.d(TAG, "host = [" + host + "]");
//        Log.d(TAG, "instance = [" + instance + "]");
//        Log.d(TAG, "host.instance = [" + host.instance + "]");
        Log.d(TAG, "singletonInterface.getValue() = [" + singletonInterface.getValue() + "]");
        Log.d(TAG, "hostContext = [" + hostContext + "]");
        Log.d(TAG, "context = [" + context + "]");
        Log.d(TAG, "eventThread = [" + eventThread + "]");
        Log.d(TAG, "this = [" + this + "]");
    }

    public ReflectionActivity() {
        Log.d(TAG, "ReflectionActivity() called");
    }

    SingletonInterface singletonInterface;

    public ReflectionActivity(String packageName, Context applicationContext, Class callback, ViewGroup contentRoot) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        ClassCaller.loadClass(classLoader, EventThread.class);
        ClassCaller.loadClass(classLoader, String.class);
        ClassCaller.loadClass(classLoader, Context.class);
        eventThread = new EventThread();
        instance = newInstance(callback);
        singletonInterface = AbsoluteSingleton.getInstance(null);
        singletonInterface.setValue("hello");
        singletonInterface.setRET(new RET());
        Log.d(TAG, "singletonInterface.getValue() = [" + singletonInterface.getValue() + "]");
        Log.d(TAG, "singletonInterface.getRET() = [" + singletonInterface.getRET() + "]");
//        proxy = (SingletonInterface) Proxy.newProxyInstance(classLoader, new Class[]{SingletonInterface.class}, new PassThroughProxyHandler(this.instance));
//        proxy.setValue("hello");
        ReflectionHelpers.callInstanceMethod(instance, "setEventThread",
                ClassCaller.from(classLoader, String.class, packageName),
                ClassCaller.from(classLoader, Context.class, applicationContext),
                ClassCaller.fromNewInstance(classLoader, EventThread.class)
        );
//        ReflectionHelpers.traverseClass(instance.getClass(), new ReflectionHelpers.TraversalRunnable() {
//            @Override
//            boolean run(Object argument) {
//                Class C = ((Class)argument);
//                try {
//                    Method m = C.getMethod("setEventThread", EventThread.class);
//                    m.invoke(instance, eventThread);
//                    return true;
//                } catch (ReflectiveOperationException e) {
//                    return false;
//                }
//            }
//        });
//        UiThread = new Handler(Looper.getMainLooper());
//        handlerThread = new HandlerThread("HandlerThreadName");
//        handlerThread.start();
//        mHandler = new Handler(handlerThread.getLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                Log.d(TAG, "handle msg = [" + msg + "]");
//            }
//
//            @Override
//            public void dispatchMessage(@NonNull Message msg) {
//                super.dispatchMessage(msg);
//                Log.d(TAG, "dispatch msg = [" + msg + "]");
//            }
//        };
//        init(this, applicationContext, callback, contentRoot);
//        mController.onCreate(null);
    }

    private void init(ReflectionActivity reflectionActivity, Context applicationContext, Class callback, ViewGroup contentRoot) {
        mController = new ReflectionActivityController(reflectionActivity, callback);
        mController.setUiThread(reflectionActivity.UiThread);
        mController.setContentRoot(contentRoot);
        mController.attachBaseContext(applicationContext);
        mController.setLayoutInflater(LayoutInflater.from(applicationContext));
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
    }

    protected void setContentView(ViewGroup contentRoot) {
        runOnUIThread(() -> {
            mContentRoot.removeViewAt(0);
            mContentRoot.addView(contentRoot);
        });
    }

    protected void setContentView(@LayoutRes int res) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                layoutInflater.inflate(res, mContentRoot, true);
            }
        });
    }

    final public void runOnUIThread(Runnable r) {
        UiThread.post(r);
    }
}