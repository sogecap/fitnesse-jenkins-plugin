package org.jenkinsci.plugins.fitnesse.util;

import java.util.function.Predicate;

/**
 * To avoid ugly try/catch blocks in predicates
 * 
 * @param <T> the type of the input to the predicate
 */
@FunctionalInterface
public interface CheckedPredicate<T> extends Predicate<T>
{

    /** {@inheritDoc} */
    @Override
    default boolean test(final T t)
    {
        try
        {
            return this.throwingTest(t);
        } catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Evaluates this potentially throwing predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate,
     *         otherwise {@code false}
     * @throws Exception if testing the input throws
     */
    boolean throwingTest(T t) throws Exception;
}
