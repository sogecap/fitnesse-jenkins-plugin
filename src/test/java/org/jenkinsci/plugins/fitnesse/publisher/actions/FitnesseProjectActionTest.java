package org.jenkinsci.plugins.fitnesse.publisher.actions;

import java.util.Calendar;
import java.util.Optional;

import org.jenkinsci.plugins.fitnesse.publisher.actions.FitnesseProjectAction;
import org.jenkinsci.plugins.fitnesse.publisher.actions.FitnesseResultsAction;
import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnesseResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.jenkinsci.plugins.fitnesse.publisher.actions.Messages;

import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.TestResult;
import hudson.util.RunList;

/**
 * {@link FitnesseProjectAction} tests
 * 
 */
public class FitnesseProjectActionTest
{

    /** Jenkins job mock */
    private final Job<?, ?> mockJob = Mockito.mock(Job.class);

    /** Jenkins builds mock */
    private final RunList<?> mockBuilds = Mockito.mock(RunList.class);

    /** Jenkins build mock */
    private final Run<?, ?> mockBuild = Mockito.mock(Run.class);

    /** FitNesse result mock */
    private final FitnesseResult mockResult = Mockito.mock(FitnesseResult.class);

    /** FitNesse top-level results mock */
    private final AggregatedFitnesseResult mockResults = Mockito.mock(AggregatedFitnesseResult.class);

    /** FitNesse results action mock */
    private final FitnesseResultsAction mockResultsAction = Mockito.mock(FitnesseResultsAction.class);

    /** Tested class */
    private FitnesseProjectAction action;

    /** Rest mocks and re-instantiate the tested action before each test */
    @Before
    public void setUp()
    {
        Mockito.reset(
                this.mockJob,
                this.mockBuilds,
                this.mockBuild,
                this.mockResult,
                this.mockResults,
                this.mockResultsAction);

        this.action = new FitnesseProjectAction(this.mockJob);
    }

    /** The action's icon should not appear in the sidebar */
    @Test
    public void testHiddenIcon()
    {
        Assert.assertNull(this.action.getIconFileName());
    }

    /** The action's name should not appear in the sidebar */
    @Test
    public void testHiddenDisplayName()
    {
        Assert.assertNull(this.action.getDisplayName());
    }

    /** The action's URL should be constant */
    @Test
    public void testUrlName()
    {
        Assert.assertEquals("fitnesse", this.action.getUrlName());
    }

    /** The action's summary text should be correctly formatted when there are results */
    @Test
    public void testSummaryWithResults()
    {
        final int failedPages = 3;
        final int skippedPages = 2;
        final int totalPages = 5 + failedPages + skippedPages;

        Mockito
                .when(this.mockResults.getTotalPages())
                .thenReturn(totalPages);

        Mockito
                .when(this.mockResults.getFailedPages())
                .thenReturn(failedPages);

        Mockito
                .when(this.mockResults.getSkippedPages())
                .thenReturn(skippedPages);

        Mockito
                .when(this.mockResultsAction.getResult())
                .thenReturn(this.mockResults);

        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(this.mockResultsAction);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);

        Assert.assertEquals(
                Messages.FitnesseProjectAction_summary(totalPages, failedPages, skippedPages),
                this.action.getSummary());

        Mockito.verify(this.mockResults).getTotalPages();
        Mockito.verify(this.mockResults).getFailedPages();
        Mockito.verify(this.mockResults).getSkippedPages();
        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /** The action's summary text should be correctly formatted when there are no results */
    @Test
    public void testSummaryWithNoResults()
    {
        Mockito
                .when(this.mockResultsAction.getResult())
                .thenReturn(null);

        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(this.mockResultsAction);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);

