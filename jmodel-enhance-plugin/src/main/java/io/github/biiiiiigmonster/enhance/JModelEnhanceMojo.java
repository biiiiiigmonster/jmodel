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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * JModel Enhancement Maven Plugin
 * <p>
 * This plugin enhances Model subclasses with dirty-tracking support by
 * injecting tracking code into setter methods using ByteBuddy.
 * <p>
 * The enhancement occurs during the process-classes phase, after Lombok
 * has generated the setter methods.
 * <p>
 * Usage in pom.xml:
 * <pre>{@code
 * <plugin>
 *     <groupId>io.github.biiiiiigmonster</groupId>
 *     <artifactId>jmodel-enhance-plugin</artifactId>
 *     <version>${jmodel.version}</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>enhance</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 * </plugin>
 * }</pre>
 *
 * @author luyunfeng
 */
@Mojo(
        name = "enhance",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class JModelEnhanceMojo extends AbstractMojo {

    /**
     * The Maven project reference
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The directory containing compiled classes to be enhanced
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    /**
     * Whether to enable verbose logging
     */
    @Parameter(property = "jmodel.enhance.verbose", defaultValue = "false")
    private boolean verbose;

    /**
     * Whether to skip enhancement
     */
    @Parameter(property = "jmodel.enhance.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Package patterns to include for enhancement (supports wildcards)
     * If not specified, all packages will be scanned
     */
    @Parameter(property = "jmodel.enhance.includes")
    private List<String> includes;

    /**
     * Package patterns to exclude from enhancement
     */
    @Parameter(property = "jmodel.enhance.excludes")
    private List<String> excludes;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("[JModel Enhance] Skipping enhancement (skip=true)");
            return;
        }

        if (!classesDirectory.exists()) {
            getLog().info("[JModel Enhance] Classes directory does not exist, skipping: " + classesDirectory);
            return;
        }

        getLog().info("[JModel Enhance] Starting bytecode enhancement...");
        getLog().info("[JModel Enhance] Classes directory: " + classesDirectory.getAbsolutePath());

        try {
            // Create ClassLoader with project dependencies
            ClassLoader projectClassLoader = createProjectClassLoader();

            // Create and run the enhancer
            ModelClassEnhancer enhancer = new ModelClassEnhancer(
                    classesDirectory,
                    projectClassLoader,
                    getLog(),
                    verbose
            );

            // Set include/exclude patterns
            if (includes != null && !includes.isEmpty()) {
                enhancer.setIncludePatterns(includes);
            }
            if (excludes != null && !excludes.isEmpty()) {
                enhancer.setExcludePatterns(excludes);
            }

            // Execute enhancement
            EnhancementResult result = enhancer.enhance();

            // Log results
            getLog().info("[JModel Enhance] Enhancement completed!");
            getLog().info("[JModel Enhance] Classes scanned: " + result.getScannedCount());
            getLog().info("[JModel Enhance] Classes enhanced: " + result.getEnhancedCount());
            getLog().info("[JModel Enhance] Setters enhanced: " + result.getSettersEnhancedCount());

            if (result.getEnhancedCount() > 0 && verbose) {
                getLog().info("[JModel Enhance] Enhanced classes:");
                for (String className : result.getEnhancedClasses()) {
                    getLog().info("  - " + className);
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("[JModel Enhance] Enhancement failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a ClassLoader that includes the project's compiled classes
     * and all test-scope dependencies.
     */
    private ClassLoader createProjectClassLoader() throws MojoExecutionException {
        try {
            List<URL> urls = new ArrayList<>();

            // Add compiled classes directory
            urls.add(classesDirectory.toURI().toURL());

            // Add test classpath elements (includes compile dependencies + test dependencies)
            List<String> classpathElements = project.getTestClasspathElements();
            for (String element : classpathElements) {
                File elementFile = new File(element);
                if (elementFile.exists()) {
                    urls.add(elementFile.toURI().toURL());
                }
            }

            if (verbose) {
                getLog().info("[JModel Enhance] ClassLoader URLs count: " + urls.size());
                for (URL url : urls) {
                    getLog().debug("[JModel Enhance] ClassLoader URL: " + url);
                }
            }

            return new URLClassLoader(
                    urls.toArray(new URL[0]),
                    Thread.currentThread().getContextClassLoader()
            );

        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Failed to create ClassLoader: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to resolve classpath: " + e.getMessage(), e);
        }
    }
}
