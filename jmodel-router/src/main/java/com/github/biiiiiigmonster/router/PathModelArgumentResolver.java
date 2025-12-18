package com.github.biiiiiigmonster.router;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.biiiiiigmonster.Model;
import com.github.biiiiiigmonster.ModelNotFoundException;
import com.github.biiiiiigmonster.relation.Relation;
import com.github.biiiiiigmonster.relation.RelationUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.View;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class PathModelArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    String PATH_MODEL_VARIABLES = View.class.getName() + ".pathModelVariables";

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
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) {
        Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        String value;
        if (uriTemplateVars == null || (value = uriTemplateVars.get(name)) == null) {
            return null;
        }

        PathModel ann = parameter.getParameterAnnotation(PathModel.class);
        String fieldName = ann.routeKey().isEmpty() ? RelationUtils.getPrimaryKey(parameter.getParameterType()) : ann.routeKey();
        Field field = ReflectUtil.getField(parameter.getParameterType(), fieldName);
        List<Model<?>> results = Relation.getResult(ListUtil.toList(value), field);
        Model<?> model = CollectionUtils.isEmpty(results) ? null : results.get(0);
        if (model != null && ann.scopeBinding()) {
            scopeBinding(model, parameter, request);
        }

        return model;
    }


    protected void scopeBinding(Model<?> model, MethodParameter parameter, NativeWebRequest request) {
        String key = PATH_MODEL_VARIABLES;
        int scope = RequestAttributes.SCOPE_REQUEST;
        LinkedHashMap<String, Object> pathVars = (LinkedHashMap<String, Object>) request.getAttribute(key, scope);
        if (pathVars == null) {
            return;
        }

        Map.Entry<String, Object> parent = null;
        for (Map.Entry<String, Object> entry : pathVars.entrySet()) {
            parent = entry;
        }
        if (parent == null) {
            return;
        }

        RelationUtils.load(model, parent.getKey());
        Object associate = ReflectUtil.getFieldValue(model, parent.getKey());
        if (associate instanceof List) {
            throw new ModelNotFoundException(parameter.getParameterType());
        }
        if (((Model<?>) associate).isNot((Model<?>) parent.getValue())) {
            throw new ModelNotFoundException(parameter.getParameterType());
        }
    }

    @Override
    protected void handleResolvedValue(Object arg, String name, MethodParameter parameter,
                                       ModelAndViewContainer mavContainer, NativeWebRequest request) {
        String key = PATH_MODEL_VARIABLES;
        int scope = RequestAttributes.SCOPE_REQUEST;
        LinkedHashMap<String, Object> pathVars = (LinkedHashMap<String, Object>) request.getAttribute(key, scope);
        if (pathVars == null) {
            pathVars = new LinkedHashMap<>();
            request.setAttribute(key, pathVars, scope);
        }
        pathVars.put(name, arg);
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
