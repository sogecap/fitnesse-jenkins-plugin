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

import java.util.Collection;

import org.jenkinsci.plugins.fitnesse.publisher.actions.FitnesseResultsAction;
import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnessePageResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.test.TestResult;

/**
 * {@link FitnesseResultsPublisher} Tests
 * 
 */
public class FitnesseResultsPublisherIntegrationTest
{

    /** Allows the use of of a local Jenkins instance */
    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Nominal FitNesse test results publication step
     * 
     * @throws Exception
     */
    @Test
    public void testPerform() throws Exception
    {
        // create a test Jenkins project
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();

        // copy the test XML results file into the Jenkins workspace
        final String resultsFile = "successful-test-results.xml";
        project.setScm(new SingleFileSCM(resultsFile, this.getClass().getResource(resultsFile)));

        // add the publication step to the project
        project.getPublishersList().add(new FitnesseResultsPublisher("*-results.xml", false));

        // launch the build and check its result
        final FreeStyleBuild build = project.scheduleBuild2(0).get();

        this.jenkinsRule.assertBuildStatusSuccess(build);

        // check the build's test results action
        final FitnesseResultsAction action = build.getAction(FitnesseResultsAction.class);

        Assert.assertNotNull(action);
        Assert.assertEquals(build, action.run);
        Assert.assertEquals(12, action.getTotalCount());
        Assert.assertEquals(2, action.getSkipCount());
        Assert.assertEquals(0, action.getFailCount());
        Assert.assertEquals(0, action.getFailOnlyCount());
        Assert.assertEquals(0, action.getExceptionCount());

        // check the top-level result
        final AggregatedFitnesseResult parent = (AggregatedFitnesseResult) action.getResult();

        Assert.assertNotNull(parent);
        Assert.assertEquals(parent.getParentAction(), action);
        Assert.assertNull(parent.getParent());
        Assert.assertTrue(parent.hasChildren());
        Assert.assertEquals(Result.SUCCESS, parent.getBuildResult());
        Assert.assertEquals(org.jenkinsci.plugins.fitnesse.publisher.model.Messages.AggregatedFitnessePageResults_title(), parent.getTitle());
        Assert.assertEquals(build, parent.getRun());
        Assert.assertEquals(10, parent.getPassCount());
        Assert.assertEquals(2, parent.getSkipCount());
        Assert.assertEquals(0, parent.getFailCount());
        Assert.assertEquals(0, parent.getFailOnlyCount());
        Assert.assertEquals(0, parent.getExceptionCount());
        Assert.assertEquals(3.951, parent.getDuration(), 0.0001);
        Assert.assertEquals(1, parent.getTotalPages());
        Assert.assertEquals(1, parent.getPassedPages());
        Assert.assertEquals(0, parent.getSkippedPages());
        Assert.assertEquals(0, parent.getFailedPages());

        final Collection<? extends TestResult> children = parent.getChildren();

        Assert.assertNotNull(children);
        Assert.assertEquals(1, children.size());

        // check child results
        final FitnessePageResult child = (FitnessePageResult) children.iterator().next();
        final String childName = "This.Is.A.Sample.Test.Output";

        Assert.assertEquals(parent, child.getParent());
        Assert.assertEquals(Result.SUCCESS, child.getBuildResult());
        Assert.assertEquals(childName, child.getName());
        Assert.assertEquals(childName, child.getDisplayName());
        Assert.assertEquals(org.jenkinsci.plugins.fitnesse.publisher.model.Messages.FitnessePageResults_title(child.getName()), child.getTitle());
        Assert.assertEquals(10, child.getPassCount());
        Assert.assertEquals(2, child.getSkipCount());
        Assert.assertEquals(0, child.getFailCount());
        Assert.assertEquals(0, child.getFailOnlyCount());
        Assert.assertEquals(0, child.getExceptionCount());
        Assert.assertEquals(3.576, child.getDuration(), 0.0001);
    }

    /**
     * FitNesse test results publication step, with tests failures
     * 
     * @throws Exception
     */
    @Test
    public void testPerformWithTestFailures() throws Exception
    {
        // create a test Jenkins project
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();

        // copy the test XML results file into the Jenkins workspace
        final String resultsFile = "failed-test-results.xml";
        project.setScm(new SingleFileSCM(resultsFile, this.getClass().getResource(resultsFile)));

        // add the publication step to the project
        project.getPublishersList().add(new FitnesseResultsPublisher("*-results.xml", true));

        // launch the build and check its result
        final FreeStyleBuild build = project.scheduleBuild2(0).get();

        this.jenkinsRule.assertBuildStatus(Result.UNSTABLE, build);

        // check the build's test results action
        final FitnesseResultsAction action = build.getAction(FitnesseResultsAction.class);

        Assert.assertNotNull(action);
        Assert.assertEquals(build, action.run);
        Assert.assertEquals(17, action.getTotalCount());
        Assert.assertEquals(2, action.getSkipCount());
        Assert.assertEquals(5, action.getFailCount());

        // check top-level result
        final AggregatedFitnesseResult parent = (AggregatedFitnesseResult) action.getResult();

        Assert.assertNotNull(parent);
        Assert.assertEquals(parent.getParentAction(), action);
        Assert.assertNull(parent.getParent());
        Assert.assertTrue(parent.hasChildren());
        Assert.assertEquals(Result.UNSTABLE, parent.getBuildResult());
        Assert.assertEquals(org.jenkinsci.plugins.fitnesse.publisher.model.Messages.AggregatedFitnessePageResults_title(), parent.getTitle());
        Assert.assertEquals(build, parent.getRun());
        Assert.assertEquals(10, parent.getPassCount());
        Assert.assertEquals(2, parent.getSkipCount());
        Assert.assertEquals(5, parent.getFailCount());
        Assert.assertEquals(4, parent.getFailOnlyCount());
        Assert.assertEquals(1, parent.getExceptionCount());
        Assert.assertEquals(7.201, parent.getDuration(), 0.0001);
        Assert.assertEquals(1, parent.getTotalPages());
        Assert.assertEquals(0, parent.getPassedPages());
        Assert.assertEquals(0, parent.getSkippedPages());
        Assert.assertEquals(1, parent.getFailedPages());

        final Collection<? extends TestResult> children = parent.getChildren();

        Assert.assertNotNull(children);
        Assert.assertEquals(1, children.size());

        // check child results
        final FitnessePageResult child = (FitnessePageResult) children.iterator().next();
        final String childName = "This.Is.A.Sample.Test.Output";

        Assert.assertEquals(parent, child.getParent());
        Assert.assertEquals(Result.UNSTABLE, parent.getBuildResult());
        Assert.assertEquals(childName, child.getDisplayName());
        Assert.assertEquals(childName, child.getName());
        Assert.assertEquals(org.jenkinsci.plugins.fitnesse.publisher.model.Messages.FitnessePageResults_title(child.getName()), child.getTitle());
        Assert.assertEquals(10, child.getPassCount());
        Assert.assertEquals(2, child.getSkipCount());
        Assert.assertEquals(5, child.getFailCount());
        Assert.assertEquals(4, child.getFailOnlyCount());
        Assert.assertEquals(1, child.getExceptionCount());
        Assert.assertEquals(6.421, child.getDuration(), 0.0001);
    }
}
