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
