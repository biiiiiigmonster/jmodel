package io.github.biiiiiigmonster.driver;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import io.github.biiiiiigmonster.Model;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 内存数据驱动实现（仅用于测试）
 * 将实体存储在内存 Map 中，支持基本的 CRUD 和条件查询
 */
@Component
public class InMemoryDataDriver implements DataDriver {

    /**
     * 数据存储：entityClass -> (id -> entity)
     */
    private final Map<Class<?>, Map<Long, Model<?>>> storage = new ConcurrentHashMap<>();

    /**
     * 自增ID计数器：entityClass -> counter
     */
    private final Map<Class<?>, AtomicLong> idCounters = new ConcurrentHashMap<>();

    /**
     * 清空所有数据（每次测试前调用）
     */
    public void clear() {
        storage.clear();
        idCounters.clear();
    }

    /**
     * 直接放入实体（用于测试数据初始化，不触发自增ID）
     */
    public <T extends Model<?>> void put(Class<T> clazz, T entity) {
        Map<Long, Model<?>> map = storage.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        Long id = (Long) ReflectUtil.getFieldValue(entity, "id");
        if (id != null) {
            map.put(id, entity);
            // 更新计数器，确保后续自增ID不冲突
            AtomicLong counter = idCounters.computeIfAbsent(clazz, k -> new AtomicLong(0));
            if (id >= counter.get()) {
                counter.set(id);
            }
        }
    }

    @Override
    public <T extends Model<?>> List<T> findByCondition(QueryCondition<T> condition) {
        Class<T> entityClass = condition.getEntityClass();
        Map<Long, Model<?>> map = storage.getOrDefault(entityClass, new ConcurrentHashMap<>());

        return map.values().stream()
                .filter(entity -> matchesCriteria(entity, condition.getCriteria()))
                .map(entity -> BeanUtil.copyProperties(entity, entityClass))
                .collect(Collectors.toList());
    }

    @Override
    public int insert(Model<?> entity) {
        Class<?> clazz = entity.getClass();
        Map<Long, Model<?>> map = storage.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
        AtomicLong counter = idCounters.computeIfAbsent(clazz, k -> new AtomicLong(0));

        // 自增ID
        Long currentId = (Long) ReflectUtil.getFieldValue(entity, "id");
        if (currentId == null) {
            long newId = counter.incrementAndGet();
            ReflectUtil.setFieldValue(entity, "id", newId);
            currentId = newId;
        } else {
            // 如果已有ID，确保计数器不冲突
            if (currentId >= counter.get()) {
                counter.set(currentId);
            }
        }

        map.put(currentId, entity);
        return 1;
    }

    @Override
    public int update(Model<?> entity) {
        Class<?> clazz = entity.getClass();
        Map<Long, Model<?>> map = storage.get(clazz);
        if (map == null) {
            return 0;
        }

        Long id = (Long) ReflectUtil.getFieldValue(entity, "id");
        if (id != null && map.containsKey(id)) {
            map.put(id, entity);
            return 1;
        }
        return 0;
    }

    @Override
    public int deleteById(Class<? extends Model<?>> entityClass, Serializable id) {
        Map<Long, Model<?>> map = storage.get(entityClass);
        if (map != null && map.remove(id) != null) {
            return 1;
        }
        return 0;
    }

    /**
     * 判断实体是否匹配所有条件
     */
    private boolean matchesCriteria(Model<?> entity, List<QueryCondition.Criterion> criteria) {
        for (QueryCondition.Criterion criterion : criteria) {
            if (!matchesCriterion(entity, criterion)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断实体是否匹配单个条件
     */
    private boolean matchesCriterion(Model<?> entity, QueryCondition.Criterion criterion) {
        String fieldName = criterion.getField();
        Object conditionValue = criterion.getValue();
        Object fieldValue = ReflectUtil.getFieldValue(entity, fieldName);

        switch (criterion.getType()) {
            case EQ:
                return objectEquals(fieldValue, conditionValue);
            case IN:
                if (conditionValue instanceof Collection) {
                    Collection<?> values = (Collection<?>) conditionValue;
                    return values.stream().anyMatch(v -> objectEquals(fieldValue, v));
                }
                return false;
            case GT:
                return compareValues(fieldValue, conditionValue) > 0;
            case LT:
                return compareValues(fieldValue, conditionValue) < 0;
            case LIKE:
                if (fieldValue instanceof String && conditionValue instanceof String) {
                    return ((String) fieldValue).contains((String) conditionValue);
                }
                return false;
            case IS_NULL:
                return fieldValue == null;
            case IS_NOT_NULL:
                return fieldValue != null;
            default:
                return false;
        }
    }

    /**
     * 比较两个对象是否相等，处理 Long/Integer 类型不一致的情况
     */
    private boolean objectEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;

        // 处理数字类型比较（Long vs Integer 等）
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).longValue() == ((Number) b).longValue();
        }
        return false;
    }

    /**
     * 比较两个值的大小
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareValues(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            if (a instanceof Number && b instanceof Number) {
                return Long.compare(((Number) a).longValue(), ((Number) b).longValue());
            }
            return ((Comparable) a).compareTo(b);
        }
        return 0;
    }
}
