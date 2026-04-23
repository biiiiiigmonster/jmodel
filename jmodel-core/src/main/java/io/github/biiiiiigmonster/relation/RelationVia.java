package io.github.biiiiiigmonster.relation;

import io.github.biiiiiigmonster.Model;
import io.github.biiiiiigmonster.driver.DataDriver;
import io.github.biiiiiigmonster.driver.DriverRegistry;
import io.github.biiiiiigmonster.driver.QueryCondition;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.github.biiiiiigmonster.relation.Relation.relatedKeyValueList;

@Getter
@SuppressWarnings({"unchecked", "rawtypes"})
public class RelationVia<R extends Model<?>> {
    private final Field localField;
    private final Field foreignField;
    private final List<Consumer<QueryCondition>> constraints;
    private List<R> results;

    public RelationVia(Field localField, Field foreignField, List<Consumer<QueryCondition>> constraints) {
        this.localField = localField;
        this.foreignField = foreignField;
        this.constraints = constraints;
    }

    public <T extends Model> List<R> getResult(List<T> models) {
        List<?> localKeyValueList = relatedKeyValueList(models, localField);
        if (CollectionUtils.isEmpty(localKeyValueList)) {
            return (results = new ArrayList<>());
        }
        Class<R> entityClass = (Class<R>) foreignField.getDeclaringClass();
        DataDriver driver = DriverRegistry.getDriver(entityClass);
        QueryCondition<R> condition = QueryCondition.create(entityClass);
        condition.in(RelationUtils.getColumn(foreignField), localKeyValueList);
        if (!CollectionUtils.isEmpty(constraints)) {
            for (Consumer<QueryCondition> c : constraints) {
                c.accept(condition);
            }
        }
        return (results = driver.findByCondition(condition));
    }

    public void addConstraints(List<Consumer<QueryCondition>> list) {
        if (!CollectionUtils.isEmpty(list)) {
            this.constraints.addAll(list);
        }
    }
}
