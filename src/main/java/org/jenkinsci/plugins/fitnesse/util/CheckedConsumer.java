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
