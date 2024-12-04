package com.biiiiiigmonster.octopus.model;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/26 9:52
 */
@Slf4j
public abstract class Model<T extends Model<?>> {
    private static final Map<String, WeakReference<Field>> LAMBDA_CACHE = new ConcurrentHashMap<>();

    public <R> R get(SerializableFunction<T, R> column) {
        R value = column.apply((T) this);
        if (value == null) {
            Field field = getField(column);
            if (field.getAnnotation(Relation.class) != null) {
                load(field.getName());
            } else if (field.getAnnotation(Computed.class) != null) {
                append(field);
            }
        }

        return column.apply((T) this);
    }

    @SafeVarargs
    public final <R> void append(SerializableFunction<T, R>... columns) {
        Arrays.stream(columns).forEach(column -> append(getField(column)));
    }

    public final void append(String... columns) {
        Arrays.stream(columns).forEach(column -> append(getField(column)));
    }

    private Field getField(SerializableFunction<T, ?> column) {
        Class<?> clazz = column.getClass();
        String name = clazz.getName();
        return Optional.ofNullable(LAMBDA_CACHE.get(name))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda lambda = SerializedLambda.resolve(column);
                    Field field = getField(methodToProperty(lambda.getImplMethodName()));
                    LAMBDA_CACHE.put(name, new WeakReference<>(field));
                    return field;
                });
    }

    private Field getField(String column) {
        return ReflectUtil.getField(this.getClass(), column);
    }

    private void append(Field field) {
        Map<Object, Method> methodMap = RelationUtils.getFillMethod(field);
        if (methodMap == null) {
            return;
        }
        Object bean = methodMap.keySet().iterator().next();
        Method method = methodMap.values().iterator().next();
        ReflectUtil.invoke(bean, method, this);
    }

    public static Class<?> toClassConfident(String name) {
        try {
            return Class.forName(name, false, getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = Model.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    private static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new RuntimeException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

    @SafeVarargs
    public final <R> void load(SerializableFunction<T, R>... withs) {
        RelationUtils.load((T) this, Arrays.stream(withs).map(with -> getField(with).getName()).toArray(String[]::new));
    }

    public final void load(String... withs) {
        RelationUtils.load((T) this, withs);
    }

    @SafeVarargs
    public final <R> void loadForce(SerializableFunction<T, R>... withs) {
        RelationUtils.loadForce((T) this, Arrays.stream(withs).map(with -> getField(with).getName()).toArray(String[]::new));
    }

    public final void loadForce(String... withs) {
        RelationUtils.loadForce((T) this, withs);
    }
}
