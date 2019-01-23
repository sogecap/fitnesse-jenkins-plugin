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
package org.jenkinsci.plugins.fitnesse.builder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.jenkinsci.plugins.fitnesse.builder.TestsExecutionCallable;
import org.jenkinsci.plugins.fitnesse.builder.FitnesseResultsBuilder.TargetType;
import org.jenkinsci.plugins.fitnesse.builder.runner.FitnessePageRunner;
import org.jenkinsci.plugins.fitnesse.builder.runner.FitnesseResponse;
import org.jenkinsci.plugins.fitnesse.builder.runner.exceptions.TestExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import hudson.AbortException;
import hudson.model.TaskListener;

/**
 * {@link TestsExecutionCallable} tests
 * 
 */
public class TestsExecutionCallableTest
{

    /** Enables the creation of temporary files/folders during tests */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Invoke the callable with an unknown target type
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testInvokeWithUnknownTargetType() throws IOException, InterruptedException
    {
        final File workspace = this.tempFolder.newFolder();

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(System.out);

        final FitnessePageRunner mockRunner = Mockito.mock(FitnessePageRunner.class);

        final TestsExecutionCallable callable = new TestsExecutionCallable(mockRunner, mockListener, TargetType.UNKNOWN.getName(), null, null, null);

        try
        {
            callable.invoke(workspace, null);
            Assert.fail("An exception should have been thrown");
        } catch (final AbortException e)
        {
            Assert.assertEquals(String.format("Unsupported target type \"%s\"", TargetType.UNKNOWN.getName()), e.getMessage());
        }
    }

