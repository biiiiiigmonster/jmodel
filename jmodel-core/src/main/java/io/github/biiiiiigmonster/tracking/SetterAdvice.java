package io.github.biiiiiigmonster.tracking;

import io.github.biiiiiigmonster.Model;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Field;

/**
 * Advice class for setter method enhancement.
 * <p>
 * This class is used by ByteBuddy at compile-time to inline tracking code
 * into setter methods. Enhanced setters will call $jmodel$trackChange()
 * to record field modifications.
 * <p>
 * Enhanced setter will look like:
 * <pre>{@code
 * public void setName(String name) {
 *     // Injected code (from onEnter):
 *     SetterAdvice.onEnter(this, "setName", name);
 *     
 *     // Original code:
 *     this.name = name;
 * }
 * }</pre>
 *
 * @author luyunfeng
 */
public class SetterAdvice {

    /**
     * Called before the original setter method executes.
     * This code is inlined into the setter at compile-time.
     *
     * @param self       the Model instance
     * @param methodName the name of the setter method (e.g., "setName")
     * @param newValue   the new value being set
     */
    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.This Object self,
            @Advice.Origin("#m") String methodName,
            @Advice.Argument(0) Object newValue) {

        // Extract field name from setter method name (setName -> name)
        if (methodName == null || !methodName.startsWith("set") || methodName.length() <= 3) {
            return;
        }

        String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);

        // Get current value before setter executes
        Object oldValue = null;
        try {
            Field field = findFieldInHierarchy(self.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                oldValue = field.get(self);
            }
        } catch (Exception ignored) {
            // Ignore errors getting old value
        }

        // Call trackChange on the Model
        if (self instanceof Model) {
            try {
                ((Model<?>) self).$jmodel$trackChange(fieldName, oldValue, newValue);
            } catch (Exception ignored) {
                // Ignore tracking errors to not break the setter
            }
        }
    }

    /**
     * Finds a field in the class hierarchy.
     * Note: This method must be public for ByteBuddy Advice inlining to work correctly.
     */
    public static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
