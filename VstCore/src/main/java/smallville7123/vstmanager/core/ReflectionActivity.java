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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static smallville7123.vstmanager.core.ReflectionHelpers.ClassParameter.from;
import static smallville7123.vstmanager.core.ReflectionHelpers.newInstance;

public class ReflectionActivity extends ContextThemeWrapper {
    private static final String TAG = "ReflectionActivity";
    private Object instance = null;

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

    Pair<Object, Context> hostLink;
    ArrayList<Pair<Object, Context>> clients = new ArrayList<>();
    static String listName = "Clients";

    public ReflectionActivity(Context context, String packageName, VST vst, Class callback, ViewGroup contentRoot) {
        Object client = linkClientWithHost(callback, context, vst.applicationContext);
        Log.d(TAG, "client = [" + client + "]");
        ReflectionHelpers.setField(client, "mContentRoot", contentRoot);
        ReflectionHelpers.callInstanceMethod(client, "onCreate", from(Bundle.class, null));
    }

    private Object linkClientWithHost(Class clientClass, Context hostContext, Context clientContext) {
        Pair<Object, Context> host = new Pair<>(this, hostContext);
        Pair<Object, Context> client = new Pair<>(newInstance(clientClass), clientContext);
        ReflectionHelpers.callInstanceMethod(client.first, "linkHost",
                from(Pair.class, host),
                from(Pair.class, client)
        );
        return client.first;
    }

    public void linkClient(Pair<Object, Context> client) {
        clients.add(client);
        Log.d(TAG, "linkClient() called with: client = [" + client + "]");
        // host/client link has successfully been established
        Log.d(TAG, "linkClient: host/client link has successfully been established");
    }

    public void linkHost(Pair<Object, Context> host, Pair<Object, Context> client) {
        Log.d(TAG, "linkHost() called with: host = [" + host + "], client = [" + client + "]");
        hostLink = host;
        // now, establish a link to the host
        ReflectionHelpers.callInstanceMethod(host.first, "linkClient",
                from(Pair.class, client)
        );
        // next we set ourselves up
        attachBaseContext(client.second);
        layoutInflater = LayoutInflater.from(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
    }

    protected void setContentView(ViewGroup contentRoot) {
        runOnUIThread(() -> {
//            if (mContentRoot.getChildAt(0) != null) {
//                mContentRoot.removeViewAt(0);
//            }
            mContentRoot.addView(contentRoot);
        });
    }

    protected void setContentView(ViewGroup contentRoot, FrameLayout.LayoutParams layoutParams) {
        runOnUIThread(() -> {
//            if (mContentRoot.getChildAt(0) != null) {
//                mContentRoot.removeViewAt(0);
//            }
            mContentRoot.addView(contentRoot, layoutParams);
        });
    }

    protected void setContentView(@LayoutRes int res) {
        runOnUIThread(() -> {
            View content = layoutInflater.inflate(res, mContentRoot, false);
//            if (mContentRoot.getChildAt(0) != null) {
//                mContentRoot.removeViewAt(0);
//            }
            mContentRoot.addView(content, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        });
    }

    final public void runOnUIThread(Runnable r) {
        r.run();
//        UiThread.post(r);
    }
}