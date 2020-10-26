package smallville7123.reflectionutils;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import smallville7123.taggable.Taggable;

public class ReflectionUtils {
    
    ArrayList<Object> internalList = new ArrayList();

    public Object getField(Object object, String fieldName) {
        return getField(null, object, fieldName);
    }

    public Object getField(Class aClass, Object object, String fieldName) {
        ReflectionField x = new ReflectionField();
        internalList.add(x);
        return x.get(aClass, object, fieldName);
    }

    public void setField(Object object, String fieldName, Object value) {
        setField(null, object, fieldName, value);
    }

    public void setField(Class aClass, Object object, String fieldName, Object value) {
        ReflectionField x = new ReflectionField();
        internalList.add(x);
        x.set(aClass, object, fieldName, value);
    }

    public Object invokeMethod(Object object, String methodName) {
        return invokeMethod(null, object, methodName);
    }

    public Object invokeMethod(Class aClass, Object object, String methodName) {
        ReflectionMethod x = new ReflectionMethod();
        internalList.add(x);
        return x.invoke(aClass, object, methodName);
    }

    public Object invokeMethod(Object object, String methodName, Object ... args) {
        return invokeMethod(null, object, methodName, args);
    }

    public Object invokeMethod(Class aClass, Object object, String methodName, Object ... args) {
        ReflectionMethod x = new ReflectionMethod();
        internalList.add(x);
        return x.invoke(aClass, object, methodName, args);
    }

    public Object instantiate(Object object, String className) {
        return instantiate(null, object, className);
    }

    public Object instantiate(Class aClass, Object object, String className) {
        ReflectionInstantiator x = new ReflectionInstantiator();
        internalList.add(x);
        return x.instantiate(aClass, object, className);
    }

    public Object instantiate(Object object, String className, Object ... args) {
        return instantiate(null, object, className, args);
    }

    public Object instantiate(Class aClass, Object object, String className, Object ... args) {
        ReflectionInstantiator x = new ReflectionInstantiator();
        internalList.add(x);
        return x.instantiate(aClass, object, className, args);
    }

    public Object instantiateInner(Object object, String className) {
        return instantiateInner(null, object, className);
    }

    public Object instantiateInner(Class aClass, Object object, String className) {
        ReflectionInstantiator x = new ReflectionInstantiator();
        internalList.add(x);
        return x.instantiateInner(aClass, object, className);
    }

    public Object instantiateInner(Object object, String className, Object ... args) {
        return instantiateInner(null, object, className, args);
    }

    public Object instantiateInner(Class aClass, Object object, String className, Object ... args) {
        ReflectionInstantiator x = new ReflectionInstantiator();
        internalList.add(x);
        return x.instantiateInner(aClass, object, className, args);
    }

    private static Class getClass(Class aClass, Object object) {
        return aClass == null ? object.getClass() : aClass;
    }

    private class ReflectionField {
        String TAG = Taggable.getTag(this);

        private Field field;
        private boolean fieldFetched;

        Object get(Class aClass, Object object, String fieldName) {
            if (!fieldFetched) {
                try {
                    // try public
                    field = ReflectionUtils.getClass(aClass, object).getField(fieldName);
                    field.setAccessible(true);
                    fieldFetched = true;
                } catch (NoSuchFieldException e) {
                    try {
                        // try non-public
                        field = ReflectionUtils.getClass(aClass, object).getDeclaredField(fieldName);
                        field.setAccessible(true);
                        fieldFetched = true;
                    } catch (NoSuchFieldException ex) {
                        Log.e(TAG, "No Such Field: Failed to retrieve field: " + fieldName, e);
                        Log.e(TAG, "No Such Field: Failed to retrieve field: " + fieldName, ex);
                    }
                }
            }

            if (field != null) {
                try {
                    return field.get(object);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Illegal Access: Failed to get value from field: " + fieldName, e);
                    field = null;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal Argument: Failed to get value from field: " + fieldName, e);
                    field = null;
                }
            } else {
                Log.e(TAG, "field is null: " + fieldName);
            }
            return null;
        }

