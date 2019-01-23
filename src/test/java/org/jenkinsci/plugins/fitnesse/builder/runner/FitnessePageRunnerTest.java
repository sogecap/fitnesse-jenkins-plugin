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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jenkinsci.plugins.fitnesse.builder.runner.FitnessePageRunner;
import org.jenkinsci.plugins.fitnesse.builder.runner.FitnesseResponse;
import org.jenkinsci.plugins.fitnesse.builder.runner.exceptions.TestExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import hudson.model.TaskListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

/**
 * Tests de la classe {@link FitnessePageRunner}
 * 
 */
public class FitnessePageRunnerTest
{

    /** Local web server which allows to mock a remote FitNesse instance */
    private MockWebServer mockServer;

    /**
     * Local web server instantiation & startup
     * 
     * @throws IOException
     */
    @Before
    public void setUp() throws IOException
    {
        this.mockServer = new MockWebServer();
        this.mockServer.start();
    }

    /**
     * Local web server shutdown
     * 
     * @throws IOException
     */
    @After
    public void tearDown() throws IOException
    {
        this.mockServer.shutdown();
    }

    /**
     * Nominal FitNesse suite execution
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testExecuteSuite() throws IOException, InterruptedException
    {
        final URL hostUrl = this.getMockServerUrl(this.mockServer);
        final String targetSuite = "TestSuite";
        final boolean captureHtmlOutput = false;

        // mock HTTP responses

        final String mockResponse = "test";

        this.mockServer.enqueue(new MockResponse().setBody(mockResponse));

        // suite runner instantiation

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(Mockito.mock(PrintStream.class));

        final FitnessePageRunner testRunner = new FitnessePageRunner(
                hostUrl,
                0,
                captureHtmlOutput,
                mockListener);

        // suite execution

        final FitnesseResponse response = testRunner.executeSuite(targetSuite).join();

        Assert.assertEquals(targetSuite, response.getPage());
        Assert.assertEquals(mockResponse, response.getContent());
    }

    /**
     * FitNesse suite execution that triggers an HTTP timeout
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testExecuteSuiteWithTimeout() throws IOException, InterruptedException
    {
        final URL hostUrl = this.getMockServerUrl(this.mockServer);
        final String targetSuite = "TestSuite";
        final boolean captureHtmlOutput = false;
        final int httpTimeout = 1;

        // mock HTTP responses

        final String mockResponse = "test";

        this.mockServer.enqueue(
                new MockResponse()
                .setBody(mockResponse)
                .setSocketPolicy(SocketPolicy.NO_RESPONSE));

        // suite runner instantiation

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(Mockito.mock(PrintStream.class));

        final FitnessePageRunner testRunner = new FitnessePageRunner(
                hostUrl,
                httpTimeout,
                captureHtmlOutput,
                mockListener);

        // suite execution

        try
        {
            testRunner.executeSuite(targetSuite).join();
            Assert.fail("An exception should have been thrown");
        } catch (final Exception e)
        {
            Assert.assertTrue(e.getCause() instanceof TestExecutionException);
        }
    }

    /**
     * Asynchronous FitNesse page execution
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testExecutePage() throws IOException, InterruptedException
    {
        final URL hostUrl = this.getMockServerUrl(this.mockServer);
        final String targetPage = "TestPage";
        final boolean captureHtmlOutput = false;

        // mock HTTP responses

        final String mockHttpResponseBody = "test";

        this.mockServer.enqueue(new MockResponse().setBody(mockHttpResponseBody));

        // page runner instantiation

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(Mockito.mock(PrintStream.class));

        final FitnessePageRunner testRunner = new FitnessePageRunner(
                hostUrl,
                0,
                captureHtmlOutput,
                mockListener);

        // page execution

        final FitnesseResponse actualResponse = testRunner.executePage(targetPage).join();

        Assert.assertNotNull(actualResponse);
        Assert.assertEquals(targetPage, actualResponse.getPage());
        Assert.assertEquals(mockHttpResponseBody, actualResponse.getContent());
    }

    /**
     * Asynchronous FitNesse page execution that produces an incorrect HTTP status code (!= [2xx..3xx])
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testExecutePageWithIncorrectHttpStatus() throws IOException, InterruptedException
    {
        final URL hostUrl = this.getMockServerUrl(this.mockServer);
        final String targetPage = "TestPage";
        final boolean captureHtmlOutput = false;

        // mock HTTP responses

        this.mockServer.enqueue(new MockResponse().setResponseCode(500));

        // page runner instantiation

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(Mockito.mock(PrintStream.class));

        final FitnessePageRunner testRunner = new FitnessePageRunner(
                hostUrl,
                0,
                captureHtmlOutput,
                mockListener);

        // page execution

        try
        {
            testRunner.executePage(targetPage).join();

        } catch (final Exception e)
        {
            Assert.assertTrue(e.getCause() instanceof TestExecutionException);
        }
    }

    /**
     * Asynchronous FitNesse page execution that triggers an HTTP timeout
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testExecutePageWithTimeout() throws IOException, InterruptedException
    {
        final URL hostUrl = this.getMockServerUrl(this.mockServer);
        final String targetPage = "TestPage";
        final boolean captureHtmlOutput = false;
        final int httpTimeout = 1;

        // mock HTTP responses

        this.mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        // page runner instantiation

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(Mockito.mock(PrintStream.class));

        final FitnessePageRunner testRunner = new FitnessePageRunner(
                hostUrl,
                httpTimeout,
                captureHtmlOutput,
                mockListener);

        // page execution

        try 
        {
            testRunner.executePage(targetPage).join();
            Assert.fail("An exception should have been thrown");
        } catch (final Exception e)
        {
            Assert.assertTrue(e.getCause() instanceof TestExecutionException);
        }
    }

    private URL getMockServerUrl(final MockWebServer mockServer)
    {
        try
        {
            return new URL("http", mockServer.getHostName(), mockServer.getPort(), "");
        } catch (final MalformedURLException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
