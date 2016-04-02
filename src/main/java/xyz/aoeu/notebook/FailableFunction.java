package xyz.aoeu.notebook;

/**
 * A function that can throw an exception
 */
public interface FailableFunction<I, O, E extends Exception> {

    /**
     * Call a function
     *
     * @param input The input value
     * @return The return value
     * @throws E The type of error that may be thrown
     */
    O apply(I input) throws E;
}