        void set(Class aClass, Object object, String fieldName, Object value) {
            if (!fieldFetched) {
                try {
                    // try public
                    field = ReflectionUtils.getClass(aClass, object).getField(fieldName);
                    field.setAccessible(true);
                    fieldFetched = true;
                } catch (NoSuchFieldException e) {
                    try {
                        // try non-public
                        field = ReflectionUtils.getClass(aClass, object).getDeclaredField(fieldName);
                        field.setAccessible(true);
                        fieldFetched = true;
                    } catch (NoSuchFieldException ex) {
                        Log.e(TAG, "No Such Field: Failed to retrieve field: " + fieldName, e);
                        Log.e(TAG, "No Such Field: Failed to retrieve field: " + fieldName, ex);
                    }
                }
            }

            if (field != null) {
                try {
                    field.set(object, value);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Illegal Access: Failed to set value from field: " + fieldName, e);
                    field = null;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal Argument: Failed to set value from field: " + fieldName, e);
                    field = null;
                }
            } else {
                Log.e(TAG, "field is null: " + fieldName);
            }
        }
    }

    private Class[] toClassArray(Object ... args) {
        if (args == null) return null;
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        return classes;
    }

    private Class getInnerClass(Class outer, String inner) throws ReflectiveOperationException {
        Class[] classes = outer.getClasses();
        Class[] declaredClasses = outer.getDeclaredClasses();
        if (classes.length == 0 && declaredClasses.length == 0) throw new ReflectiveOperationException("class has no constructors: " + outer);
        for (Class aClass : classes) {
            if (aClass.getSimpleName().contentEquals(inner)) {
                return aClass;
            }
        }
        for (Class aClass : declaredClasses) {
            if (aClass.getSimpleName().contentEquals(inner)) {
                return aClass;
            }
        }
        return null;
    }

    private class ReflectionMethod {
        String TAG = Taggable.getTag(this);

        private Method method;
        private boolean methodFetched;

        Object invoke(Class aClass, Object object, String methodName) {
            return invoke(aClass, object, methodName, (Object[]) null);
        }


        Object invoke(Class aClass, Object object, String methodName, Object ... args) {
            if (!methodFetched) {
                try {
                    // try public
                    if (args != null) {
                        method = ReflectionUtils.getClass(aClass, object).getMethod(methodName, toClassArray(args));
                    } else {
                        method = ReflectionUtils.getClass(aClass, object).getMethod(methodName);
                    }
                    method.setAccessible(true);
                    methodFetched = true;
                } catch (NoSuchMethodException e) {
                    try {
                        // try non-public
                        if (args != null) {
                            method = ReflectionUtils.getClass(aClass, object).getDeclaredMethod(methodName, toClassArray(args));
                        } else {
                            method = ReflectionUtils.getClass(aClass, object).getDeclaredMethod(methodName);
                        }
                        method.setAccessible(true);
                        methodFetched = true;
                    } catch (NoSuchMethodException ex) {
                        Log.e(TAG, "No Such Field: Failed to retrieve method: " + methodName, e);
                        Log.e(TAG, "No Such Field: Failed to retrieve method: " + methodName, ex);
                    }
                }
            }

            if (method != null) {
                try {
                    if (args != null) {
                        return method.invoke(object, args);
                    } else {
                        return method.invoke(object);
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Illegal Access: Failed to invoke method: " + methodName, e);
                    method = null;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal Argument: Failed to invoke method: " + methodName, e);
                    method = null;
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "Invocation Target: Failed to invoke method: " + methodName, e);
                    method = null;
                }
            } else {
                Log.e(TAG, "method is null: " + methodName);
            }
            return null;
        }
    }

    private class ReflectionInstantiator {
        String TAG = Taggable.getTag(this);

        private Constructor constructor;
        private boolean constructorFetched;

        public Object instantiate(Class aClass, Object object, String className) {
            return instantiate(aClass, object, className, (Object[]) null);
        }