    /**
     * Invoke the callable with an suite target type
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithSuiteTarget() throws IOException, InterruptedException
    {
        // given

        final FitnesseResponse expectedResponse = new FitnesseResponse("TestSuite", "foo");
        final CompletableFuture<FitnesseResponse> expectedFuture = CompletableFuture.completedFuture(expectedResponse);

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(System.out);

        final FitnessePageRunner mockRunner = Mockito.mock(FitnessePageRunner.class);
        Mockito.when(mockRunner.executeSuite(expectedResponse.getPage())).thenReturn(expectedFuture);

        final TestsExecutionCallable callable = new TestsExecutionCallable(mockRunner, mockListener, TargetType.SUITE.getName(), null, null, expectedResponse.getPage());

        // when

        final List<FitnesseResponse> responses = callable.invoke(null, null);

        // then

        Mockito.verify(mockRunner).executeSuite(expectedResponse.getPage());

        Assert.assertEquals(1, responses.size());

        final FitnesseResponse actualResponse = responses.get(0);

        Assert.assertEquals(expectedResponse.getPage(), actualResponse.getPage());
        Assert.assertEquals(expectedResponse.getContent(), actualResponse.getContent());
    }

    /**
     * Invoke the callable with a pages target type
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithPagesTarget() throws IOException, InterruptedException
    {
        // given

        final Map<String, FitnesseResponse> expectedResponses = new HashMap<>();
        expectedResponses.put("TestPageOne", new FitnesseResponse("TestPageOne", "foo"));
        expectedResponses.put("TestPageTwo", new FitnesseResponse("TestPageTwo", "bar"));
        expectedResponses.put("TestPageThree", new FitnesseResponse("TestPageThree", "baz"));

        final String targetPages = String.join("\n", expectedResponses.keySet());

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(System.out);

        final FitnessePageRunner mockRunner = Mockito.mock(FitnessePageRunner.class);

        for (final Entry<String, FitnesseResponse> entry : expectedResponses.entrySet())
        {
            Mockito.when(mockRunner.executePage(entry.getKey())).thenReturn(CompletableFuture.completedFuture(entry.getValue()));
        }

        final TestsExecutionCallable callable = new TestsExecutionCallable(mockRunner, mockListener, TargetType.PAGES.getName(), null, targetPages, null);

        // when

        final List<FitnesseResponse> responses = callable.invoke(null, null);

        // then

        for (final FitnesseResponse actualResponse : responses)
        {
            Mockito.verify(mockRunner).executePage(actualResponse.getPage());

            final FitnesseResponse expectedResponse = expectedResponses.get(actualResponse.getPage());

            Assert.assertNotNull(expectedResponse);
            Assert.assertEquals(expectedResponse.getPage(), actualResponse.getPage());
            Assert.assertEquals(expectedResponse.getContent(), actualResponse.getContent());
        }
    }

    /**
     * Invoke the callable with a pages target type, and some failed requests
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithPagesTargetAndFailedRequests() throws IOException, InterruptedException
    {
        // given

        final FitnesseResponse expectedResponse = new FitnesseResponse("TestPageOne", "foo");

        final String failurePage = "TestPageTwo";
        final String errorMessage = "I failed";
        final CompletableFuture<FitnesseResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new TestExecutionException(errorMessage));

        final String targetPages = String.join("\n", expectedResponse.getPage(), failurePage);

        final PrintStream mockLogger = Mockito.mock(PrintStream.class);
        Mockito.doNothing().when(mockLogger).println(errorMessage);

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(mockLogger);

        final FitnessePageRunner mockRunner = Mockito.mock(FitnessePageRunner.class);
        Mockito.when(mockRunner.executePage(expectedResponse.getPage())).thenReturn(CompletableFuture.completedFuture(expectedResponse));
        Mockito.when(mockRunner.executePage(failurePage)).thenReturn(failedFuture);

        final TestsExecutionCallable callable = new TestsExecutionCallable(mockRunner, mockListener, TargetType.PAGES.getName(), null, targetPages, null);

        // when

        final List<FitnesseResponse> responses = callable.invoke(null, null);

        // then

        Mockito.verify(mockRunner).executePage(expectedResponse.getPage());
        Mockito.verify(mockRunner).executePage(failurePage);
        Mockito.verify(mockLogger).println(errorMessage);

        Assert.assertEquals(1, responses.size());

        final FitnesseResponse actualResponse = responses.get(0);

        Assert.assertEquals(expectedResponse.getPage(), actualResponse.getPage());
        Assert.assertEquals(expectedResponse.getContent(), actualResponse.getContent());
    }

    /**
     * Invoke the callable with a text file target type
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithTextFileTarget() throws IOException, InterruptedException
    {
        // given

        final Map<String, FitnesseResponse> expectedResponses = new HashMap<>();
        expectedResponses.put("TestPageOne", new FitnesseResponse("TestPageOne", "foo"));
        expectedResponses.put("TestPageTwo", new FitnesseResponse("TestPageTwo", "bar"));
        expectedResponses.put("TestPageThree", new FitnesseResponse("TestPageThree", "baz"));

        final File workspace = this.tempFolder.newFolder();
        final Path targetFile = workspace.toPath().resolve("pages.txt");

        Files.write(targetFile, expectedResponses.keySet());

        final TaskListener mockListener = Mockito.mock(TaskListener.class);
        Mockito.when(mockListener.getLogger()).thenReturn(System.out);

        final FitnessePageRunner mockRunner = Mockito.mock(FitnessePageRunner.class);

        for (final Entry<String, FitnesseResponse> entry : expectedResponses.entrySet())
        {
            Mockito.when(mockRunner.executePage(entry.getKey())).thenReturn(CompletableFuture.completedFuture(entry.getValue()));
        }

        final TestsExecutionCallable callable = new TestsExecutionCallable(mockRunner, mockListener, TargetType.TEXT_FILE.getName(), targetFile.toString(), null, null);

        // when

        final List<FitnesseResponse> responses = callable.invoke(workspace, null);

        // then

        for (final FitnesseResponse actualResponse : responses)
        {
            Mockito.verify(mockRunner).executePage(actualResponse.getPage());

            final FitnesseResponse expectedResponse = expectedResponses.get(actualResponse.getPage());

            Assert.assertNotNull(expectedResponse);
            Assert.assertEquals(expectedResponse.getPage(), actualResponse.getPage());
            Assert.assertEquals(expectedResponse.getContent(), actualResponse.getContent());
        }
    }
}
