package com.github.biiiiiigmonster.router;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.ModelNotFoundException;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationUtils;
import com.google.common.collect.Lists;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class PathModelArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnn = parameter.hasParameterAnnotation(PathModel.class);
        boolean isModel = Model.class.isAssignableFrom(parameter.getParameterType());
        return hasAnn && isModel;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        PathModel ann = parameter.getParameterAnnotation(PathModel.class);
        Assert.state(ann != null, "No PathModel annotation");
        return new PathModelNamedValueInfo(ann);
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String value;
        if (uriTemplateVars == null || (value = uriTemplateVars.get(name)) == null) {
            return null;
        }

        PathModel ann = parameter.getParameterAnnotation(PathModel.class);
        String fieldName = ann.routeKey().isEmpty() ? RelationUtils.getPrimaryKey(parameter.getParameterType()) : ann.routeKey();
        Field field = ReflectUtil.getField(parameter.getParameterType(), fieldName);
        List<String> values = Lists.newArrayList(value);
        List<?> results = RelationUtils.hasRelatedRepository(field)
                ? byRelatedRepository(values, field)
                : Relation.byRelatedMethod(values, field);

        return CollectionUtils.isEmpty(results) ? null : results.get(0);
    }

    private <T extends Model<?>> List<T> byRelatedRepository(List<String> values, Field routeField) {
        BaseMapper<T> repository = (BaseMapper<T>) RelationUtils.getRelatedRepository(routeField.getDeclaringClass());
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.in(RelationUtils.getColumn(routeField), values);
        return repository.selectList(wrapper);
    }

    protected void handleMissingValue(String name, MethodParameter parameter, NativeWebRequest request) {
        throw new ModelNotFoundException(parameter.getParameterType());
    }

    private static final class PathModelNamedValueInfo extends NamedValueInfo {

        public PathModelNamedValueInfo(PathModel annotation) {
            super(annotation.name(), annotation.required(), null);
        }
    }
}
