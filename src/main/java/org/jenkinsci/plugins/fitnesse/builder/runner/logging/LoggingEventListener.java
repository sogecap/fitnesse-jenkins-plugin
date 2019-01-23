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

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Interceptor;

/**
 * OkHttp {@link Interceptor} which enables additional logging for API calls
 * 
 * <p>
 * This class logs the success or failure of HTTP calls from a transport layer
 * point of view; It does not handle, for instance, unexpected HTTP status codes.
 * 
 */
public class LoggingEventListener extends EventListener
{

    private final String targetedPage;

    private final PrintStream logger;

    private long callStart;

    /**
     * Initializes a new listener
     * 
     * @param targetedPage Targeted FitNesse page
     * @param logger Jenkins logger
     */
    public LoggingEventListener(final String targetedPage, final PrintStream logger)
    {
        this.targetedPage = targetedPage;
        this.logger = logger;
    }

    /** {@inheritDoc} */
    @Override
    public void connectStart(final Call call, final InetSocketAddress inetSocketAddress, final Proxy proxy)
    {
        // we react to the "connectStart" event because "callStart" is executed as soon as an asynchronous
        // request is enqueued, which is not when the remote FitNesse host really begins to execute our request
        this.callStart = System.currentTimeMillis();
        this.logger.printf(">> Running page \"%s\"...%n", this.targetedPage);
    }

    /** {@inheritDoc} */
    @Override
    public void callFailed(final Call call, final IOException ioe)
    {
        this.logger.printf("<< Failed to execute page \"%s\": %s%n", this.targetedPage, ioe.getMessage());
    }

    /** {@inheritDoc} */
    @Override
    public void callEnd(final Call call)
    {
        final long requestDuration = System.currentTimeMillis() - this.callStart;
        final long minutesElapsed = TimeUnit.MILLISECONDS.toMinutes(requestDuration);
        final long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(requestDuration) - TimeUnit.MINUTES.toSeconds(minutesElapsed);
        final long milliSecondsElapsed = requestDuration - (TimeUnit.MINUTES.toMillis(minutesElapsed) + TimeUnit.SECONDS.toMillis(secondsElapsed));

        this.logger.printf(
                "<< Execution of FitNesse page \"%s\" took %02d:%02d.%03d%n",
                this.targetedPage,
                minutesElapsed,
                secondsElapsed,
                milliSecondsElapsed);
    }
}
