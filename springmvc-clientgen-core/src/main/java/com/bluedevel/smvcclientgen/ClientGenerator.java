package com.bluedevel.smvcclientgen;

/**
 * @author Robin Engel
 */
public interface ClientGenerator {
    String render(ClientGeneratorConfiguration config) throws Exception;
}
