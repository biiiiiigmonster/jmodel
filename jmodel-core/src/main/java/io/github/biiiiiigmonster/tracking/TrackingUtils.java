package io.github.biiiiiigmonster.tracking;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.attribute.AttributeUtils;
import io.github.biiiiiigmonster.relation.RelationType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dirty-tracking 工具类
 * <p>
 * 提供获取可追踪字段列表等功能，结果按类缓存以避免重复反射。
 *
 * @author luyunfeng
 */
public class TrackingUtils {

    /**
     * 可追踪字段缓存：key=实体类, value=可追踪字段列表
     */
    private static final Map<Class<?>, List<Field>> TRACKABLE_FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取指定类的所有可追踪字段（带缓存）
     * <p>
     * 可追踪字段条件：
     * <ul>
     *     <li>非 static</li>
     *     <li>非 transient</li>
     *     <li>无关系注解（@HasOne, @HasMany 等）</li>
     *     <li>无计算属性注解（@Attribute）</li>
     * </ul>
     * 遍历范围：从具体子类到 Model（不含 Model 自身字段）
     *
     * @param clazz 实体类
     * @return 不可变的可追踪字段列表
     */
    public static List<Field> getTrackableFields(Class<?> clazz) {
        return TRACKABLE_FIELDS_CACHE.computeIfAbsent(clazz, TrackingUtils::resolveTrackableFields);
    }

    /**
     * 解析可追踪字段（无缓存，仅首次调用时执行）
     */
    private static List<Field> resolveTrackableFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        // 遍历类层级，直到 Model（不含）
        while (current != null && current != Model.class && Model.class.isAssignableFrom(current)) {
            for (Field field : current.getDeclaredFields()) {
                if (isTrackable(field)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }

        return Collections.unmodifiableList(fields);
    }

    /**
     * 判断字段是否可追踪
     */
    private static boolean isTrackable(Field field) {
        int modifiers = field.getModifiers();

        // 排除 static 字段
        if (Modifier.isStatic(modifiers)) {
            return false;
        }

        // 排除 transient 字段
        if (Modifier.isTransient(modifiers)) {
            return false;
        }

        // 排除关系字段（带 @HasOne, @HasMany, @BelongsTo 等注解）
        if (RelationType.hasRelationAnnotation(field)) {
            return false;
        }

        // 排除计算属性字段（带 @Attribute 注解）
        if (AttributeUtils.hasAttributeAnnotation(field)) {
            return false;
        }

        return true;
    }
}
