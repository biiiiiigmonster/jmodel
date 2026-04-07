package io.github.biiiiiigmonster.relation;

import io.github.biiiiiigmonster.Model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class Siblings<T extends Model<?>> extends Relation<T> {
    /**
     * @param relatedField Post.comments
     * @param foreignField Comment.post_id
     * @param localField   Post.id
     */
    public Siblings(Field relatedField, Field foreignField, Field localField) {
        super(relatedField);
    }

    @Override
    public <R extends Model<?>> List<R> getEager(List<T> models) {
        return Collections.emptyList();
    }

    @Override
    public <R extends Model<?>> void match(List<T> models, List<R> results) {

    }
}
