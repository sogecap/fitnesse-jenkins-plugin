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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.jenkinsci.plugins.fitnesse.builder.FitnesseResultsBuilder.TargetType;
import org.jenkinsci.plugins.fitnesse.util.MockFitnesseResponseDispatcher;
import org.jenkinsci.plugins.fitnesse.util.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import okhttp3.mockwebserver.MockWebServer;

/** {@link FitnesseResultsBuilder} tests */
public class FitnesseResultsBuilderIntegrationTest
{

    /** Allows to execute local Jenkins instances */
    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

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
     * Execute a unsupported FitNesse target type
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithUnsupportedTarget() throws Exception
    {
        // FitNesse build step configuration
        final FitnesseResultsBuilder builder = new FitnesseResultsBuilder();
        builder.setRemoteFitnesseUrl(this.mockServer.url("/").url());
        builder.setHttpTimeout(120);
        builder.setTargetType("UnsupportedTargetType");
        builder.setTargetPages("This.Is.A.Fake.Page");
        builder.setIncludeHtmlOutput(true);
        builder.setFilenameOutputFormat("%s-results.xml");

        // test Jenkins project creation
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();

        // add FitNesse build step to the project
        project.getBuildersList().add(builder);

        // launch a build for this project and check its result
        final FreeStyleBuild build = project.scheduleBuild2(0).get();

        Assert.assertEquals("No requests should have been made", 0, this.mockServer.getRequestCount());
        Assert.assertEquals("The build should have failed", Result.FAILURE, build.getResult());
    }

