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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnessePageResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnesseResult;
import org.jenkinsci.plugins.fitnesse.publisher.parser.FitnesseResultHandler;
import org.jenkinsci.plugins.fitnesse.publisher.parser.FitnesseResultTransformerFactory;
import org.jenkinsci.plugins.fitnesse.publisher.parser.exceptions.TestParsingException;
import org.jenkinsci.plugins.fitnesse.util.CheckedConsumer;
import org.jenkinsci.plugins.fitnesse.util.CheckedPredicate;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.test.TestResult;
import jenkins.MasterToSlaveFileCallable;

/**
 * Executes the bulk of the publishing work, i.e.:
 * 
 * <ul>
 * <li>scanning the workspace for the FitNesse report files
 * <li>filter out stale files (i.e. not produced during the current build)
 * <li>parse valid reports
 * <li>consolidate report into a single {@link TestResult} instance
 * </ul>
 * 
 * <p>
 * All the code inside {@link TestsParsingCallable#invoke(File, VirtualChannel)} is executed
 * on the remote node when appropriate.
 * 
 */
public class TestsParsingCallable extends MasterToSlaveFileCallable<AggregatedFitnesseResult>
{
    private static final long serialVersionUID = -747527912539187305L;

    private final long buildTime;

    private final String testResultsGlob;

    private final long timeOnMaster;

    private final FilePath htmlOutputDirectory;

    private final boolean compressOutput;

    private final TaskListener listener;

    /**
     * Initializes a new callable responsible for parsing
     * and publishing the tests with the supplied configuration values
     * 
     * @param testResultsGlob glob pattern that matches the test results files to parse
     * @param buildTime the time at which the build was started
     * @param timeOnMaster the current time on the Jenkins master instance
     * @param htmlOutputDirectory the directory where the tests' HTML content will be written
     * @param compressOutput whether to compress the captured tests' HTML content
     * @param listener Jenkins build listener, for logging
     */
    public TestsParsingCallable(final String testResultsGlob, final long buildTime, final long timeOnMaster, final FilePath htmlOutputDirectory, final boolean compressOutput, final TaskListener listener)
    {
        this.buildTime = buildTime;
        this.testResultsGlob = testResultsGlob;
        this.timeOnMaster = timeOnMaster;
        this.htmlOutputDirectory = htmlOutputDirectory;
        this.compressOutput = compressOutput;
        this.listener = listener;
    }

    /** {@inheritDoc} */
    @Override
    public AggregatedFitnesseResult invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException
    {
        final long slaveTime = System.currentTimeMillis();
        final long localBuildTime = (this.buildTime + (slaveTime - this.timeOnMaster)) - /* error margin */ 3000;

        // retrieve FitNesse report files from the workspace

        final FilePath[] paths = new FilePath(workspace).list(this.testResultsGlob);

        if (paths.length == 0)
        {
            throw new AbortException("No FitNesse report files found");
        }

        // filter out the files generated before the current build, and parse the remaining ones

        final Transformer transformer = FitnesseResultTransformerFactory.newInstance();

        final List<FitnesseResultHandler> results = Arrays.stream(paths)
                .filter((CheckedPredicate<FilePath>) input -> input.lastModified() >= localBuildTime)
                .map(path -> this.parseReportFile(path, transformer))
                .collect(Collectors.toList());

        if (results.isEmpty())
        {
            throw new AbortException("FitNesse report files were found but none of them are new. Did tests run?");
        }

        // replace all captured test output with the path of the file it has been written to

        results.stream()
        .flatMap(r -> r.getDetails().stream())
        .forEach((CheckedConsumer<FitnesseResult>) r -> this.externalizeOutput(r, channel));

        // aggregate all parsed results into a single one

        return this.aggregateResults(results);
    }

    private FitnesseResultHandler parseReportFile(final FilePath reportPath, final Transformer transformer)
    {
        final FitnesseResultHandler handler = new FitnesseResultHandler();

        this.listener.getLogger().format("Parsing FitNesse report file \"%s\"%n", reportPath.getName());

        try (InputStream reportInputStream = Files.newInputStream(Paths.get(reportPath.getRemote())))
        {
            transformer.transform(new StreamSource(reportInputStream), new SAXResult(handler));
        } catch (TransformerException | IOException e)
        {
            throw new TestParsingException("Could not parse results file", e);
        }

        return handler;
    }

    private void externalizeOutput(final FitnesseResult result, final VirtualChannel channel) throws IOException, InterruptedException
    {
        if (result.getHtmlContent() == null)
        {
            return;
        }

        final String contentExtension = this.compressOutput ? "html.zip" : "html";
        final String contentFile = this.htmlOutputDirectory.child(String.join(".", result.getPage(), contentExtension)).getRemote();
        final FilePath contentFilePath = new FilePath(channel, contentFile);

        // handle optional output compression
        final OutputStream os = this.compressOutput ? new GZIPOutputStream(contentFilePath.write()) : contentFilePath.write();

        try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8))
        {
            osw.write(result.getHtmlContent());
        }

        result.setHtmlContent(contentFilePath.getRemote());
    }

    private AggregatedFitnesseResult aggregateResults(final List<FitnesseResultHandler> results)
    {
        final AggregatedFitnesseResult allResults = new AggregatedFitnesseResult();

        results.stream()
        .map(FitnesseResultHandler::getDetails)
        .flatMap(List::stream)
        .map(FitnessePageResult::new)
        .forEach(allResults::addChild);

        allResults.tally();

        final long totalDuration = results.stream()
                .map(FitnesseResultHandler::getSummary)
                .mapToLong(FitnesseResult::getDuration)
                .sum();

        allResults.setDuration(totalDuration);

        return allResults;
    }
}
