package io.github.biiiiiigmonster.enhance;

import io.github.biiiiiigmonster.Model;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Core class that performs bytecode enhancement on Model subclasses.
 * <p>
 * This enhancer scans compiled class files, identifies Model subclasses,
 * and enhances their setter methods with dirty-tracking support.
 *
 * @author luyunfeng
 */
public class ModelClassEnhancer {

    private final File classesDirectory;
    private final ClassLoader classLoader;
    private final Log log;
    private final TrackingFieldResolver fieldResolver;

    private List<String> includePatterns = new ArrayList<>();
    private List<String> excludePatterns = new ArrayList<>();

    // Compiled patterns for performance
    private List<Pattern> compiledIncludes = new ArrayList<>();
    private List<Pattern> compiledExcludes = new ArrayList<>();

    public ModelClassEnhancer(File classesDirectory, ClassLoader classLoader, Log log, boolean verbose) {
        this.classesDirectory = classesDirectory;
        this.classLoader = classLoader;
        this.log = log;
        this.verbose = verbose;
        this.fieldResolver = new TrackingFieldResolver(log);
    }

    public ModelClassEnhancer(File classesDirectory, ClassLoader classLoader, Log log) {
        this(classesDirectory, classLoader, log, false);
    }

    private boolean verbose = false;

    public void setIncludePatterns(List<String> patterns) {
        this.includePatterns = patterns;
        this.compiledIncludes = compilePatterns(patterns);
    }

    public void setExcludePatterns(List<String> patterns) {
        this.excludePatterns = patterns;
        this.compiledExcludes = compilePatterns(patterns);
    }

    private List<Pattern> compilePatterns(List<String> patterns) {
        List<Pattern> compiled = new ArrayList<>();
        for (String pattern : patterns) {
            // Convert glob-like patterns to regex
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("**", ".*")
                    .replace("*", "[^.]*");
            compiled.add(Pattern.compile(regex));
        }
        return compiled;
    }

