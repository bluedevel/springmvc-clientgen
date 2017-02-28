package com.bluedevel.smvcclientgen.core;

import com.bluedevel.smvcclientgen.Parameter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Robin Engel
 */
public class FunctionConfig {

    private String name;
    private String method;
    private String path;
    private String consumes;
    private String produces;
    private List<Parameter> parameters;

    public boolean hasParametersOfType(Parameter.Type type) {
        return getParametersByTypeAsStream(type)
                .findAny()
                .isPresent();
    }

    public List<String> getParametersByType(Parameter.Type type) {
        return getParametersByTypeAsStream(type)
                .collect(Collectors.toList());
    }

    private Stream<String> getParametersByTypeAsStream(Parameter.Type type) {
        return parameters.stream()
                .filter(p -> p.getType() == type)
                .map(Parameter::getName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConsumes() {
        return consumes;
    }

    public void setConsumes(String consumes) {
        this.consumes = consumes;
    }

    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}
