package org.jenkinsci.plugins.fitnesse.publisher.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

/**
 * Top-level test result which contains all the child results which were
 * produced by the FitNesse tests execution
 * 
 */
public class AggregatedFitnesseResult extends TabulatedResult
{

    /** serialVersionUID */
    private static final long serialVersionUID = -5263885043599086391L;

    private transient AbstractTestResultAction<?> parentAction;

    private final List<FitnessePageResult> children;

    private long duration;

    private int passCount;

    private int skipCount;

    private int failCount;

    private int failOnlyCount;

    private int exceptionCount;

    /**
     * Default constructor
     */
    public AggregatedFitnesseResult()
    {
        this.children = new ArrayList<>();
        this.duration = 0;
        this.passCount = 0;
        this.skipCount = 0;
        this.failCount = 0;
        this.failOnlyCount = 0;
        this.exceptionCount = 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
        return Messages.AggregatedFitnessePageResults_title();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle()
    {
        return this.getDisplayName();
    }

    /** {@inheritDoc} */
    @Override
    public float getDuration()
    {
        return BigDecimal
                .valueOf(this.duration)
                .divide(BigDecimal.valueOf(1000))
                .floatValue();
    }

    /** {@inheritDoc} */
    @Override
    public int getPassCount()
    {
        return this.passCount;
    }

    /** {@inheritDoc} */
    @Override
    public int getSkipCount()
    {
        return this.skipCount;
    }

    /** {@inheritDoc} */
    @Override
    public int getFailCount()
    {
        return this.failCount;
    }

    /**
     * @return number of failed assertions, not due to an exception
     */
    public int getFailOnlyCount()
    {
        return this.failOnlyCount;
    }

    /**
     * @return number of failed assertions, due to an exception
     */
    public int getExceptionCount()
    {
        return this.exceptionCount;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends TestResult> getChildren()
    {
        return this.children;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public TestObject getParent()
    {
        return null; // this is the top-level test result
    }

    /** {@inheritDoc} */
    @Override
    public TestResult findCorrespondingResult(final String id)
    {
        // does the given id matches the top-level result ?
        if (this.getId().equals(id) || (id == null))
        {
            return this;
        }

        if (this.hasChildren())
        {
            // append the given child id to the top-level result id, if applicable
            final String childId = ((this.getId() == null) || this.getId().isEmpty()) ? id : String.join("/", this.getId(), id);

            // look for a matching result among all child results
            for (final TestResult child : this.getChildren())
            {
                // this is a child result, so we know its type
                final FitnessePageResult result = (FitnessePageResult) child.findCorrespondingResult(childId);

                // a mathcing result was found
                if (result != null)
                {
                    // reattach the build owning this result
                    result.setRun(this.getRun());
                    return result;
                }
            }
        }

        // no corresponding result was found
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp)
    {
        return this.findCorrespondingResult(token);
    }

    /** {@inheritDoc} */
    @Override
    public Run<?, ?> getRun()
    {
        return this.parentAction == null ? null : this.parentAction.run;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractTestResultAction getParentAction()
    {
        return this.parentAction;
    }

    /** {@inheritDoc} */
    @Override
    public void setParentAction(final AbstractTestResultAction action)
    {
        this.parentAction = action;
    }

    /** {@inheritDoc} */
    @Override
    public Result getBuildResult()
    {
        return this.children
                .stream()
                .anyMatch(FitnessePageResult::hasFailures) ? Result.UNSTABLE
                    : Result.SUCCESS;
    }

    /** {@inheritDoc} */
    @Override
    public void tally()
    {
        this.passCount = 0;
        this.skipCount = 0;
        this.failCount = 0;
        this.failOnlyCount = 0;
        this.exceptionCount = 0;

        // sum the counts across all the child results
        for (final TestResult result : this.getChildren())
        {
            this.passCount += result.getPassCount();
            this.failCount += result.getFailCount();
            this.skipCount += result.getSkipCount();

            if (result instanceof FitnessePageResult)
            {
                this.failOnlyCount += ((FitnessePageResult) result).getFailOnlyCount();
                this.exceptionCount += ((FitnessePageResult) result).getExceptionCount();
            }
        }
    }

    /**
     * @return the total number of pages which were executed
     */
    public int getTotalPages()
    {
        return this.children
                .stream()
                .mapToInt(r -> 1)
                .sum();
    }

    /**
     * @return the number of pages which were executed successfully
     */
    public int getPassedPages()
    {
        return this.children
                .stream()
                .filter(FitnessePageResult::isPassed)
                .mapToInt(r -> 1)
                .sum();
    }

    /**
     * @return the number of pages which contain failing assertions
     */
    public int getFailedPages()
    {
        return this.children
                .stream()
                .filter(FitnessePageResult::hasFailures)
                .mapToInt(r -> 1)
                .sum();
    }

    /**
     * @return the number of pages whose execution was skipped
     */
    public int getSkippedPages()
    {
        return this.children
                .stream()
                .filter(r -> !r.hasFailures() && !r.isPassed() && r.hasSkipped())
                .mapToInt(r -> 1)
                .sum();
    }

    /**
     * Add a child result to this top-level result
     * 
     * @param aChild a child result
     */
    public void addChild(final FitnessePageResult aChild)
    {
        this.children.add(aChild);
        aChild.setParent(this);
    }

    /**
     * @param duration the total execution duration of the child results
     */
    public void setDuration(final long duration)
    {
        this.duration = duration;
    }
}