    /**
     * Executes the enhancement process.
     *
     * @return EnhancementResult containing statistics
     * @throws IOException if file operations fail
     */
    public EnhancementResult enhance() throws IOException {
        EnhancementResult result = new EnhancementResult();

        // Collect all .class files
        List<Path> classFiles = collectClassFiles();
        logDebug("Found " + classFiles.size() + " class files");

        for (Path classFile : classFiles) {
            result.incrementScanned();

            String className = getClassName(classFile);
            if (className == null) {
                continue;
            }

            // Check include/exclude patterns
            if (!shouldProcess(className)) {
                logDebug("Skipping (pattern): " + className);
                result.addSkippedClass(className, "excluded by pattern");
                continue;
            }

            try {
                // Load the class
                Class<?> clazz = classLoader.loadClass(className);

                // Check if it's a Model subclass (but not Model itself)
                if (!isEnhanceableModelSubclass(clazz)) {
                    if (verbose && className.contains("entity")) {
                        logInfo("Checking class: " + className + " - superclass: " + 
                            (clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "null"));
                    }
                    logDebug("Skipping (not Model subclass): " + className);
                    result.addSkippedClass(className, "not a Model subclass");
                    continue;
                }

                // Enhance the class
                int setterCount = enhanceClass(clazz, classFile);

                if (setterCount > 0) {
                    result.addEnhancedClass(className, setterCount);
                    logInfo("Enhanced: " + className + " (" + setterCount + " setters)");
                } else {
                    result.addSkippedClass(className, "no trackable setters");
                    logDebug("Skipping (no setters): " + className);
                }

            } catch (ClassNotFoundException e) {
                result.addError(className, "Class not found: " + e.getMessage());
                logWarn("Could not load class: " + className);
            } catch (Exception e) {
                result.addError(className, e.getMessage());
                logWarn("Error enhancing " + className + ": " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Collects all .class files in the classes directory.
     */
    private List<Path> collectClassFiles() throws IOException {
        List<Path> classFiles = new ArrayList<>();

        Files.walkFileTree(classesDirectory.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".class")) {
                    classFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return classFiles;
    }

    /**
     * Converts a class file path to a fully qualified class name.
     */
    private String getClassName(Path classFile) {
        Path relativePath = classesDirectory.toPath().relativize(classFile);
        String pathString = relativePath.toString();

        // Remove .class extension and convert to package notation
        if (pathString.endsWith(".class")) {
            return pathString
                    .substring(0, pathString.length() - 6)
                    .replace(File.separatorChar, '.')
                    .replace('/', '.');
        }
        return null;
    }

    /**
     * Checks if a class name matches the include/exclude patterns.
     */
    private boolean shouldProcess(String className) {
        // If includes are specified, class must match at least one
        if (!compiledIncludes.isEmpty()) {
            boolean matched = false;
            for (Pattern pattern : compiledIncludes) {
                if (pattern.matcher(className).matches()) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        // Check excludes - if matches any, skip
        for (Pattern pattern : compiledExcludes) {
            if (pattern.matcher(className).matches()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a class is an enhanceable Model subclass.
     */
    private boolean isEnhanceableModelSubclass(Class<?> clazz) {
        // Must be a subclass of Model
        if (!Model.class.isAssignableFrom(clazz)) {
            return false;
        }

        // Must not be Model itself
        if (clazz.equals(Model.class)) {
            return false;
        }

        // Must not be abstract
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }

        // Must not be an interface
        if (clazz.isInterface()) {
            return false;
        }

        return true;
    }

    /**
     * Enhances a single class by modifying its setter methods.
     *
     * @return number of setters enhanced
     */
    private int enhanceClass(Class<?> clazz, Path classFile) throws IOException {
        // Get trackable fields for this class
        Set<String> trackableFields = fieldResolver.getTrackableFields(clazz);

        if (trackableFields.isEmpty()) {
            return 0;
        }

        logDebug("Trackable fields for " + clazz.getName() + ": " + trackableFields);

        // Find setter methods to enhance
        List<Method> settersToEnhance = findSettersToEnhance(clazz, trackableFields);

        if (settersToEnhance.isEmpty()) {
            return 0;
        }

        // Build the enhanced class using Advice
        DynamicType.Builder<?> builder = new ByteBuddy()
                .redefine(clazz);

        // Create matcher for all trackable setters
        ElementMatcher.Junction<MethodDescription> setterMatcher = ElementMatchers.none();
        Set<String> enhancedSetterNames = new HashSet<>();

        for (Method setter : settersToEnhance) {
            String fieldName = getFieldNameFromSetter(setter.getName());
            if (fieldName != null && trackableFields.contains(fieldName)) {
                setterMatcher = setterMatcher.or(
                        ElementMatchers.named(setter.getName())
                                .and(ElementMatchers.takesArguments(setter.getParameterTypes()))
                );
                enhancedSetterNames.add(setter.getName());
            }
        }

        // Apply the enhancement using Advice.visit (code is inlined at compile-time)
        // The SetterAdvice.onEnter method will be inlined into each setter
        // Using visit() instead of intercept() to preserve the original method body
        // SetterAdvice is in jmodel-core module to ensure runtime availability
        builder = builder.visit(Advice.to(io.github.biiiiiigmonster.tracking.SetterAdvice.class).on(setterMatcher));

        // Save the enhanced class
        DynamicType.Unloaded<?> unloaded = builder.make();
        unloaded.saveIn(classesDirectory);

        logDebug("Enhanced setters: " + enhancedSetterNames);

        return enhancedSetterNames.size();
    }

    /**
     * Finds setter methods that should be enhanced.
     */
    private List<Method> findSettersToEnhance(Class<?> clazz, Set<String> trackableFields) {
        List<Method> setters = new ArrayList<>();

        // Get all declared methods (including inherited ones we might want to override)
        for (Method method : clazz.getDeclaredMethods()) {
            if (isSetterMethod(method)) {
                String fieldName = getFieldNameFromSetter(method.getName());
                if (fieldName != null && trackableFields.contains(fieldName)) {
                    setters.add(method);
                }
            }
        }

        return setters;
    }

    /**
     * Checks if a method is a setter method (setXxx pattern with one parameter).
     */
    private boolean isSetterMethod(Method method) {
        String name = method.getName();
        return name.startsWith("set")
                && name.length() > 3
                && method.getParameterCount() == 1
                && method.getReturnType() == void.class
                && !Modifier.isStatic(method.getModifiers());
    }

    /**
     * Extracts field name from setter method name.
     * e.g., "setName" -> "name", "setUserName" -> "userName"
     */
    private String getFieldNameFromSetter(String setterName) {
        if (setterName.startsWith("set") && setterName.length() > 3) {
            String fieldPart = setterName.substring(3);
            // Convert first char to lowercase
            return Character.toLowerCase(fieldPart.charAt(0)) + fieldPart.substring(1);
        }
        return null;
    }

    // Logging helpers
    private void logInfo(String message) {
        if (log != null) {
            log.info("[JModel Enhance] " + message);
        }
    }

    private void logDebug(String message) {
        if (log != null) {
            log.debug("[JModel Enhance] " + message);
        }
    }

    private void logWarn(String message) {
        if (log != null) {
            log.warn("[JModel Enhance] " + message);
        }
    }
}
