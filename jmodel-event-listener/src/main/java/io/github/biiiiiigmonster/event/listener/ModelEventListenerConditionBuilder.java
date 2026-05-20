package io.github.biiiiiigmonster.event.listener;

import io.github.biiiiiigmonster.Model;
import org.springframework.util.StringUtils;

/**
 * 将 {@link io.github.biiiiiigmonster.ModelEventListener#models()} 转换为 SpEL 条件表达式。
 */
public final class ModelEventListenerConditionBuilder {

    private static final String MODEL_PROPERTY = "event.model";

    private ModelEventListenerConditionBuilder() {
    }

    public static String build(Class<? extends Model<?>>[] models, String condition) {
        String modelsCondition = buildModelsCondition(models);
        boolean hasModelsCondition = StringUtils.hasText(modelsCondition);
        boolean hasCondition = StringUtils.hasText(condition);

        if (hasModelsCondition && hasCondition) {
            return "(" + modelsCondition + ") && (" + condition + ")";
        }
        if (hasModelsCondition) {
            return modelsCondition;
        }
        return hasCondition ? condition : "";
    }

    private static String buildModelsCondition(Class<? extends Model<?>>[] models) {
        if (models == null || models.length == 0) {
            return "";
        }
        if (models.length == 1) {
            return MODEL_PROPERTY + " instanceof T(" + models[0].getName() + ")";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < models.length; i++) {
            if (i > 0) {
                builder.append(" || ");
            }
            builder.append(MODEL_PROPERTY)
                    .append(" instanceof T(")
                    .append(models[i].getName())
                    .append(")");
        }
        return builder.toString();
    }
}
