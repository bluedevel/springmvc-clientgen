package com.bluedevel.smvcclientgen;

/**
 * Describes a parameter on a resource handler.
 *
 * @author Robin Engel
 */
public class Parameter {

    public enum Type {
        PATH,
        QUERY
    }

    private String name;
    private Type type;

    public Parameter(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
