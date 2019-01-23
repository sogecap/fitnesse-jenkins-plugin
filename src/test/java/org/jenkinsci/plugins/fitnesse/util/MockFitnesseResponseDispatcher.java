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