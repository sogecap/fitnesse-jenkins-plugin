/*
 * Copyright (C) 2019 Société Générale.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import org.jenkinsci.plugins.fitnesse.builder.runner.FitnessePageRunner;
import org.jenkinsci.plugins.fitnesse.builder.runner.FitnesseResponse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

/**
 * {@link Builder} implementation which allows to add a Jenkins build step
 * managing the execution of FitNesse pages or suites from a given configuration
 * 
 * <p>
 * FitNesse targets are executed according to the user-provided configuration.
 * 
 * <p>
 * The results of the FitNesse targets execution are written to disk into Jenkins's
 * workspace and thus may be deleted once they are no longer of use.
 * 
 */
public class FitnesseResultsBuilder extends Builder implements SimpleBuildStep
{

    private URL remoteFitnesseUrl;

    private String targetType;

    private String targetSuite;

    private String targetPages;

    private String targetFile;

    private boolean includeHtmlOutput;

    private int httpTimeout;

    private String filenameOutputFormat;

    private int concurrencyLevel;

    /** Default constructor */
    @DataBoundConstructor
    public FitnesseResultsBuilder()
    {
        this.httpTimeout = DescriptorImpl.DEFAULT_HTTP_TIMEOUT;
        this.filenameOutputFormat = DescriptorImpl.DEFAULT_FILENAME_OUTPUT_FORMAT;
        this.concurrencyLevel = DescriptorImpl.DEFAULT_CONCURRENCY_LEVEL;
    }

    /** {@inheritDoc} */
    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener) throws InterruptedException, IOException
    {
        listener.getLogger().printf("Launching FitNesse tests on remote host \"%s\"...%n", this.remoteFitnesseUrl);

        final FitnessePageRunner runner = new FitnessePageRunner(this.remoteFitnesseUrl, this.httpTimeout, this.includeHtmlOutput, this.concurrencyLevel, listener);
        final TestsExecutionCallable callable = new TestsExecutionCallable(runner, listener, this.targetType, this.targetFile, this.targetPages, this.targetSuite);

        // execute the pages on the node and write the resulting responses in the workspace
        try
        {
            for (final FitnesseResponse response : workspace.act(callable))
            {
                workspace
                        .child(String.format(this.filenameOutputFormat, response.getPage()))
                        .write(response.getContent(), StandardCharsets.UTF_8.name());
            }
        } catch (final InterruptedException ie)
        {
            listener.getLogger().println("Build was aborted, stopping queued and running tests...");
            runner.cancelRequests();
            throw ie;
        }
    }

    /** {@inheritDoc} */
    @Override
    public DescriptorImpl getDescriptor()
    {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * @return name of the remote Fitnesse URL
     */
    public URL getRemoteFitnesseUrl()
    {
        return this.remoteFitnesseUrl;
    }

    /**
     * @return type of the FitNesse targets
     */
    public String getTargetType()
    {
        return this.targetType;
    }

    /**
     * @return name of the workspace file which contains the list of FitNesse pages to run
     */
    public String getTargetFile()
    {
        return this.targetFile;
    }

    /**
     * @return newline/space-delimited list of the FitNesse targets to run
     */
    public String getTargetPages()
    {
        return this.targetPages;
    }

    /**
     * @return name of the targeted FitNesse suite
     */
    public String getTargetSuite()
    {
        return this.targetSuite;
    }

    /**
     * @return whether to capture the HTML output of the FitNesse tests
     */
    public boolean getIncludeHtmlOutput()
    {
        return this.includeHtmlOutput;
    }

    /**
     * @return HTTP timeout
     */
    public int getHttpTimeout()
    {
        return this.httpTimeout;
    }

    /**
     * @return filename pattern of the generated Fitnesse test output files
     */
    public String getFilenameOutputFormat()
    {
        return this.filenameOutputFormat;
    }

    /**
     * @return maximum number of concurrently running pages
     */
    public int getConcurrencyLevel()
    {
        return this.concurrencyLevel;
    }

    /**
     * @param remoteFitnesseUrl
     */
    @DataBoundSetter
    public void setRemoteFitnesseUrl(final URL remoteFitnesseUrl)
    {
        this.remoteFitnesseUrl = remoteFitnesseUrl;
    }

    /**
     * @param httpTimeout
     */
    @DataBoundSetter
    public void setHttpTimeout(final int httpTimeout)
    {
        this.httpTimeout = httpTimeout;
    }

    /**
     * @param targetType
     */
    @DataBoundSetter
    public void setTargetType(final String targetType)
    {
        this.targetType = targetType;
    }

    /**
     * @param includeHtmlOutput
     */
    @DataBoundSetter
    public void setIncludeHtmlOutput(final boolean includeHtmlOutput)
    {
        this.includeHtmlOutput = includeHtmlOutput;
    }

    /**
     * @param targetSuite
     */
    @DataBoundSetter
    public void setTargetSuite(final String targetSuite)
    {
        this.targetSuite = targetSuite;
    }

    /**
     * @param targetPages
     */
    @DataBoundSetter
    public void setTargetPages(final String targetPages)
    {
        this.targetPages = targetPages;
    }

    /**
     * @param targetTextFile
     */
    @DataBoundSetter
    public void setTargetFile(final String targetTextFile)
    {
        this.targetFile = targetTextFile;
    }

    /**
     * @param filenameOutputFormat
     */
    @DataBoundSetter
    public void setFilenameOutputFormat(final String filenameOutputFormat)
    {
        this.filenameOutputFormat = filenameOutputFormat;
    }

    /**
     * @param concurrencyLevel
     */
    @DataBoundSetter
    public void setConcurrencyLevel(final int concurrencyLevel)
    {
        this.concurrencyLevel = concurrencyLevel;
    }

    /**
     * FitNesse execution targets
     * 
     * <p>
     * Describes the different types of FitNesse targets to be run by {@link FitnessePageRunner}.
     * 
     * @see FitnessePageRunner
     */
    public enum TargetType
    {

        /** Text file containing a list of the FitNesse pages to execute */
        TEXT_FILE("textfile"),

        /** List of FitNesse pages to execute */
        PAGES("pages"),

        /** FitNesse page containing a list of FitNesse pages to execute */
        SUITE("suite"),

        /** Unsupported target type */
        UNKNOWN("unknown");

        private String name;

        /**
         * @param name target name
         */
        TargetType(final String name)
        {
            this.name = name;
        }

        /**
         * Determines the target type corresponding to the given string
         * 
         * @param value string representation of the target type
         * @return the corresponding target type, or {@code FitnesseTargetType#UNKNOWN} if none was found
         */
        public static TargetType targetTypeFor(final String value)
        {
            for (final TargetType type : TargetType.values())
            {
                if (type.getName().equals(value))
                {
                    return type;
                }
            }

            return UNKNOWN;
        }

        /**
         * @return name of the target
         */
        public String getName()
        {
            return this.name;
        }
    }
}
