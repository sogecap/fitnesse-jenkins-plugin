package org.jenkinsci.plugins.fitnesse.publisher.graph;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

/**
 * Trend graph (passed/skipped/ignored) of the FitNesse tests results
 * 
 */
public final class FitnesseGraph extends Graph
{

    /** Default height */
    private static final int DEFAULT_GRAPH_HEIGHT = 200;

    /** Default width */
    private static final int DEFAULT_GRAPH_WIDTH = 500;

    /** Default color */
    private static final Color DEFAULT_GRAPH_COLOR = Color.BLUE;

    /** Label of the x-axis */
    private final String xAxisLabel;

    /** Label of the y-axis */
    private final String yAxisLabel;

    /** Colors used to draw the graph */
    private final Color[] colors;

    /** Data used to draw the graph */
    private final CategoryDataset categoryDataset;

    /**
     * Create a graph from the FitNesse test results
     * 
     * @param timestamp graph generation epoch (used by Jenkins for HTTP cache headers)
     * @param categoryDataset data used fo drawing the graph
     * @param xAxisLabel label used for the x-axis
     * @param yAxisLabel label used for the y-axis
     * @param colors colors used to draw the graph
     * @return the resulting graph
     */
    public static FitnesseGraph from(final long timestamp, final CategoryDataset categoryDataset, final String xAxisLabel, final String yAxisLabel, final Color... colors)
    {
        return new FitnesseGraph(timestamp, FitnesseGraph.DEFAULT_GRAPH_WIDTH, FitnesseGraph.DEFAULT_GRAPH_HEIGHT, categoryDataset, xAxisLabel, yAxisLabel, colors);
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createGraph()
    {
        final JFreeChart chart = ChartFactory.createStackedAreaChart(
                null, // no title
                this.xAxisLabel,
                this.yAxisLabel,
                this.categoryDataset,
                PlotOrientation.VERTICAL,
                false, // no legend
                true, // generate tooltips
                true); // generate URLs

        chart.setBackgroundPaint(Color.WHITE);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setForegroundAlpha(0.7f);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.DARK_GRAY);

        final CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        plot.setDomainAxis(domainAxis);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRange(true);

        final Font font = new Font("Dialog", Font.PLAIN, 12);
        plot.getDomainAxis().setLabelFont(font);
        plot.getDomainAxis().setTickLabelFont(font);
        plot.getRangeAxis().setLabelFont(font);
        plot.getRangeAxis().setTickLabelFont(font);

        final CustomStackAreaRenderer renderer = new CustomStackAreaRenderer();

        for (int i = 0; i < this.colors.length; i++)
        {
            renderer.setSeriesPaint(i, this.colors[i]);
        }

        plot.setRenderer(renderer);
        plot.setInsets(new RectangleInsets(15.0, 0, 0, 5.0));

        return chart;
    }

    /**
     * Private constructor
     * 
     * @param timestamp
     * @param graphWidth
     * @param graphHeight
     * @param categoryDataset
     * @param xAxislabel
     * @param yAxisLabel
     * @param colors
     */
    private FitnesseGraph(final long timestamp, final int graphWidth, final int graphHeight, final CategoryDataset categoryDataset, final String xAxislabel, final String yAxisLabel, final Color... colors)
    {
        super(timestamp, graphWidth, graphHeight);

        this.xAxisLabel = xAxislabel;
        this.yAxisLabel = yAxisLabel;
        this.colors = ((colors == null) || (colors.length == 0)) ? new Color[] {FitnesseGraph.DEFAULT_GRAPH_COLOR } : colors;
        this.categoryDataset = categoryDataset;
    }
}
