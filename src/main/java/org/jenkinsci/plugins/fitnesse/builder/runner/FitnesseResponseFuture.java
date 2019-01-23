package org.jenkinsci.plugins.fitnesse.builder.runner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.jenkinsci.plugins.fitnesse.builder.runner.exceptions.TestExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Bridge between OkHttp's {@link Callback} and {@link CompletableFuture} for FitNesse responses
 * 
 * <p>
 * Completes the future exceptionally in the event of a failed networking call or an incorrect HTTP response.
 * 
 */
public class FitnesseResponseFuture implements Callback
{

    private final String targetPage;

    private final CompletableFuture<FitnesseResponse> future;

    /**
     * Initialize a future for the given FitNesse page
     * 
     * @param targetPage the targeted FitNesse page for this call
     */
    public FitnesseResponseFuture(final String targetPage)
    {
        this.targetPage = targetPage;
        this.future = new CompletableFuture<>();
    }

    /** {@inheritDoc} */
    @Override
    public void onFailure(final Call call, final IOException e)
    {
        this.future.completeExceptionally(
                new TestExecutionException(
                        String.format("<< Error while executing HTTP request for page %s: %s",
                                this.targetPage, e.getMessage()),
                        e));
    }

    /** {@inheritDoc} */
    @Override
    public void onResponse(final Call call, final Response response) throws IOException
    {
        try (Response res = response)
        {
            if (!res.isSuccessful())
            {
                this.future.completeExceptionally(
                        new TestExecutionException(
                                String.format("<< Incorrect HTTP response for page \"%s\": %d - %s",
                                        this.targetPage, response.code(), response.message())));
            } else
            {
                this.future.complete(new FitnesseResponse(this.targetPage, response.body().string()));
            }
        }
    }

    /**
     * @return the {@link Future} corresponding to the asynchronous HTTP call
     */
    public CompletableFuture<FitnesseResponse> getFuture()
    {
        return this.future;
    }
}
