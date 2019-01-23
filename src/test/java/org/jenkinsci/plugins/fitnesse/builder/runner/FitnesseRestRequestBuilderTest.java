package org.jenkinsci.plugins.fitnesse.builder.runner;

import java.net.MalformedURLException;
import java.net.URL;

import org.jenkinsci.plugins.fitnesse.builder.runner.FitnesseRestRequestBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import okhttp3.Request;

/**
 * {@link FitnesseRestRequestBuilder} tests
 * 
 */
public class FitnesseRestRequestBuilderTest
{

    /** Tested class */
    private FitnesseRestRequestBuilder requestBuilder;

    /** Instantiate a fresh builder for each test case */
    @Before
    public void setUp()
    {
        this.requestBuilder = new FitnesseRestRequestBuilder();
    }

    /**
     * Build a suite execution request from a valid URL
     * 
     * @throws MalformedURLException
     */
    @Test
    public void testBuildValidSuiteUrl() throws MalformedURLException
    {
        final String expectedHost = "foobar.com";
        final String expectedPage = "TestSuite";
        final int expectedPort = 8443;

        final URL hostUrl = new URL("https", expectedHost, expectedPort, expectedPage);

        final Request request = this.requestBuilder
                .withHostUrl(hostUrl)
                .withHtmlOutput()
                .withSuiteTarget()
                .withTargetPage(expectedPage)
                .build();

        // constant parameters
        Assert.assertEquals("GET", request.method());
        Assert.assertEquals("text/xml", request.header("Accept"));
        Assert.assertEquals("xml", request.url().queryParameter("format"));

        // additional parameters
        Assert.assertTrue(request.isHttps());
        Assert.assertEquals(expectedHost, request.url().host());
        Assert.assertEquals(expectedPort, request.url().port());
        Assert.assertEquals(expectedPage, request.url().pathSegments().get(0));
        Assert.assertTrue(request.url().queryParameterNames().contains("suite"));
        Assert.assertTrue(request.url().queryParameterNames().contains("includehtml"));
    }

    /**
     * Build a test execution request from a valid URL
     * 
     * @throws MalformedURLException
     */
    @Test
    public void testBuildValidTestUrl() throws MalformedURLException
    {
        final String expectedHost = "foobar.com";
        final String expectedPage = "TestPage";
        final int expectedPort = 8080;

        final URL hostUrl = new URL("http", expectedHost, expectedPort, expectedPage);

        final Request request = this.requestBuilder
                .withHostUrl(hostUrl)
                .withTargetPage('/' + expectedPage)
                .build();

        // constant parameters
        Assert.assertEquals("GET", request.method());
        Assert.assertEquals("text/xml", request.header("Accept"));
        Assert.assertEquals("xml", request.url().queryParameter("format"));

        // additional parameters
        Assert.assertFalse(request.isHttps());
        Assert.assertEquals(expectedHost, request.url().host());
        Assert.assertEquals(expectedPort, request.url().port());
        Assert.assertEquals(expectedPage, request.url().pathSegments().get(0));
        Assert.assertTrue(request.url().queryParameterNames().contains("test"));
        Assert.assertFalse(request.url().queryParameterNames().contains("includehtml"));
    }
}
