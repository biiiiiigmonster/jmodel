package io.github.biiiiiigmonster.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Annotation processor that generates static metamodel classes for Model entities.
 * <p>
 * This processor discovers all classes that extend {@code Model<T>} and generates
 * corresponding metamodel classes with static {@code SingularAttribute} fields
 * for each persistent field in the entity.
 * <p>
 * The generated metamodel classes follow the naming convention of appending "_"
 * to the entity class name (e.g., {@code User} → {@code User_}).
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JmodelProcessor extends AbstractProcessor {

    private static final String MODEL_CLASS_NAME = "io.github.biiiiiigmonster.Model";

    private Filer filer;
    private Messager messager;
    private MetamodelGenerator generator;
    private Set<String> processedEntities;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.generator = new MetamodelGenerator();
        this.processedEntities = new HashSet<>();
    }

    /**
     * Processes all types and finds Model subclasses to generate metamodels.
     *
     * @param annotations the annotation types requested to be processed
     * @param roundEnv    environment for information about the current and prior round
     * @return false to allow other processors to process these annotations
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        // Process all root elements to find Model subclasses
        for (Element element : roundEnv.getRootElements()) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                
                if (isModelSubclass(typeElement)) {
                    String qualifiedName = typeElement.getQualifiedName().toString();
                    
                    // Avoid processing the same entity multiple times
                    if (!processedEntities.contains(qualifiedName)) {
                        processedEntities.add(qualifiedName);
                        generateMetamodel(typeElement);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if a type extends Model.
     * <p>
     * This method traverses the type hierarchy to determine if the given type
     * is a subclass of {@code io.github.biiiiiigmonster.Model}.
     *
     * @param typeElement the type element to check
     * @return true if the type extends Model, false otherwise
     */
    private boolean isModelSubclass(TypeElement typeElement) {
        TypeMirror superclass = typeElement.getSuperclass();
        
        while (superclass != null && superclass.getKind() != TypeKind.NONE) {
            if (superclass.getKind() == TypeKind.DECLARED) {
                DeclaredType declaredType = (DeclaredType) superclass;
                TypeElement superElement = (TypeElement) declaredType.asElement();
                String superClassName = superElement.getQualifiedName().toString();
                
                if (MODEL_CLASS_NAME.equals(superClassName)) {
                    return true;
                }
                
                // Continue up the hierarchy
                superclass = superElement.getSuperclass();
            } else {
                break;
            }
        }
        
        return false;
    }

    /**
     * Generates metamodel for a single entity.
     * <p>
     * Creates a new source file with the metamodel class containing static
     * {@code SingularAttribute} fields for each persistent field in the entity.
     *
     * @param entityElement the entity type element to generate metamodel for
     */
    private void generateMetamodel(TypeElement entityElement) {
        try {
            String metamodelClassName = generator.getMetamodelClassName(entityElement);
            String sourceCode = generator.generate(entityElement, processingEnv);
            
            JavaFileObject sourceFile = filer.createSourceFile(metamodelClassName, entityElement);
            
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(sourceCode);
            }
            
            messager.printMessage(Diagnostic.Kind.NOTE, 
                    "Generated metamodel: " + metamodelClassName, entityElement);
            
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate metamodel for entity '" + 
                    entityElement.getQualifiedName() + "': " + e.getMessage(),
                    entityElement);
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Unexpected error generating metamodel for entity '" +
                    entityElement.getQualifiedName() + "': " + e.getMessage(),
                    entityElement);
        }
    }
}
