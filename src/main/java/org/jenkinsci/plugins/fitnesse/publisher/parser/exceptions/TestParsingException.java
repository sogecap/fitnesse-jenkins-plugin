package org.jenkinsci.plugins.fitnesse.publisher.parser.exceptions;

/**
 * Exception thrown if something goes awry during the parsing of FitNesse tests results
 * 
 */
public final class TestParsingException extends RuntimeException
{

    private static final long serialVersionUID = -1337813912026903302L;

    /**
     * Constructor
     * 
     * @param message explanatory message about the cause of the exception
     * @param cause originally thrown exception
     */
    public TestParsingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param cause originally thrown exception
     */
    public TestParsingException(final Throwable cause)
    {
        super(cause);
    }
}
