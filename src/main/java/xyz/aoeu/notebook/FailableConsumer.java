package xyz.aoeu.notebook;

import java.util.function.Consumer;

/**
 * Similar to a {@link Consumer} but able to throw an exception
 */
@FunctionalInterface
public interface FailableConsumer<I, E extends Exception> {

    /**
     * Consume a function
     *
     * @param input The input value
     * @throws E The type of error that may be thrown
     */
    void accept(I input) throws E;
}
