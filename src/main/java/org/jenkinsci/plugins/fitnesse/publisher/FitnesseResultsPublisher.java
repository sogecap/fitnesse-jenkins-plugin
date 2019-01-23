package org.jenkinsci.plugins.fitnesse.publisher;

import java.io.IOException;
import java.io.PrintStream;

import org.jenkinsci.plugins.fitnesse.publisher.actions.FitnesseResultsAction;
import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

/**
 * Post-build {@link Publisher} which parses FitNesse test results files and provides
 * a user-facing presentation of these results via a build {@link Action}, similar to
 * Jenkins' JUnit plugin
 * 
 */
public final class FitnesseResultsPublisher extends Recorder implements SimpleBuildStep
{

    private String xmlResultsGlob;

    private boolean markBuildAsUnstable;

    private boolean compressHtmlOutput;

    /**
     * Constructor
     * 
     * @param xmlResultsGlob pattern used to match the FitNesse results files
     * @param markBuildAsUnstable whether to mark the build as unstable if there are any FitNesse tests failures
     */
    public FitnesseResultsPublisher(final String xmlResultsGlob, final boolean markBuildAsUnstable)
    {
        this.xmlResultsGlob = xmlResultsGlob;
        this.markBuildAsUnstable = markBuildAsUnstable;
    }

    /**
     * Default constructor
     */
    @DataBoundConstructor
    public FitnesseResultsPublisher()
    {
        // no-op
    }

    /** {@inheritDoc} */
    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace, final Launcher launcher, final TaskListener listener) throws InterruptedException, IOException
    {
        final PrintStream logger = listener.getLogger();

        logger.printf("Publishing FitNesse results report...%n");

        // parse FitNesse result files
        final long buildTime = build.getTimestamp().getTimeInMillis();
        final long timeOnMaster = System.currentTimeMillis();
        final FilePath masterBuildDirectory = new FilePath(build.getRootDir());

        final AggregatedFitnesseResult results = workspace.act(new TestsParsingCallable(
                this.xmlResultsGlob,
                buildTime,
                timeOnMaster, 
                masterBuildDirectory,
                this.compressHtmlOutput,
                listener));

        // mark the build as unstable if there are any test failures and the user asked for it
        if (this.getMarkBuildAsUnstable() && (results.getFailCount() > 0))
        {
            final Result buildResult = Result.UNSTABLE;
            logger.printf("FitNesse test results contain failures, marking build as %s%n", buildResult);
            build.setResult(buildResult);
        }

        logger.printf("Successfully published FitNesse tests report.%n");

        // add the resulting action to the current build
        final FitnesseResultsAction action = new FitnesseResultsAction(build, results);
        results.setParentAction(action);
        build.addAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public DescriptorImpl getDescriptor()
    {
        return (DescriptorImpl) super.getDescriptor();
    }

    /** {@inheritDoc} */
    @Override
    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
    }

    /**
     * @return the XML results files pattern
     */
    public String getXmlResultsPath()
    {
        return this.xmlResultsGlob;
    }

    /**
     * @return {@code true} if tests failures should mark the build as unstable, {@code false} otherwise
     */
    public boolean getMarkBuildAsUnstable()
    {
        return this.markBuildAsUnstable;
    }

    /**
     * @return {@code true} if the captured HTML output should be compressed, {@code false} otherwise
     */
    public boolean getCompressHtmlOutput()
    {
        return this.compressHtmlOutput;
    }

    /**
     * @param xmlResultsPath
     */
    @DataBoundSetter
    public void setXmlResultsPath(final String xmlResultsPath)
    {
        this.xmlResultsGlob = xmlResultsPath;
    }

    /**
     * @param markBuildAsUnstable
     */
    @DataBoundSetter
    public void setMarkBuildAsUnstable(final boolean markBuildAsUnstable)
    {
        this.markBuildAsUnstable = markBuildAsUnstable;
    }

    /**
     * @param compressHtmlOutput
     */
    @DataBoundSetter
    public void setCompressHtmlOutput(final boolean compressHtmlOutput)
    {
        this.compressHtmlOutput = compressHtmlOutput;
    }
}
