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
package org.jenkinsci.plugins.fitnesse.publisher.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.kohsuke.stapler.export.Exported;

import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

/**
 * Contains the statistics of a FitNesse page/suite execution
 * 
 * <p>
 * Used for presentation means, unlike {@link FitnesseResult}.
 * 
 */
public class FitnessePageResult extends TestResult
{

    /** serialVersionUID */
    private static final long serialVersionUID = -2491740833549554569L;

    /** Result which contain the test execution statistics (Skipped, Failed, Passed, etc.) */
    private final FitnesseResult result;

    /** The build in which this result was produced */
    private transient Run<?, ?> run;

    /** Parent (top-level) result */
    private TestObject parent;

    /**
     * Constructor
     * 
     * @param result result which contain the test execution statistics
     */
    public FitnessePageResult(final FitnesseResult result)
    {
        this.result = result;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
        return this.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle()
    {
        return Messages.FitnessePageResults_title(this.result.getPage());
    }

    /** {@inheritDoc} */
    @Override
    public String getName()
    {
        return this.result.getPage();
    }

    /** {@inheritDoc} */
    @Override
    public int getPassCount()
    {
        return this.result.getRight();
    }

    /** {@inheritDoc} */
    @Override
    public int getFailCount()
    {
        return this.result.getWrong() + this.result.getExceptions();
    }

    /** {@inheritDoc} */
    @Override
    public int getSkipCount()
    {
        return this.result.getIgnored();
    }

    /** {@inheritDoc} */
    @Override
    public float getDuration()
    {
        return BigDecimal
                .valueOf(this.result.getDuration())
                .divide(BigDecimal.valueOf(1000))
                .floatValue();
    }

    /** {@inheritDoc} */
    @Override
    public Result getBuildResult()
    {
        return this.hasFailures() ? Result.UNSTABLE : Result.SUCCESS;
    }

    /** {@inheritDoc} */
    @Override
    public TestObject getParent()
    {
        return this.parent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final TestObject parent)
    {
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public Run<?, ?> getRun()
    {
        return this.run;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPassed()
    {
        return !this.hasFailures() && (this.getPassCount() > 0);
    }

    /** {@inheritDoc} */
    @Override
    public TestResult findCorrespondingResult(final String id)
    {
        if (this.getId().equals(id) || (id == null))
        {
            return this;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.toPrettyString();
    }

    /**
     * @return number of failing assertions, not due to an exception
     */
    @Exported(visibility = 2)
    public int getFailOnlyCount()
    {
        return this.result.getWrong();
    }

    /**
     * @return number of failing assertions, due to an exception
     * 
     */
    @Exported(visibility = 2)
    public int getExceptionCount()
    {
        return this.result.getExceptions();
    }

    /**
     * @return link pointing to the details of this result
     */
    @Exported(visibility = 2)
    public String getDetailsLink()
    {
        if (this.result.getHtmlContent() != null)
        {
            return String.format("<a href=\"%1$s\">%1$s</a>", this.getName());
        }

        return this.getName();
    }

    /**
     * @return this result's captured HTML output, read from the filesystem
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Exported(visibility = 2)
    public String getHtmlContent() throws IOException
    {
        final String htmlContent = this.result.getHtmlContent();

        // use a placeholder if no output is available
        if (htmlContent == null)
        {
            return String.format("<p>%s</p>", Messages.FitnessePageResults_noContent());
        }

        final FilePath htmlContentFilePath = new FilePath(new File(htmlContent));

        // handle optional output compression
        InputStream in;

        try
        {
            in = htmlContentFilePath.read();
        } catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return null;
        }

        if (htmlContentFilePath.getName().endsWith(".zip"))
        {
            in = new GZIPInputStream(in);
        }

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
        {
            final StringBuilder builder = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
            {
                builder.append(line);
            }

            return builder.toString();
        }
    }

    /**
     * @return whether some assertions have been skipped
     */
    public boolean hasSkipped()
    {
        return this.getSkipCount() != 0;
    }

    /**
     * @return whether some assertions have failed, due to an exception or not
     */
    public boolean hasFailures()
    {
        return this.hasWrong() || this.hasExceptions();
    }

    /**
     * @return whether some assertions have failed, not due to an exception
     */
    public boolean hasWrong()
    {
        return this.getFailCount() > 0;
    }

    /**
     * @return whether some assertions have thrown some exceptions
     */
    public boolean hasExceptions()
    {
        return this.getExceptionCount() > 0;
    }

    /**
     * @param run Jenkins build
     */
    public void setRun(final Run<?, ?> run)
    {
        this.run = run;
    }
}