    /**
     * Execute a list of FitNesse targets
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithPagesTarget() throws Exception
    {
        // FitNesse build step configuration
        final URL remoteFitnesseUrl = this.mockServer.url("/").url();
        final String targetPage = "This.Is.A.Fake.Page";
        final String filenameOutputFormat = "%s-results.xml";

        final FitnesseResultsBuilder builder = new FitnesseResultsBuilder();
        builder.setRemoteFitnesseUrl(remoteFitnesseUrl);
        builder.setHttpTimeout(120);
        builder.setTargetType(TargetType.PAGES.getName());
        builder.setTargetPages(targetPage);
        builder.setIncludeHtmlOutput(true);
        builder.setFilenameOutputFormat(filenameOutputFormat);

        // mock remote Fitnesse host HTTP responses
        final String expectedResponse = "Coucou";

        final Map<String, String> expectedResults = Collections.singletonMap(String.format(filenameOutputFormat, targetPage), expectedResponse);

        this.mockServer.setDispatcher(new MockFitnesseResponseDispatcher(Collections.singletonMap(targetPage, expectedResponse)));

        // create a test FitNesse project
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();

        // add the FitNesse build step to the project
        project.getBuildersList().add(builder);

        // launch the build and check its result
        final FreeStyleBuild build = this.jenkinsRule.buildAndAssertSuccess(project);

        Assert.assertEquals("One request should have been made", 1, this.mockServer.getRequestCount());

        Assert.assertEquals("The request path and/or parameters are incorrect",
                "/This.Is.A.Fake.Page?test&includehtml&format=xml&nochunk",
                this.mockServer.takeRequest().getPath());

        Assert.assertEquals("The output filenames and/or content is incorrect",
                expectedResults,
                TestUtils.gatherWorkspaceOutputFiles(filenameOutputFormat, build.getWorkspace()));
    }

    /**
     * Execute a list of FitNesse targets read from a workspace text file
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithTextFileTarget() throws Exception
    {
        // FitNesse build step configuration
        final URL remoteFitnesseUrl = this.mockServer.url("/").url();
        final int httpTimeout = 120;
        final String targetType = TargetType.TEXT_FILE.getName();
        final boolean includeHtmlOutput = true;
        final String filenameOutputFormat = "%s-results.xml";
        final URL testFile = this.getClass().getResource("fitnesseTests.txt");
        final Path testFilePath = Paths.get(testFile.toURI());

        // mock remote Fitnesse host HTTP responses
        final UnaryOperator<String> generateResultFilename = page -> String.format(filenameOutputFormat, page);
        final UnaryOperator<String> generateMockResponse = (page) -> String.format("%s - OK", page);

        final List<String> testPages = Files.readAllLines(testFilePath, StandardCharsets.UTF_8);

        // generate expected results & requests
        final Map<String, String> expectedResults = testPages.stream().collect(Collectors.toMap(generateResultFilename, generateMockResponse));
        final List<String> expectedRequests = testPages.stream().map(page -> String.format("/%s?test&includehtml&format=xml&nochunk", page)).collect(Collectors.toList());

        // create a custom dispatcher which will answer the requests with the mock HTTP responses
        final MockFitnesseResponseDispatcher dispatcher = new MockFitnesseResponseDispatcher(
                testPages.stream().collect(Collectors.toMap(Function.identity(), generateMockResponse)));

        this.mockServer.setDispatcher(dispatcher);

        // instantiate the FitNesse test runner
        final FitnesseResultsBuilder pageBuilder = new FitnesseResultsBuilder();
        pageBuilder.setRemoteFitnesseUrl(remoteFitnesseUrl);
        pageBuilder.setHttpTimeout(httpTimeout);
        pageBuilder.setTargetType(targetType);
        pageBuilder.setTargetFile(testFile.getFile());
        pageBuilder.setIncludeHtmlOutput(includeHtmlOutput);
        pageBuilder.setFilenameOutputFormat(filenameOutputFormat);

        // create a test Jenkins project
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();
        project.setScm(new SingleFileSCM(testFile.getFile(), Files.readAllBytes(testFilePath)));

        // add the FitNesse build step to the project
        project.getBuildersList().add(pageBuilder);

        // launch the build and check its result
        final FreeStyleBuild build = this.jenkinsRule.buildAndAssertSuccess(project);

        Assert.assertEquals("A request for each targeted page should have been made", 3, this.mockServer.getRequestCount());

        Assert.assertThat("The requests' path and/or parameters are incorrect",
                expectedRequests,
                Matchers.hasItems(new String[] {
                        this.mockServer.takeRequest().getPath(),
                        this.mockServer.takeRequest().getPath(),
                        this.mockServer.takeRequest().getPath()
                }));

        Assert.assertEquals("The output filenames and/or content is incorrect",
                expectedResults,
                TestUtils.gatherWorkspaceOutputFiles(filenameOutputFormat, build.getWorkspace()));
    }

    /**
     * Execute a FitNesse test suite
     * 
     * @throws Exception
     */
    @Test
    public void testRunWithSuiteTarget() throws Exception
    {
        // mock remote Fitnesse host HTTP responses
        final URL remoteFitnesseUrl = this.mockServer.url("/").url();
        final int httpTimeout = 120;
        final String targetType = TargetType.SUITE.getName();
        final String targetSuite = "This.Is.A.Fake.Suite";
        final boolean includeHtmlOutput = true;
        final String filenameOutputFormat = "%s-results.xml";

        final FitnesseResultsBuilder builder = new FitnesseResultsBuilder();
        builder.setRemoteFitnesseUrl(remoteFitnesseUrl);
        builder.setHttpTimeout(httpTimeout);
        builder.setTargetType(targetType);
        builder.setTargetSuite(targetSuite);
        builder.setIncludeHtmlOutput(includeHtmlOutput);
        builder.setFilenameOutputFormat(filenameOutputFormat);

        // mock remote Fitnesse host HTTP responses
        final String expectedResponse = "Coucou";

        final Map<String, String> expectedResults = Collections.singletonMap(
                String.format(filenameOutputFormat, targetSuite), expectedResponse);

        final MockFitnesseResponseDispatcher dispatcher = new MockFitnesseResponseDispatcher(
                Collections.singletonMap(targetSuite, expectedResponse));

        this.mockServer.setDispatcher(dispatcher);

        // create a test Jenkins project
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();

        // add the FitNesse build step to the project
        project.getBuildersList().add(builder);

        // launch the build and check its result
        final FreeStyleBuild build = this.jenkinsRule.buildAndAssertSuccess(project);

        Assert.assertEquals("One request should have been made", 1, this.mockServer.getRequestCount());

        Assert.assertEquals("The request path and/or parameters are incorrect",
                "/This.Is.A.Fake.Suite?suite&includehtml&format=xml&nochunk",
                this.mockServer.takeRequest().getPath());

        Assert.assertEquals("The output filenames and/or content is incorrect",
                expectedResults,
                TestUtils.gatherWorkspaceOutputFiles(filenameOutputFormat, build.getWorkspace()));
    }
}
