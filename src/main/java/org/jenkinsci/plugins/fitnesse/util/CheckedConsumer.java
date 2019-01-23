package org.jenkinsci.plugins.fitnesse.util;

import java.util.function.Consumer;

/**
 * To avoid ugly try/catch blocks in consumers
 * 
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface CheckedConsumer<T> extends Consumer<T>
{

    /** {@inheritDoc} */
    @Override
    default void accept(final T t)
    {
        try
        {
            this.throwingAccept(t);
        } catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs this potentially throwing operation on the given argument.
     *
     * @param t the input argument
     * @throws Exception if accepting the input throws
     */
    void throwingAccept(T t) throws Exception;
}
