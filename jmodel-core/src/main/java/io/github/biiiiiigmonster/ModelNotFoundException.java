package io.github.biiiiiigmonster;

public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(Class<?> clazz) {
        super(String.format("No query results for model [%s].", clazz.getName()));
    }
}
