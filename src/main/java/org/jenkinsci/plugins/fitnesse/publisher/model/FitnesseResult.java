package org.jenkinsci.plugins.fitnesse.publisher.model;

import java.io.Serializable;

/**
 * POJO which holds the execution statistics of a FitNesse page or suite
 * 
 * <p>
 * This is meant to be instantiated by the parser only.
 * 
 */
public class FitnesseResult implements Serializable
{

    /** serialVersionUID */
    private static final long serialVersionUID = -8097412249692623102L;

    private final String page;

    private final long duration;

    private final int right;

    private final int wrong;

    private final int ignored;

    private final int exceptions;

    private String htmlContent;

    /**
     * Constructor
     * 
     * @param page targeted FitNesse page
     * @param duration test execution duration
     * @param right number of correct tests
     * @param wrong number of failed tests
     * @param ignored number of skipped tests
     * @param exceptions number of tests which threw an exception
     * @param htmlContent the captured HTML output (may be {@code null})
     */
    public FitnesseResult(final String page, final long duration, final int right, final int wrong, final int ignored, final int exceptions, final String htmlContent)
    {
        this.page = page;
        this.duration = duration;
        this.right = right;
        this.wrong = wrong;
        this.ignored = ignored;
        this.exceptions = exceptions;
        this.htmlContent = htmlContent;
    }

    /**
     * @return targeted FitNesse page
     */
    public String getPage()
    {
        return this.page;
    }

    /**
     * @return test execution duration
     */
    public long getDuration()
    {
        return this.duration;
    }

    /**
     * @return number of correct tests
     */
    public int getRight()
    {
        return this.right;
    }

    /**
     * @return number of failed tests
     */
    public int getWrong()
    {
        return this.wrong;
    }

    /**
     * @return number of skipped tests
     */
    public int getIgnored()
    {
        return this.ignored;
    }

    /**
     * @return number of tests which threw an exception
     */
    public int getExceptions()
    {
        return this.exceptions;
    }

    /**
     * @return the captured HTML output (may be {@code null})
     */
    public String getHtmlContent()
    {
        return this.htmlContent;
    }

    /**
     * @param htmlContent the captured HTML output associated with this result
     */
    public void setHtmlContent(final String htmlContent)
    {
        this.htmlContent = htmlContent;
    }

    /**
     * @return {@code true} if some captured HTML output is present, {@code false} otherwise
     */
    public boolean hasHtmlContent()
    {
        return this.htmlContent != null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return String.format(
                "%s: %s right, %s wrong, %s ignored, %s exceptions, in %s ms",
                this.page, this.right, this.wrong, this.ignored, this.exceptions, this.duration);
    }
}
