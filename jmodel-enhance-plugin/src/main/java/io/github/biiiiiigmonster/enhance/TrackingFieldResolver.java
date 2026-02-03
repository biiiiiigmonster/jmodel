package io.github.biiiiiigmonster.enhance;

import org.apache.maven.plugin.logging.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolves which fields should be tracked for dirty-tracking.
 * <p>
 * Excludes:
 * <ul>
 *   <li>Fields with JModel relation annotations (@HasOne, @HasMany, etc.)</li>
 *   <li>Fields with JModel computed attribute annotations (@Computed)</li>
 *   <li>transient fields</li>
 *   <li>static fields</li>
 *   <li>Fields starting with $ (internal fields)</li>
 * </ul>
 *
 * @author luyunfeng
 */
public class TrackingFieldResolver {

    private final Log log;

    /**
     * JModel relation annotation class names that should be excluded from tracking.
     */
    private static final Set<String> RELATION_ANNOTATION_NAMES = new HashSet<>(Arrays.asList(
            "io.github.biiiiiigmonster.relation.annotation.HasOne",
            "io.github.biiiiiigmonster.relation.annotation.HasMany",
            "io.github.biiiiiigmonster.relation.annotation.BelongsTo",
            "io.github.biiiiiigmonster.relation.annotation.BelongsToMany",
            "io.github.biiiiiigmonster.relation.annotation.HasOneThrough",
            "io.github.biiiiiigmonster.relation.annotation.HasManyThrough",
            "io.github.biiiiiigmonster.relation.annotation.MorphOne",
            "io.github.biiiiiigmonster.relation.annotation.MorphMany",
            "io.github.biiiiiigmonster.relation.annotation.MorphTo",
            "io.github.biiiiiigmonster.relation.annotation.MorphToMany",
            "io.github.biiiiiigmonster.relation.annotation.MorphedByMany"
    ));

    /**
     * Attribute annotation class names that should be excluded from tracking.
     */
    private static final Set<String> ATTRIBUTE_ANNOTATION_NAMES = new HashSet<>(Arrays.asList(
            "io.github.biiiiiigmonster.attribute.Computed"
    ));

    public TrackingFieldResolver(Log log) {
        this.log = log;
    }

    /**
     * Gets all trackable field names for a given class.
     *
     * @param clazz the class to analyze
     * @return set of trackable field names
     */
    public Set<String> getTrackableFields(Class<?> clazz) {
        Set<String> trackableFields = new HashSet<>();

        // Process all fields in the class hierarchy (up to Model)
        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.getName().equals("io.github.biiiiiigmonster.Model")) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (isTrackableField(field)) {
                    trackableFields.add(field.getName());
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return trackableFields;
    }

    /**
     * Checks if a field should be tracked for dirty-tracking.
     */
    private boolean isTrackableField(Field field) {
        String fieldName = field.getName();

        // Exclude internal fields (starting with $)
        if (fieldName.startsWith("$")) {
            logDebug("Excluding field " + fieldName + ": internal field");
            return false;
        }

        // Exclude static fields
        if (Modifier.isStatic(field.getModifiers())) {
            logDebug("Excluding field " + fieldName + ": static");
            return false;
        }

        // Exclude transient fields
        if (Modifier.isTransient(field.getModifiers())) {
            logDebug("Excluding field " + fieldName + ": transient");
            return false;
        }

        // Exclude relation fields
        if (hasRelationAnnotation(field)) {
            logDebug("Excluding field " + fieldName + ": relation annotation");
            return false;
        }

        // Exclude computed attribute fields
        if (hasAttributeAnnotation(field)) {
            logDebug("Excluding field " + fieldName + ": computed attribute");
            return false;
        }

        return true;
    }

    /**
     * Checks if a field has any JModel relation annotation.
     */
    private boolean hasRelationAnnotation(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            String annotationName = annotation.annotationType().getName();
            if (RELATION_ANNOTATION_NAMES.contains(annotationName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a field has any JModel attribute/computed annotation.
     */
    private boolean hasAttributeAnnotation(Field field) {
        for (Annotation annotation : field.getAnnotations()) {
            String annotationName = annotation.annotationType().getName();
            if (ATTRIBUTE_ANNOTATION_NAMES.contains(annotationName)) {
                return true;
            }
        }
        return false;
    }

    private void logDebug(String message) {
        if (log != null) {
            log.debug("[TrackingFieldResolver] " + message);
        }
    }
}
