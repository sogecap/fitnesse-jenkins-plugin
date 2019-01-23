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
package org.jenkinsci.plugins.fitnesse.builder.runner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.fitnesse.builder.runner.logging.LoggingEventListenerFactory;

import hudson.model.TaskListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Asynchronously executes FitNesse pages on a remote FitNesse server via the REST API
 * 
 */
public class FitnessePageRunner implements Serializable
{

    private static final long serialVersionUID = -4160591890313113976L;

    private final URL hostUrl;

    private final boolean includeHtmlOutput;

    private final int httpTimeout;

    private final TaskListener listener;

    private transient OkHttpClient httpClient;

    /**
     * Constructor
     * 
     * @param hostUrl URL of the remote FitNesse host
     * @param httpTimeout HTTP timeout
     * @param includeHtmlOutput whether to include the HTML test output in the responses
     * @param listener build listener (for logging)
     */
    public FitnessePageRunner(final URL hostUrl, final int httpTimeout, final boolean includeHtmlOutput, final TaskListener listener)
    {
        this.hostUrl = hostUrl;
        this.includeHtmlOutput = includeHtmlOutput;
        this.httpTimeout = httpTimeout;
        this.listener = listener;
        this.httpClient = this.createHttpClient();
    }

    /**
     * Executes a given FitNesse page asynchronously
     * 
     * @param targetPage name of the targeted page
     * @return the result of the asychronous page execution
     */
    public CompletableFuture<FitnesseResponse> executePage(final String targetPage)
    {
        final FitnesseRestRequestBuilder requestBuilder = new FitnesseRestRequestBuilder()
                .withHostUrl(this.hostUrl)
                .withTargetPage(targetPage);

        if (this.includeHtmlOutput)
        {
            requestBuilder.withHtmlOutput();
        }

        return this.scheduleRequest(targetPage, requestBuilder.build());
    }

    /**
     * Executes a given FitNesse suite asynchronously
     * 
     * @param targetPage name of the targeted page
     * @return the result of the asychronous suite execution
     */
    public CompletableFuture<FitnesseResponse> executeSuite(final String targetPage)
    {
        final FitnesseRestRequestBuilder requestBuilder = new FitnesseRestRequestBuilder()
                .withHostUrl(this.hostUrl)
                .withTargetPage(targetPage)
                .withSuiteTarget();

        if (this.includeHtmlOutput)
        {
            requestBuilder.withHtmlOutput();
        }

        return this.scheduleRequest(targetPage, requestBuilder.build());
    }

    private CompletableFuture<FitnesseResponse> scheduleRequest(final String targetPage, final Request request)
    {
        final FitnesseResponseFuture future = new FitnesseResponseFuture(targetPage);

        this.httpClient.newCall(request).enqueue(future);

        // log the failed responses and null them out
        return future.getFuture();
    }

    private OkHttpClient createHttpClient()
    {
        return new OkHttpClient().newBuilder()
                .readTimeout(this.httpTimeout, TimeUnit.SECONDS)
                .eventListenerFactory(new LoggingEventListenerFactory(this.listener.getLogger()))
                .build();
    }

    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException
    {
        in.defaultReadObject();
        this.httpClient = this.createHttpClient();
    }
}