        Assert.assertTrue(this.action.getSummary().isEmpty());

        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * A trend graph should be generated when there are results
     */
    @Test
    public void testTrendGraphWithResults()
    {
        this.mockJobWithFitnesseResultsAndAction();

        Assert.assertNotNull(this.action.getTrendGraph());

        Mockito.verify(this.mockResults, Mockito.times(4)).getRun();
        Mockito.verify(this.mockBuild, Mockito.times(3)).getParent();
        Mockito.verify(this.mockBuild).getTimestamp();
        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob, Mockito.times(3)).getBuilds();
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * No trend graph should be generated when there are no results
     */
    @Test
    public void testTrendGraphWithNoResults()
    {
        Mockito
                .when(this.mockJob.getLastBuild())
                .thenReturn(null);

        Assert.assertNull(this.action.getTrendGraph());

        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * A count graph should be generated when there are results
     */
    @Test
    public void testCountGraphWithResults()
    {
        this.mockJobWithFitnesseResultsAndAction();

        Assert.assertNotNull(this.action.getCountGraph());

        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * No trend graph should be generated when there are no results
     */
    @Test
    public void testCountGraphWithNoResults()
    {
        Mockito
                .when(this.mockJob.getLastBuild())
                .thenReturn(null);

        Assert.assertNull(this.action.getCountGraph());

        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * A duration graph should be generated when there are results
     */
    @Test
    public void testDurationGraphWithResults()
    {
        this.mockJobWithFitnesseResultsAndAction();

        Assert.assertNotNull(this.action.getDurationGraph());

        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * No duration graph should be generated when there are no results
     */
    @Test
    public void testDurationGraphWithNoResults()
    {
        Mockito
                .when(this.mockJob.getLastBuild())
                .thenReturn(null);

        Assert.assertNull(this.action.getDurationGraph());

        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * No result should be available when no builds have been launched
     */
    @Test
    public void testGetLatestResultsWithNoBuild()
    {
        Mockito
                .when(this.mockJob.getLastBuild())
                .thenReturn(null);

        Assert.assertFalse(this.action.getLatestResult().isPresent());

        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * No result should be available when at least a build has been launched
     * but does not contain published FitNesse results
     */
    @Test
    public void testGetLatestResultsWithOneBuildAndNoAction()
    {
        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(null);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastSuccessfulBuild())
                .thenReturn(this.mockBuild);

        Assert.assertFalse(this.action.getLatestResult().isPresent());

        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
        Mockito.verify(this.mockJob).getLastSuccessfulBuild();
    }

    /**
     * No result should be available when several builds have been launched
     * but none contain published FitNesse results
     */
    @Test
    public void testGetLatestResultsWithMultipleBuildsAndNoAction()
    {
        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(null);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);

        Assert.assertFalse(this.action.getLatestResult().isPresent());

        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * No result should be available when a build has been launched
     * but its action does not contain published FitNesse results
     */
    @Test
    public void testGetLatestResultsWithOneBuildAndNoResults()
    {
        Mockito
                .when(this.mockResultsAction.getResult())
                .thenReturn(null);

        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(this.mockResultsAction);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);

        Assert.assertFalse(this.action.getLatestResult().isPresent());

        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    /**
     * A result should be available when a build has been launched
     * and contains published FitNesse results
     */
    @Test
    public void testGetLatestResultsWithOneBuildAndResults()
    {
        Mockito
                .when(this.mockResultsAction.getResult())
                .thenReturn(this.mockResults);

        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(this.mockResultsAction);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);

        final Optional<TestResult> latestResult = this.action.getLatestResult();

        Assert.assertTrue(latestResult.isPresent());
        Assert.assertEquals(this.mockResults, latestResult.get());

        Mockito.verify(this.mockResultsAction).getResult();
        Mockito.verify(this.mockBuild).getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any());
        Mockito.verify(this.mockJob).getLastBuild();
    }

    // Helpers

    /**
     * Mock a Jenkins object graph that contains a {@link FitnesseResultsAction} and some mock test results
     */
    private void mockJobWithFitnesseResultsAndAction()
    {
        Mockito
                .<Run<?, ?>> when(this.mockResults.getRun())
                .thenReturn(this.mockBuild);

        Mockito
                .<Job<?, ?>> when(this.mockBuild.getParent())
                .thenReturn(this.mockJob);

        Mockito
                .when(this.mockBuild.getTimestamp())
                .thenReturn(Calendar.getInstance());

        Mockito
                .when(this.mockResultsAction.getResult())
                .thenReturn(this.mockResults);

        Mockito
                .when(this.mockBuild.getAction(ArgumentMatchers.<Class<FitnesseResultsAction>> any()))
                .thenReturn(this.mockResultsAction);

        Mockito
                .<RunList<?>> when(this.mockJob.getBuilds())
                .thenReturn(this.mockBuilds);

        Mockito
                .<Run<?, ?>> when(this.mockJob.getLastBuild())
                .thenReturn(this.mockBuild);
    }
}
