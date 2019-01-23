/*
 * Copyright (C) 2019 Société Générale.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
