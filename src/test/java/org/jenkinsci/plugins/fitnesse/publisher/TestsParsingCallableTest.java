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
package org.jenkinsci.plugins.fitnesse.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jenkinsci.plugins.fitnesse.publisher.TestsParsingCallable;
import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnessePageResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.TaskListener;

/**
 * {@link TestsParsingCallableTest} tests
 * 
 */
public class TestsParsingCallableTest
{

    /** Enables the creation of temporary files/folders during tests */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Invoke the callable with no FitNesse report files in the workspace
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithNoFitnesseReportFiles() throws IOException, InterruptedException
    {
        final File workspace = this.tempFolder.newFolder();

        final TestsParsingCallable callable = new TestsParsingCallable("*", 0, 0, null, false, null);

        try
        {
            callable.invoke(workspace, null);
            Assert.fail("An exception should have been thrown");
        } catch (final AbortException e)
        {
            Assert.assertEquals("No FitNesse report files found", e.getMessage());
        }
    }

    /**
     * Invoke the callable with only stale FitNesse report files in the workspace
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithOnlyStaleFitnesseReportFiles() throws IOException, InterruptedException
    {
        final File workspace = this.tempFolder.newFolder();
        final File reportFile = Files.createFile(workspace.toPath().resolve("test-result.xml")).toFile();

        // backdate the last modified time of the report file
        // enough for it to be considered stale, i.e. < System.currentTimeMillis()
        // when the callable is invoked
        final long now = System.currentTimeMillis();
        reportFile.setLastModified(now - 10000);

        final TestsParsingCallable callable = new TestsParsingCallable("*-result.xml", now, now, null, false, null);

        try
        {
            callable.invoke(workspace, null);
            Assert.fail("An exception should have been thrown");
        } catch (final AbortException e)
        {
            Assert.assertEquals("FitNesse report files were found but none of them are new. Did tests run?", e.getMessage());
        }
    }

    /**
     * Invoke the callable with valid FitNesse report files in the workspace
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithValidFitnesseReportFiles() throws IOException, InterruptedException
    {
        final File workspace = this.tempFolder.newFolder();
        final File htmlOutputDirectory = new File(workspace, "htmloutput");
        final String name = "successful-test-results.xml";

        Files.copy(this.getClass().getResourceAsStream(name), workspace.toPath().resolve(name));

        final TaskListener mockListener = Mockito.mock(TaskListener.class);

        Mockito.when(mockListener.getLogger()).thenReturn(System.out);

        final TestsParsingCallable callable = new TestsParsingCallable("*-test-results.xml", 0, 0, new FilePath(htmlOutputDirectory), false, mockListener);

        final AggregatedFitnesseResult topLevelResult = callable.invoke(workspace, null);

        Assert.assertNotNull(topLevelResult);

        Assert.assertEquals(10, topLevelResult.getPassCount());
        Assert.assertEquals(0, topLevelResult.getFailOnlyCount());
        Assert.assertEquals(2, topLevelResult.getSkipCount());
        Assert.assertEquals(0, topLevelResult.getExceptionCount());
        Assert.assertEquals(3.951, topLevelResult.getDuration(), 0.0001);

        Assert.assertEquals(1, topLevelResult.getTotalPages());
        Assert.assertEquals(1, topLevelResult.getPassedPages());
        Assert.assertEquals(0, topLevelResult.getSkippedPages());
        Assert.assertEquals(0, topLevelResult.getFailedPages());

        Assert.assertEquals(1, topLevelResult.getChildren().size());

        final FitnessePageResult child = (FitnessePageResult) topLevelResult.getChildren().iterator().next();

        Assert.assertEquals(10, child.getPassCount());
        Assert.assertEquals(0, child.getFailOnlyCount());
        Assert.assertEquals(2, child.getSkipCount());
        Assert.assertEquals(0, child.getExceptionCount());
        Assert.assertEquals(3.576, child.getDuration(), 0.0001);
        Assert.assertEquals("Test output goes here", child.getHtmlContent());
    }

    /**
     * Invoke the callable with valid FitNesse report files in the workspace,
     * and compress the captured HTML output
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testInvokeWithValidFitnesseReportFilesAndCompressOutput() throws IOException, InterruptedException
    {
        final File workspace = this.tempFolder.newFolder();
        final File htmlOutputDirectory = new File(workspace, "htmloutput");
        final String name = "successful-test-results.xml";

        Files.copy(this.getClass().getResourceAsStream(name), workspace.toPath().resolve(name));

        final TaskListener mockListener = Mockito.mock(TaskListener.class);

        Mockito.when(mockListener.getLogger()).thenReturn(System.out);

        final TestsParsingCallable callable = new TestsParsingCallable("*-test-results.xml", 0, 0, new FilePath(htmlOutputDirectory), true, mockListener);

        final AggregatedFitnesseResult topLevelResult = callable.invoke(workspace, null);

        Assert.assertNotNull(topLevelResult);

        Assert.assertEquals(10, topLevelResult.getPassCount());
        Assert.assertEquals(0, topLevelResult.getFailOnlyCount());
        Assert.assertEquals(2, topLevelResult.getSkipCount());
        Assert.assertEquals(0, topLevelResult.getExceptionCount());
        Assert.assertEquals(3.951, topLevelResult.getDuration(), 0.0001);

        Assert.assertEquals(1, topLevelResult.getTotalPages());
        Assert.assertEquals(1, topLevelResult.getPassedPages());
        Assert.assertEquals(0, topLevelResult.getSkippedPages());
        Assert.assertEquals(0, topLevelResult.getFailedPages());

        Assert.assertEquals(1, topLevelResult.getChildren().size());

        final FitnessePageResult child = (FitnessePageResult) topLevelResult.getChildren().iterator().next();

        Assert.assertEquals(10, child.getPassCount());
        Assert.assertEquals(0, child.getFailOnlyCount());
        Assert.assertEquals(2, child.getSkipCount());
        Assert.assertEquals(0, child.getExceptionCount());
        Assert.assertEquals(3.576, child.getDuration(), 0.0001);
        Assert.assertEquals("Test output goes here", child.getHtmlContent());
    }
}
