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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.fitnesse.builder.FitnesseResultsBuilder.TargetType;
import org.jenkinsci.plugins.fitnesse.builder.runner.FitnessePageRunner;
import org.jenkinsci.plugins.fitnesse.builder.runner.FitnesseResponse;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

/**
 * Executes the bulk of the building work <strong>on the node where the build occurs</strong>, i.e.:
 * 
 * <ul>
 * <li>read the list of FitNesse pages or suite to execute, according to the set target type
 * <li>execute the resulting pages or suite on the remote FitNesse server
 * <li>write the corresponding responses to the workspace, for them to be picked up by the
 * publishing step later on
 * </ul>
 * 
 * <p>
 * All the code inside {@link TestsExecutionCallable#invoke(File, VirtualChannel)} is executed
 * on the remote node when appropriate.
 * 
 */
public class TestsExecutionCallable extends MasterToSlaveFileCallable<List<FitnesseResponse>>
{

    private static final long serialVersionUID = -9160350424551516236L;

    private final FitnessePageRunner runner;

    private final TaskListener listener;

    private final String targetType;

    private final String targetFile;

    private final String targetPages;

    private final String targetSuite;

    /**
     * Initializes a new callable responsible for executing
     * the tests with the supplied runner & targets
     * 
     * @param runner class responsible for running the FitNesse pages
     * @param listener Jenkins build listener, for logging
     * @param targetType the type of the targeted pages (suite/pages/text file)
     * @param targetFile path to the file containing the pages to run
     * @param targetPages a list of FitNesse pages to run
     * @param targetSuite a FitNesse suite to run
     */
    public TestsExecutionCallable(
            final FitnessePageRunner runner,
            final TaskListener listener,
            final String targetType,
            final String targetFile,
            final String targetPages,
            final String targetSuite)
    {
        this.runner = runner;
        this.listener = listener;
        this.targetType = targetType;
        this.targetFile = targetFile;
        this.targetPages = targetPages;
        this.targetSuite = targetSuite;
    }

    /** {@inheritDoc} */
    @Override
    public List<FitnesseResponse> invoke(final File workspace, final VirtualChannel channel) throws IOException, InterruptedException
    {
        final TargetType suppliedTargetType = TargetType.targetTypeFor(this.targetType);

        if (suppliedTargetType == TargetType.UNKNOWN)
        {
            throw new AbortException(String.format("Unsupported target type \"%s\"", this.targetType));
        }

        // execution of a FitNesse suite

        if (suppliedTargetType == TargetType.SUITE)
        {
            return Collections.singletonList(this.runner.executeSuite(this.targetSuite).join());
        }

        // execution of a FitNesse pages list

        String[] pages = new String[] {};

        // read pages from a text file in the workspace
        if (suppliedTargetType == TargetType.TEXT_FILE)
        {
            pages = new FilePath(workspace)
                    .child(this.targetFile)
                    .readToString()
                    .replaceAll("\r\n", "\n")
                    .split("\n");
        }

        // read pages from the configuration
        if (suppliedTargetType == TargetType.PAGES)
        {
            pages = this.targetPages.split("\n");
        }

        // enqueue all FitNesse calls
        final CompletableFuture<FitnesseResponse>[] responses = Arrays.stream(pages)
                .map(this.runner::executePage)
                // log failed responses and null them out
                .map(future -> future.handle((response, error) -> {
                    if (error != null)
                    {
                        this.listener.getLogger().println(error.getMessage());
                        return null;
                    }
                    return response;
                }))
                .toArray(CompletableFuture[]::new);

        // await the completion of all calls
        CompletableFuture.allOf(responses).join();

        // collect all successful responses
        return Arrays.stream(responses)
                // does not block, as we called allOf() and
                // join() on the resulting future earlier
                .map(CompletableFuture::join)
                // filter the null (failed) responses
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
