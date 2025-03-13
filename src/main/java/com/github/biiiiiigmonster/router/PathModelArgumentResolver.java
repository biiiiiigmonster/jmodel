package com.github.biiiiiigmonster.router;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.View;

import java.util.HashMap;
import java.util.Map;

public class PathModelArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(PathModel.class);
        boolean isModel = Model.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && isModel;
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
        QueryWrapper<?> queryWrapper = new QueryWrapper<>(model.getClass());
        queryWrapper.eq(ann.routeKey().isEmpty() ? RelationUtils.getPrimaryKey(parameter.getParameterType()) : ann.routeKey(), value);
        model = model.first(queryWrapper);

        if (model != null && ann.scopeBinding()) {
            String key = View.PATH_VARIABLES;
            int scope = RequestAttributes.SCOPE_REQUEST;
            Map<String, Object> pathVars = (Map<String, Object>) request.getAttribute(key, scope);
            if (pathVars != null) {
                Model<?> parent = (Model<?>) pathVars.values().toArray()[pathVars.size() - 1];
                if (!model.isAssociate(parent)) {
                    // 抛出异常，不是关联关系
                }
            }
        }

        return model;
    }

    protected void handleMissingValue(String name, MethodParameter parameter, NativeWebRequest request)
            throws Exception {
        // 抛出异常
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleResolvedValue(@Nullable Object arg, String name, MethodParameter parameter,
                                       @Nullable ModelAndViewContainer mavContainer, NativeWebRequest request) {
        String key = View.PATH_VARIABLES;
        int scope = RequestAttributes.SCOPE_REQUEST;
        Map<String, Object> pathVars = (Map<String, Object>) request.getAttribute(key, scope);
        if (pathVars == null) {
            pathVars = new HashMap<>();
            request.setAttribute(key, pathVars, scope);
        }
        pathVars.put(name, arg);
    }

    private static final class PathModelNamedValueInfo extends NamedValueInfo {

        public PathModelNamedValueInfo(PathModel annotation) {
            super(annotation.name(), annotation.required(), null);
        }
    }
}
