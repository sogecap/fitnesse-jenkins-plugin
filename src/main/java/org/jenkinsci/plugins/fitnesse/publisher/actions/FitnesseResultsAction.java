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
import java.util.Collections;

import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;

import hudson.model.Action;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

/**
 * Adds to a Jenkins build a link that points to the FitNesse results that
 * were published as part of it, along with a short descriptive summary.
 * 
 */
public class FitnesseResultsAction extends AbstractTestResultAction<FitnesseResultsAction> implements StaplerProxy, LastBuildAction
{

    /** Results of the FitNesse tests published by this action */
    private final AggregatedFitnesseResult result;

    /**
     * Constructor
     * 
     * @param run build associated with this action
     * @param result test results associated with this action
     */
    public FitnesseResultsAction(final Run<?, ?> run, final AggregatedFitnesseResult result)
    {
        this.result = result;
    }

    /** {@inheritDoc} */
    @Override
    public String getIconFileName()
    {
        return "/plugin/fitnesse-jenkins-plugin/icons/fitnesse-logo-32x32.png";
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
        return Messages.FitnessePageResultsAction_displayName();
    }

    /** {@inheritDoc} */
    @Override
    public String getUrlName()
    {
        return "fitnesseReport";
    }

    /** {@inheritDoc} */
    @Override
    @Exported(visibility = 2)
    public int getSkipCount()
    {
        return this.result.getSkipCount();
    }

    /** {@inheritDoc} */
    @Override
    @Exported(visibility = 2)
    public int getTotalCount()
    {
        return this.result.getTotalCount();
    }

    /** {@inheritDoc} */
    @Override
    @Exported(visibility = 2)
    public int getFailCount()
    {
        return this.result.getFailCount();
    }

    /**
     * @return the number of wrong assertions
     */
    @Exported(visibility = 2)
    public int getFailOnlyCount()
    {
        return this.result.getFailOnlyCount();
    }

    /**
     * @return the number of assertions that threw an exception
     */
    @Exported(visibility = 2)
    public int getExceptionCount()
    {
        return this.result.getExceptionCount();
    }

    /** {@inheritDoc} */
    @Override
    public TestResult getResult()
    {
        // reattach parent action to the result,
        // because it is lost after deserialization
        if (this.result.getParentAction() == null)
        {
            this.result.setParentAction(this);
        }

        return this.result;
    }

    /** {@inheritDoc} */
    @Override
    public Object getTarget()
    {
        return this.result;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends Action> getProjectActions()
    {
        return Collections.singletonList(new FitnesseProjectAction(this.run.getParent()));
    }

    /**
     * Exposed for use in template publisher/actions/FitnesseTestResultsAction/summary.jelly
     * 
     * @return brief descriptive summary of the FitNesse test results
     */
    public String getSummary()
    {
        return Messages.FitnessePageResultsAction_summary(this.result.getTotalPages(), this.result.getFailedPages(), this.result.getSkippedPages());
    }
}
