package io.github.biiiiiigmonster.enhance;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Model 子类字节码增强器。
 * <p>
 * 负责扫描编译输出目录中的 {@code Model} 子类，识别可追踪字段，
 * 并使用 ByteBuddy 对 setter 方法注入追踪代码。
 *
 * @author luyunfeng
 */
public class ModelClassEnhancer implements Closeable {

    private static final String MODEL_CLASS_NAME = "io.github.biiiiiigmonster.Model";
    private static final String RELATION_META_ANNOTATION = "io.github.biiiiiigmonster.relation.annotation.config.Relation";
    private static final String ATTRIBUTE_ANNOTATION = "io.github.biiiiiigmonster.attribute.Attribute";

    private final File classesDirectory;
    private final ClassFileLocator classFileLocator;
    private final TypePool typePool;
    private final URLClassLoader classLoader;
    private final Log log;

    public ModelClassEnhancer(File classesDirectory, List<String> compileClasspathElements, Log log) throws IOException {
        this.classesDirectory = classesDirectory;
        this.log = log;

        // 构建 URLClassLoader，包含编译期所有类路径
        List<URL> urls = new ArrayList<URL>();
        for (String element : compileClasspathElements) {
            try {
                urls.add(new File(element).toURI().toURL());
            } catch (MalformedURLException e) {
                log.warn("[JModel Enhance] Invalid classpath element: " + element, e);
            }
        }
        this.classLoader = new URLClassLoader(urls.toArray(new URL[0]));

        // 构建 ClassFileLocator：classes 目录 + 编译期类路径
        this.classFileLocator = new ClassFileLocator.Compound(
            new ClassFileLocator.ForFolder(classesDirectory),
            ClassFileLocator.ForClassLoader.of(classLoader)
        );
        this.typePool = TypePool.Default.of(classFileLocator);
    }

    /**
     * 执行增强，返回增强的类数量
     */
    public int enhance() throws MojoExecutionException {
        List<File> classFiles = new ArrayList<File>();
        scanClassFiles(classesDirectory, classFiles);
        int count = 0;

        for (File classFile : classFiles) {
            String className = resolveClassName(classFile);
            if (className == null) {
                continue;
            }

            try {
                TypePool.Resolution resolution = typePool.describe(className);
                if (!resolution.isResolved()) {
                    log.debug("[JModel Enhance] Cannot resolve type: " + className);
                    continue;
                }

                TypeDescription typeDescription = resolution.resolve();

                // 跳过接口、注解、枚举
                if (typeDescription.isInterface() || typeDescription.isAnnotation() || typeDescription.isEnum()) {
                    continue;
                }

                // 检查是否为 Model 子类
                if (!isModelSubclass(typeDescription)) {
                    continue;
                }

                // 获取当前类声明的可追踪字段
                List<FieldDescription.InDefinedShape> trackableFields = getTrackableFields(typeDescription);
                if (trackableFields.isEmpty()) {
                    log.debug("[JModel Enhance] No trackable fields in: " + className);
                    continue;
                }

                // 增强类
                enhanceClass(typeDescription, trackableFields);
                count++;
            } catch (Exception e) {
                throw new MojoExecutionException("[JModel Enhance] Failed to enhance class: " + className, e);
            }
        }

        return count;
    }

    /**
     * 检查类型是否为 {@code io.github.biiiiiigmonster.Model} 的子类
     */
    private boolean isModelSubclass(TypeDescription type) {
        TypeDescription.Generic superClass = type.getSuperClass();
        while (superClass != null) {
            String superName = superClass.asErasure().getName();
            if (MODEL_CLASS_NAME.equals(superName)) {
                return true;
            }
            if ("java.lang.Object".equals(superName)) {
                return false;
            }
            try {
                superClass = superClass.asErasure().getSuperClass();
            } catch (Exception e) {
                log.debug("[JModel Enhance] Cannot resolve superclass of: " + superName);
                return false;
            }
        }
        return false;
    }

    /**
     * 获取类中所有可追踪字段（排除 static、transient、关系字段、计算属性字段）
     */
    private List<FieldDescription.InDefinedShape> getTrackableFields(TypeDescription type) {
        List<FieldDescription.InDefinedShape> trackable = new ArrayList<FieldDescription.InDefinedShape>();

        for (FieldDescription.InDefinedShape field : type.getDeclaredFields()) {
            if (field.isStatic()) {
                continue;
            }
            if (field.isTransient()) {
                continue;
            }
            if (hasRelationAnnotation(field)) {
                continue;
            }
            if (hasAttributeAnnotation(field)) {
                continue;
            }
            trackable.add(field);
        }

        return trackable;
    }

