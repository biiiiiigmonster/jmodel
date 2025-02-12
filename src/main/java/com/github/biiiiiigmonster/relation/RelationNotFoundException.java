package com.github.biiiiiigmonster.relation;

public class RelationNotFoundException extends RuntimeException {
    public RelationNotFoundException(Class<?> clazz, String fieldName) {
        super(String.format("Call to undefined relationship [%s] on model [%s].", clazz.getName(), fieldName));
    }
}
