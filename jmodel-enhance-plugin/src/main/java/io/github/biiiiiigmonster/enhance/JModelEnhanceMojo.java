package io.github.biiiiiigmonster.enhance;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * JModel 字节码增强 Maven 插件入口。
 * <p>
 * 在 {@code process-classes} 阶段执行，扫描所有 {@code Model} 子类，
 * 对其 setter 方法注入 {@code $jmodel$trackChange} 调用，实现 dirty-tracking。
 *
 * @author luyunfeng
 */
@Mojo(
    name = "enhance",
    defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    requiresDependencyResolution = ResolutionScope.COMPILE,
    threadSafe = true
)
public class JModelEnhanceMojo extends AbstractMojo {

    /**
     * 编译输出目录（默认 target/classes）。
     * <p>
     * 可在 Maven 配置中覆盖此参数，例如设置为 {@code ${project.build.testOutputDirectory}}
     * 以增强测试类。
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    /**
     * Maven 项目对象（用于获取编译期类路径）
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!classesDirectory.exists()) {
            getLog().info("[JModel Enhance] Classes directory not found, skipping enhancement.");
            return;
        }

        getLog().info("[JModel Enhance] Scanning " + classesDirectory.getAbsolutePath());

        ModelClassEnhancer enhancer = null;
        try {
            enhancer = new ModelClassEnhancer(
                classesDirectory,
                project.getCompileClasspathElements(),
                getLog()
            );

            int count = enhancer.enhance();
            getLog().info("[JModel Enhance] Complete: " + count + " class(es) enhanced.");
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("[JModel Enhance] Enhancement failed", e);
        } finally {
            if (enhancer != null) {
                try {
                    enhancer.close();
                } catch (Exception e) {
                    getLog().warn("[JModel Enhance] Failed to close enhancer resources", e);
                }
            }
        }
    }
}
