package com.github.bluedevel.maven.plugins.smvcclientgen.mojo;

import org.apache.maven.plugin.AbstractMojoExecutionException;

/**
 * Provides a method of throwing and handling expected exceptions inside streams.
 *
 * @author Robin Engel
 */
public class StreamExceptions {

    /**
     * Wrap a exception inside a {@link RuntimeException} so that it can be thrown out of a stream
     * as an unchecked exception.
     */
    public static void throwSilent(AbstractMojoExecutionException e) {
        throw new RuntimeException("This exception serves only as a wrapper. You shouldn't ever see it!", e);
    }

    /**
     * Take a {@link RuntimeException} and unwrap the cause if it's one of the expected exceptions.<br>
     * This is used for catching expected exceptions out of a stream, which are wrapped in an unexpected exception.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void handle(RuntimeException e, Class<? extends E> clazz) throws E {
        Throwable cause = e.getCause();

        if (clazz.isInstance(cause)) {
            throw (E) cause;
        }
    }

}
