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

import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Request;

/**
 * Builds OkHttp {@link Request}s which target the FitNesse REST API from a defined set of parameters
 * 
 */
public final class FitnesseRestRequestBuilder
{

    private URL hostUrl;

    private boolean targetsSuite;

    private String targetPage;

    private boolean includeHtmlOutput;

    /**
     * Sets the remote FitNesse host URL
     * 
     * @param hostUrl remote FitNesse host URL
     * @return this builder
     */
    public FitnesseRestRequestBuilder withHostUrl(final URL hostUrl)
    {
        this.hostUrl = hostUrl;

        return this;
    }

    /**
     * Whether the target page is a Fitnesse suite
     * 
     * @return this builder
     */
    public FitnesseRestRequestBuilder withSuiteTarget()
    {
        this.targetsSuite = true;

        return this;
    }

    /**
     * Set the targeted FitNesse page
     * 
     * @param targetPage the targeted FitNesse page
     * @return this builder
     */
    public FitnesseRestRequestBuilder withTargetPage(final String targetPage)
    {
        this.targetPage = targetPage;

        return this;
    }

    /**
     * Whether to include the test HTML output in the response
     * 
     * @return this builder
     */
    public FitnesseRestRequestBuilder withHtmlOutput()
    {
        this.includeHtmlOutput = true;

        return this;
    }

    /**
     * Builds the OkHttp request corresponding to the configured parameters
     * 
     * @return the resulting {@link Request} which targets the FitNesse REST API
     */
    public Request build()
    {
        final StringBuilder queryStringBuilder = new StringBuilder();
        queryStringBuilder.append('?');
        queryStringBuilder.append(this.targetsSuite ? "suite" : "test");
        queryStringBuilder.append(this.includeHtmlOutput ? "&includehtml" : "");
        queryStringBuilder.append("&format=xml");
        queryStringBuilder.append("&nochunk");

        URL remoteFitnesseUrl;

        try
        {
            final String path = (this.targetPage.startsWith("/") ? this.targetPage : ('/' + this.targetPage));
            remoteFitnesseUrl = new URL(this.hostUrl.getProtocol(), this.hostUrl.getHost(), this.hostUrl.getPort(), path + queryStringBuilder.toString());
        } catch (final MalformedURLException e)
        {
            throw new IllegalArgumentException("Could not build FitNesse REST URI", e);
        }

        return new Request.Builder()
                .url(remoteFitnesseUrl)
                .addHeader("Accept", "text/xml")
                .get()
                .build();
    }
}
