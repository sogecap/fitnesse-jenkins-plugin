package org.jenkinsci.plugins.fitnesse.builder.runner.exceptions;

/**
 * Exception thrown if something goes wrong during the execution of FitNesse tests results
 * 
 */
public final class TestExecutionException extends RuntimeException
{

    private static final long serialVersionUID = -7784025308171354125L;

    /**
     * Constructor
     * 
     * @param message explanatory message about the cause of the exception
     * @param cause originally thrown exception
     */
    public TestExecutionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param message explanatory message about the cause of the exception
     */
    public TestExecutionException(final String message)
    {
        super(message);
    }
}
