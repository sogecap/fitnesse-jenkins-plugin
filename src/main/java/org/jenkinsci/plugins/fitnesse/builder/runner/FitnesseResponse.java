package org.jenkinsci.plugins.fitnesse.builder.runner;

import java.io.Serializable;

/**
 * POJO that encapsulates a FitNesse page execution response
 * 
 */
public class FitnesseResponse implements Serializable
{

    private static final long serialVersionUID = -5187666078633930941L;

    private final String page;

    private final String content;

    /**
     * Create a new Fitnesse response
     * 
     * @param page name of the FitNesse page that was run
     * @param content body of the FitNesse response
     */
    public FitnesseResponse(final String page, final String content)
    {
        this.page = page;
        this.content = content;
    }

    /**
     * @return name of the FitNesse page that was run
     */
    public String getPage()
    {
        return this.page;
    }

    /**
     * @return body of the FitNesse response
     */
    public String getContent()
    {
        return this.content;
    }
}
