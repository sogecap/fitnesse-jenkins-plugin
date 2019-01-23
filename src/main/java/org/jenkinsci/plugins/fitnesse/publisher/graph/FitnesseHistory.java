package org.jenkinsci.plugins.fitnesse.publisher.graph;

import java.awt.Color;
import java.util.Calendar;

import hudson.tasks.junit.History;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

/**
 * Allows the generation of a dataset containing tests executions results
 * and the generation of an additional trend graph
 * 
 */
public class FitnesseHistory extends History
{

    /**
     * Constructor
     * 
     * @param testObject object holding the tests execution results
     */
    public FitnesseHistory(final TestObject testObject)
    {
        super(testObject);
    }

    /**
     * Generate the tests results trend graph
     * 
     * @return the resulting graph
     */
    public Graph getTrendGraph()
    {
        final DataSetBuilder<String, ChartLabel> builder = new DataSetBuilder<>();

        for (final TestResult result : this.getList())
        {
            // the numeric prefixes are only here to force the label ordering;
            // they are not displayed because the legend is hidden by default
            builder.add(result.getSkipCount(), "0 - Skipped", new ChartLabel(result));
            builder.add(result.getFailCount(), "1 - Failed", new ChartLabel(result));
            builder.add(result.getPassCount(), "2 - Passed", new ChartLabel(result));
        }

        final Calendar timestamp = this.getTestObject().getRun().getTimestamp();

        // the colors's ordering match the categories in the dataset above
        return FitnesseGraph.from(timestamp.getTimeInMillis(), builder.build(), "build", "count", Color.YELLOW, Color.RED, Color.GREEN);
    }
}
