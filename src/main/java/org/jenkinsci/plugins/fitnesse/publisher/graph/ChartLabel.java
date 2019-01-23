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
package org.jenkinsci.plugins.fitnesse.publisher.graph;

import hudson.model.Run;
import hudson.tasks.test.TestResult;
import jenkins.model.Jenkins;

/**
 * POJO which holds the data needed to render tooltips and URLs in a graph
 * 
 */
public class ChartLabel implements Comparable<ChartLabel>
{

    /** Test results */
    private final TestResult results;

    /**
     * Constructor
     * 
     * @param results test results
     */
    public ChartLabel(final TestResult results)
    {
        this.results = results;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final ChartLabel that)
    {
        return this.results.getRun().number - that.results.getRun().number; // order builds by their number
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o)
    {
        if ((o == null) || (this.getClass() != o.getClass()))
        {
            return false;
        }

        final ChartLabel that = (ChartLabel) o;

        return this.results.equals(that.results);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return this.results.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.results.getRun().getDisplayName();
    }

    /**
     * @return test results
     */
    public TestResult getResults()
    {
        return this.results;
    }

    /**
     * @return URL pointing to the test results details page
     */
    public String getUrl()
    {
        final Run<?, ?> build = this.results.getRun();
        final String buildLink = build.getUrl();
        final String actionUrl = this.results.getTestResultAction().getUrlName();

        return String.format("%s%s%s", Jenkins.getInstance().getRootUrlFromRequest(), buildLink, actionUrl);
    }
}
