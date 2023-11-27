package com.biiiiiigmonster.jmodel.model;

import cn.hutool.core.collection.ListUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author v-luyunfeng
 * @date 2023/10/25 18:16
 */
@Data
public class RelationOption<T> {
    private List<T> models;
    private String field;
    private Boolean loadMissing = false;
    private List<RelationOption<T>> nestedOptions = new ArrayList<>();

    public RelationOption(List<T> models, String field) {
        this.models = models;
        this.field = field;
    }

    public RelationOption(T model, String field) {
        this.models = ListUtil.toList(model);
        this.field = field;
    }
}