    /**
     * 检查字段是否带有关系注解（注解本身带有 @Relation 元注解）
     */
    private boolean hasRelationAnnotation(FieldDescription.InDefinedShape field) {
        AnnotationList annotations = field.getDeclaredAnnotations();
        for (AnnotationDescription annotation : annotations) {
            TypeDescription annotationType = annotation.getAnnotationType();
            try {
                AnnotationList metaAnnotations = annotationType.getDeclaredAnnotations();
                for (AnnotationDescription metaAnnotation : metaAnnotations) {
                    if (RELATION_META_ANNOTATION.equals(metaAnnotation.getAnnotationType().getName())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.debug("[JModel Enhance] Cannot resolve meta-annotations for: " + annotationType.getName());
            }
        }
        return false;
    }

    /**
     * 检查字段是否带有 @Attribute 注解
     */
    private boolean hasAttributeAnnotation(FieldDescription.InDefinedShape field) {
        AnnotationList annotations = field.getDeclaredAnnotations();
        for (AnnotationDescription annotation : annotations) {
            if (ATTRIBUTE_ANNOTATION.equals(annotation.getAnnotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 增强 Model 子类：为所有可追踪字段的 setter 注入追踪代码
     */
    private void enhanceClass(TypeDescription typeDescription,
                              List<FieldDescription.InDefinedShape> trackableFields) throws IOException {
        log.info("[JModel Enhance] Enhancing class: " + typeDescription.getName());

        AsmVisitorWrapper.ForDeclaredMethods visitorWrapper = new AsmVisitorWrapper.ForDeclaredMethods();
        boolean hasEnhancedSetter = false;

        for (FieldDescription.InDefinedShape field : trackableFields) {
            String fieldName = field.getName();
            String setterName = toSetterName(fieldName);
            String fieldDescriptor = field.getDescriptor();
            String fieldOwnerInternalName = typeDescription.getInternalName();

            // 检查类中是否声明了对应的 setter 方法
            boolean setterExists = false;
            for (MethodDescription.InDefinedShape method : typeDescription.getDeclaredMethods()) {
                if (method.getName().equals(setterName)
                    && method.getParameters().size() == 1
                    && !method.isAbstract()) {
                    setterExists = true;
                    break;
                }
            }

            if (!setterExists) {
                log.debug("[JModel Enhance]   No setter found: " + setterName + " for field: " + fieldName);
                continue;
            }

            log.info("[JModel Enhance]   Enhancing setter: " + setterName + "() -> tracking field '" + fieldName + "'");

            visitorWrapper = visitorWrapper.method(
                ElementMatchers.<MethodDescription>named(setterName)
                    .and(ElementMatchers.<MethodDescription>takesArguments(1)),
                new SetterInterceptor(fieldName, fieldDescriptor, fieldOwnerInternalName)
            );
            hasEnhancedSetter = true;
        }

        if (!hasEnhancedSetter) {
            log.debug("[JModel Enhance] No setters to enhance in: " + typeDescription.getName());
            return;
        }

        DynamicType.Builder<?> builder = new ByteBuddy()
            .redefine(typeDescription, classFileLocator)
            .visit(visitorWrapper);

        DynamicType.Unloaded<?> unloaded = builder.make();
        unloaded.saveIn(classesDirectory);
    }

    /**
     * 将字段名转换为 setter 方法名
     * <p>
     * 遵循 JavaBean 规范和 Lombok 默认行为：
     * {@code name} → {@code setName}，{@code email} → {@code setEmail}
     */
    private static String toSetterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * 递归扫描目录中的 .class 文件
     */
    private void scanClassFiles(File directory, List<File> result) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanClassFiles(file, result);
            } else if (file.getName().endsWith(".class")) {
                result.add(file);
            }
        }
    }

    /**
     * 从 .class 文件路径解析全限定类名
     */
    private String resolveClassName(File classFile) {
        String relativePath = classesDirectory.toURI().relativize(classFile.toURI()).getPath();
        if (!relativePath.endsWith(".class")) {
            return null;
        }
        return relativePath.substring(0, relativePath.length() - ".class".length())
            .replace('/', '.');
    }

    @Override
    public void close() throws IOException {
        classFileLocator.close();
        classLoader.close();
    }
}
