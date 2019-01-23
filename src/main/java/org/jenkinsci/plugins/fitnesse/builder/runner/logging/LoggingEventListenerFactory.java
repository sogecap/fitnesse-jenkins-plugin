package org.jenkinsci.plugins.fitnesse.builder.runner.logging;

import java.io.PrintStream;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.EventListener.Factory;

/**
 * Factory which instantiates {@link LoggingEventListener} from OkHttp {@link Call}s
 * 
 */
public class LoggingEventListenerFactory implements Factory
{

    private final PrintStream logger;

    /**
     * Constructor
     * 
     * @param logger Jenkins logger
     */
    public LoggingEventListenerFactory(final PrintStream logger)
    {
        this.logger = logger;
    }

    /** {@inheritDoc} */
    @Override
    public EventListener create(final Call call)
    {
        // remove leading forward slash
        final String targetedPage = call.request().url().url().getPath().substring(1);
        return new LoggingEventListener(targetedPage, this.logger);
    }
}
