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

import java.util.Optional;

import org.jenkinsci.plugins.fitnesse.publisher.graph.FitnesseHistory;
import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.tasks.test.TestResult;
import hudson.util.Graph;

/**
 * Adds to a Jenkins project a link that points to the latest FitNesse
 * results that were published, along with a short descriptive summary.
 * Also adds a graph which shows the trend of the last few builds that
 * published FitNesse results.
 *
 */
public class FitnesseProjectAction implements Action
{

    private final Job<?, ?> job;

    /**
     * Constructor
     * 
     * @param project Jenkins project
     */
    public FitnesseProjectAction(final Job<?, ?> project)
    {
        this.job = project;
    }

    /** {@inheritDoc} */
    @Override
    public String getIconFileName()
    {
        return null; // hide from left sidebar
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
        return null; // hide from left sidebar
    }

    /** {@inheritDoc} */
    @Override
    public String getUrlName()
    {
        return "fitnesse";
    }

    /**
     * @return {@code true} if there are any FitNesse test results, {@code false} otherwise
     */
    public boolean hasResults()
    {
        return this.getLatestResult().isPresent();
    }

    /**
     * @return the FitNesse test results trend graph
     */
    public Graph getTrendGraph()
    {
        return this.getLatestResult()
                .map(FitnesseHistory::new)
                .map(FitnesseHistory::getTrendGraph)
                .orElse(null);
    }

    /**
     * @return the FitNesse test results count graph
     */
    public Graph getCountGraph()
    {
        return this.getLatestResult()
                .map(FitnesseHistory::new)
                .map(FitnesseHistory::getCountGraph)
                .orElse(null);
    }

    /**
     * @return the FitNesse test results duration graph
     */
    public Graph getDurationGraph()
    {
        return this.getLatestResult()
                .map(FitnesseHistory::new)
                .map(FitnesseHistory::getDurationGraph)
                .orElse(null);
    }

    /**
     * @return the latest test results associated that were published.
     *         {@code Optional#empty()} in case none were found.
     */
    public Optional<TestResult> getLatestResult()
    {
        final Run<?, ?> lastSuccessfulBuild = this.job.getLastSuccessfulBuild();
        Run<?, ?> build = this.job.getLastBuild();

        while (build != null)
        {
            final FitnesseResultsAction action = build.getAction(FitnesseResultsAction.class);

            if (action != null)
            {
                return Optional.ofNullable(action.getResult());
            } else if (build.equals(lastSuccessfulBuild))
            {
                // there is only one build and it does
                // not contain any published results
                return Optional.empty();
            }

            build = build.getPreviousBuild();
        }

        return Optional.empty();
    }

    /**
     * @return a brief summary describing the latest FitNesse results
     */
    public String getSummary()
    {
        return this.getLatestResult()
                .map(AggregatedFitnesseResult.class::cast)
                .map(result -> Messages.FitnesseProjectAction_summary(
                        result.getTotalPages(),
                        result.getFailedPages(),
                        result.getSkippedPages()))
                .orElse("");
    }
}
