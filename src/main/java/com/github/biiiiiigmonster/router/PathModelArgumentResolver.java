package com.github.biiiiiigmonster.router;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.ModelNotFoundException;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

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
    @SuppressWarnings("unchecked")
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String value;
        if (uriTemplateVars == null || (value = uriTemplateVars.get(name)) == null) {
            return null;
        }

        PathModel ann = parameter.getParameterAnnotation(PathModel.class);
        Model<?> model = (Model<?>) parameter.getParameterType().getDeclaredConstructor().newInstance();
        QueryWrapper<Model<?>> queryWrapper = new QueryWrapper<>(model);
        queryWrapper.eq(ann.routeKey().isEmpty() ? RelationUtils.getPrimaryKey(parameter.getParameterType()) : ann.routeKey(), value);
        model = model.first(queryWrapper);

        return model;
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