        public Object instantiate(Class aClass, Object object, String className, Object ... args) {
            if (!constructorFetched) {
                Class target = ReflectionUtils.getClass(aClass, object);
                Constructor[] constructors = target.getConstructors();
                Constructor[] declaredconstructors = target.getDeclaredConstructors();
                try {
                    // try public
                    if (args != null) {
                        constructor = target.getConstructor(toClassArray(args));
                    } else {
                        if (constructors.length != 0) constructor = constructors[0];
                        else throw new NoSuchMethodException("no public constructors found");
                    }
                    constructor.setAccessible(true);
                    constructorFetched = true;
                } catch (NoSuchMethodException e) {
                    try {
                        // try non-public
                        if (args != null) {
                            constructor = target.getDeclaredConstructor(toClassArray(args));
                        } else {
                            if (declaredconstructors.length != 0) constructor = declaredconstructors[0];
                            else throw new NoSuchMethodException("no non-public constructors found");
                        }
                        constructor.setAccessible(true);
                        constructorFetched = true;
                    } catch (NoSuchMethodException ex) {
                        Log.e(TAG, "No Such Field: Failed to retrieve constructor: " + className, e);
                        Log.e(TAG, "No Such Field: Failed to retrieve constructor: " + className, ex);
                    }
                }
            }

            if (constructor != null) {
                try {
                    if (args != null) {
                        return constructor.newInstance(args);
                    } else {
                        return constructor.newInstance();
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Illegal Access: Failed to invoke constructor: " + className, e);
                    constructor = null;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal Argument: Failed to invoke constructor: " + className, e);
                    constructor = null;
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "Invocation Target: Failed to invoke constructor: " + className, e);
                    constructor = null;
                } catch (InstantiationException e) {
                    Log.e(TAG, "Instantiation: Failed to invoke constructor: " + className, e);
                    constructor = null;
                }
            } else {
                Log.e(TAG, "constructor is null: " + className);
            }
            return null;
        }

        public Object instantiateInner(Class aClass, Object object, String className) {
            return instantiateInner(aClass, object, className, (Object[]) null);
        }

        public Object instantiateInner(Class aClass, Object object, String className, Object ... args) {
            if (!constructorFetched) {
                Class target = ReflectionUtils.getClass(aClass, object);
                if (className != null) {
                    try {
                        Class t = getInnerClass(target, className);
                        if (t != null) target = t;
                        else Log.e(TAG, "failed to find class: " + className);
                    } catch (ReflectiveOperationException e) {
                        Log.e(TAG, "Reflection Operation: failed to find class: " + className, e);
                    }
                }
                Constructor[] constructors = target.getConstructors();
                Constructor[] declaredconstructors = target.getDeclaredConstructors();
                try {
                    // try public
                    if (args != null) {
                        constructor = target.getConstructor(toClassArray(args));
                    } else {
                        if (constructors.length != 0) constructor = constructors[0];
                        else throw new NoSuchMethodException("no public constructors found");
                    }
                    constructor.setAccessible(true);
                    constructorFetched = true;
                } catch (NoSuchMethodException e) {
                    try {
                        // try non-public
                        if (args != null) {
                            constructor = target.getDeclaredConstructor(toClassArray(args));
                        } else {
                            if (declaredconstructors.length != 0) constructor = declaredconstructors[0];
                            else throw new NoSuchMethodException("no non-public constructors found");
                        }
                        constructor.setAccessible(true);
                        constructorFetched = true;
                    } catch (NoSuchMethodException ex) {
                        Log.e(TAG, "No Such Field: Failed to retrieve constructor: " + className, e);
                        Log.e(TAG, "No Such Field: Failed to retrieve constructor: " + className, ex);
                    }
                }
            }

            if (constructor != null) {
                try {
                    if (args != null) {
                        return constructor.newInstance(object, args);
                    } else {
                        return constructor.newInstance(object);
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Illegal Access: Failed to invoke constructor: " + className, e);
                    constructor = null;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Illegal Argument: Failed to invoke constructor: " + className, e);
                    constructor = null;
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "Invocation Target: Failed to invoke constructor: " + className, e);
                    constructor = null;
                } catch (InstantiationException e) {
                    Log.e(TAG, "Instantiation: Failed to invoke constructor: " + className, e);
                    constructor = null;
                }
            } else {
                Log.e(TAG, "constructor is null: " + className);
            }
            return null;
        }
    }
}
