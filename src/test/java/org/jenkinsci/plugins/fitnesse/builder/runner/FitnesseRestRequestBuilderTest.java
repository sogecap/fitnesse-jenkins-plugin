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
