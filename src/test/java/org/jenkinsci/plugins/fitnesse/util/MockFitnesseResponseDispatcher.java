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

import java.util.Map;

import org.junit.Assert;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * {@link Dispatcher} implementation in charge of providing a
 * predefined set of HTTP responses based upon a provided route
 * 
 */
public class MockFitnesseResponseDispatcher extends Dispatcher
{

    private final Map<String, String> mockResponses;

    /**
     * Initializes a new dispatcher
     * @param mockResponses target page -> expected content mapping
     */
    public MockFitnesseResponseDispatcher(final Map<String, String> mockResponses)
    {
        this.mockResponses = mockResponses;
    }

    /** {@inheritDoc} */
    @Override
    public MockResponse dispatch(final RecordedRequest request) throws InterruptedException
    {
        final String requestPath = request.getPath();
        final String targetedPage = requestPath.substring(1, requestPath.indexOf('?'));

        final String mockResponse = this.mockResponses.getOrDefault(targetedPage, "");

        Assert.assertNotNull("Unexpected request " + request.toString(), mockResponse);

        return new MockResponse()
                .addHeader("Content-Type", "text/xml")
                .setBody(mockResponse);
    }
}