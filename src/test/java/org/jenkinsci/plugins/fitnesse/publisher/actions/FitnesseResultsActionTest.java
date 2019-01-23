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
package org.jenkinsci.plugins.fitnesse.publisher.actions;

import java.util.Collection;

import org.jenkinsci.plugins.fitnesse.publisher.actions.FitnesseProjectAction;
import org.jenkinsci.plugins.fitnesse.publisher.actions.FitnesseResultsAction;
import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.jenkinsci.plugins.fitnesse.publisher.actions.Messages;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

/**
 * {@link FitnesseResultsAction} tests
 * 
 */
public class FitnesseResultsActionTest
{

    /** Jenkins job mock */
    private final Job<?, ?> mockJob = Mockito.mock(Job.class);

    /** Jenkins build mock */
    private final Run<?, ?> mockBuild = Mockito.mock(Run.class);

    /** FitNesse results mock */
    private final AggregatedFitnesseResult mockResults = Mockito.mock(AggregatedFitnesseResult.class);

    /** Tested class */
    private FitnesseResultsAction resultsAction;

    /** Rest mocks and re-instantiate the tested action before each test */
    @Before
    public void setUp()
    {
        Mockito.reset(
                this.mockJob,
                this.mockBuild,
                this.mockResults);

        this.resultsAction = new FitnesseResultsAction(this.mockBuild, this.mockResults);
    }

    /** Action icon path */
    @Test
    public void testIconFileName()
    {
        Assert.assertEquals("/plugin/fitnesse-jenkins-plugin/icons/fitnesse-logo-32x32.png", this.resultsAction.getIconFileName());
    }

    /** Action name */
    @Test
    public void testDisplayName()
    {
        Assert.assertEquals(Messages.FitnessePageResultsAction_displayName(), this.resultsAction.getDisplayName());
    }

    /** Action URL */
    @Test
    public void testUrlName()
    {
        Assert.assertEquals("fitnesseReport", this.resultsAction.getUrlName());
    }

    /** Total number of tests */
    @Test
    public void testTotalTestsCount()
    {
        final int testsCount = 10;

        Mockito
                .when(this.mockResults.getTotalCount())
                .thenReturn(testsCount);

        Assert.assertEquals(testsCount, this.resultsAction.getTotalCount());

        Mockito.verify(this.mockResults).getTotalCount();
    }

    /** Number of ignored tests */
    @Test
    public void testSkippedTestsCount()
    {
        final int testsCount = 10;

        Mockito
                .when(this.mockResults.getSkipCount())
                .thenReturn(testsCount);

        Assert.assertEquals(testsCount, this.resultsAction.getSkipCount());

        Mockito.verify(this.mockResults).getSkipCount();
    }

    /** Number of failed tests */
    @Test
    public void testFailTestsCount()
    {
        final int testsCount = 10;

        Mockito
                .when(this.mockResults.getFailCount())
                .thenReturn(testsCount);

        Assert.assertEquals(testsCount, this.resultsAction.getFailCount());

        Mockito.verify(this.mockResults).getFailCount();
    }

    /** Tests results associated with the action */
    @Test
    public void testGetResult()
    {
        Assert.assertEquals(this.resultsAction.getResult(), this.mockResults);
    }

    /** Object which handles the web (Stapler) requests */
    @Test
    public void testGetTarget()
    {
        Assert.assertEquals(this.resultsAction.getTarget(), this.mockResults);
    }

    /** Actions associated with the Jenkins job */
    @Test
    public void testGetProjectActions()
    {
        Mockito
                .<Job<?, ?>> when(this.mockBuild.getParent())
                .thenReturn(this.mockJob);

        this.resultsAction.run = this.mockBuild;

        final Collection<? extends Action> projectActions = this.resultsAction.getProjectActions();

        Assert.assertFalse(projectActions.isEmpty());
        Assert.assertEquals(1, projectActions.size());
        Assert.assertEquals(FitnesseProjectAction.class, projectActions.iterator().next().getClass());

        Mockito.verify(this.mockBuild).getParent();
    }

    /** Action test results statistics summary */
    @Test
    public void testSummary()
    {
        final int totalPages = 10;
        final int failedPages = 5;
        final int skippedPages = 2;

        Mockito
                .when(this.mockResults.getTotalPages())
                .thenReturn(totalPages);

        Mockito
                .when(this.mockResults.getFailedPages())
                .thenReturn(failedPages);

        Mockito
                .when(this.mockResults.getSkippedPages())
                .thenReturn(skippedPages);

        Assert.assertEquals(
                Messages.FitnessePageResultsAction_summary(totalPages, failedPages, skippedPages),
                this.resultsAction.getSummary());

        Mockito.verify(this.mockResults).getTotalPages();

        Mockito.verify(this.mockResults).getFailedPages();

        Mockito.verify(this.mockResults).getSkippedPages();
    }
}
