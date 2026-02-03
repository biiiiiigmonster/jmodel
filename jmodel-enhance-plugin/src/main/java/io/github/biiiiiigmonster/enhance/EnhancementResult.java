package io.github.biiiiiigmonster.enhance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of the enhancement process.
 * Contains statistics and details about enhanced classes.
 *
 * @author luyunfeng
 */
public class EnhancementResult {
    private int scannedCount = 0;
    private int enhancedCount = 0;
    private int settersEnhancedCount = 0;
    private final List<String> enhancedClasses = new ArrayList<>();
    private final List<String> skippedClasses = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public void incrementScanned() {
        scannedCount++;
    }

    public void addEnhancedClass(String className, int setterCount) {
        enhancedCount++;
        settersEnhancedCount += setterCount;
        enhancedClasses.add(className);
    }

    public void addSkippedClass(String className, String reason) {
        skippedClasses.add(className + " (" + reason + ")");
    }

    public void addError(String className, String error) {
        errors.add(className + ": " + error);
    }

    public int getScannedCount() {
        return scannedCount;
    }

    public int getEnhancedCount() {
        return enhancedCount;
    }

    public int getSettersEnhancedCount() {
        return settersEnhancedCount;
    }

    public List<String> getEnhancedClasses() {
        return Collections.unmodifiableList(enhancedClasses);
    }

    public List<String> getSkippedClasses() {
        return Collections.unmodifiableList(skippedClasses);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
