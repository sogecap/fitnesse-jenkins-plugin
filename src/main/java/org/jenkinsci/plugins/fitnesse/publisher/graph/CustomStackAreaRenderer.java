package org.jenkinsci.plugins.fitnesse.publisher.graph;

import org.jfree.data.category.CategoryDataset;

import org.jenkinsci.plugins.fitnesse.publisher.actions.Messages;

import hudson.util.StackedAreaRenderer2;

/**
 * Renderer which allows customization of tooltips and URLs for a graph
 * 
 */
public class CustomStackAreaRenderer extends StackedAreaRenderer2
{

    /** serialVersionUID */
    private static final long serialVersionUID = -6044704485702633292L;

    /** {@inheritDoc} */
    @Override
    public String generateURL(final CategoryDataset dataset, final int row, final int column)
    {
        return ((ChartLabel) dataset.getColumnKey(column)).getUrl();
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column)
    {
        final ChartLabel build = (ChartLabel) dataset.getColumnKey(column);
        final String buildId = Messages.FitnessePageResultsAction_build(build.getResults().getRun().getId());

        // note: the ordering must be the same than the one specified when building the dataset (cf. FitnesseHistory)
        switch (row)
        {
        case 0:
            return String.valueOf(Messages.FitnessePageResultsAction_skipped(buildId, build.getResults().getSkipCount()));
        case 1:
            return String.valueOf(Messages.FitnessePageResultsAction_failed(buildId, build.getResults().getFailCount()));
        default:
            return String.valueOf(Messages.FitnessePageResultsAction_total(buildId, build.getResults().getTotalCount()));
        }
    }
}
