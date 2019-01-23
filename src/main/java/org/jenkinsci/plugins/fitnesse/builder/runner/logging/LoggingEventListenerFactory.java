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
package org.jenkinsci.plugins.fitnesse.builder.runner.logging;

import java.io.PrintStream;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.EventListener.Factory;

/**
 * Factory which instantiates {@link LoggingEventListener} from OkHttp {@link Call}s
 * 
 */
public class LoggingEventListenerFactory implements Factory
{

    private final PrintStream logger;

    /**
     * Constructor
     * 
     * @param logger Jenkins logger
     */
    public LoggingEventListenerFactory(final PrintStream logger)
    {
        this.logger = logger;
    }

    /** {@inheritDoc} */
    @Override
    public EventListener create(final Call call)
    {
        // remove leading forward slash
        final String targetedPage = call.request().url().url().getPath().substring(1);
        return new LoggingEventListener(targetedPage, this.logger);
    }
}
