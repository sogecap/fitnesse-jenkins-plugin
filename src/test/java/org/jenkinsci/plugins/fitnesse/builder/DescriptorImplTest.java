package org.jenkinsci.plugins.fitnesse.builder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jenkinsci.plugins.fitnesse.builder.DescriptorImpl;
import org.junit.Assert;
import org.junit.Test;

import org.jenkinsci.plugins.fitnesse.builder.Messages;

import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/** {@link DescriptorImpl} tests */
public class DescriptorImplTest
{

    /** Tested class */
    private final DescriptorImpl descriptor;

    /** Default constructor */
    public DescriptorImplTest()
    {
        this.descriptor = new DescriptorImpl();
    }

    /** Valid FitNesse URL */
    @Test
    public void testValidFitnesseUrl()
    {
        Assert.assertEquals(Kind.OK, this.descriptor.doCheckRemoteFitnesseUrl("http://remote.host.com:8080").kind);
    }

    /**
     * Invalid FitNesse URL
     * 
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testInvalidFitnesseUrl() throws UnsupportedEncodingException
    {
        final String invalidHost = "this.is.!.an.URL?";

        final FormValidation result = this.descriptor.doCheckRemoteFitnesseUrl(invalidHost);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(
                Util.escape(Messages.FitnessePageBuilder_errors_invalidRemoteURL(invalidHost)),
                result.getMessage());
    }

    /** {@code null} or empty FitNesse URL */
    @Test
    public void testEmptyOrNullFitnesseUrl()
    {
        FormValidation result;

        result = this.descriptor.doCheckRemoteFitnesseUrl("");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingRemoteURL(), result.getMessage());

        result = this.descriptor.doCheckRemoteFitnesseUrl(null);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingRemoteURL(), result.getMessage());
    }

    /**
     * Reachable FitNesse URL
     * 
     * @throws IOException
     */
    @Test
    public void testReachableFitnesseUrl() throws IOException
    {
        try (final MockWebServer mockServer = new MockWebServer())
        {
            final HttpUrl url = mockServer.url("/");
            mockServer.enqueue(new MockResponse().setResponseCode(200));

            final FormValidation result = this.descriptor.doTestConnection(url.toString());

            Assert.assertEquals(Kind.OK, result.kind);
            Assert.assertEquals(
                    Util.escape(Messages.FitnessePageBuilder_validRemoteURL(url)),
                    result.getMessage());
        }
    }

    /**
     * Unreachable FitNesse URL
     * 
     * @throws IOException
     */
    @Test
    public void testUnreachableFitnesseUrl() throws IOException
    {
        final String url = "http://localhost:1";
        final FormValidation result = this.descriptor.doTestConnection(url);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(
                Util.escape(Messages.FitnessePageBuilder_errors_unreachableRemoteURL(url)),
                result.getMessage());
    }

    /**
     * Reachable FitNesse URL returning an invalid HTTP status
     * 
     * @throws IOException
     */
    @Test
    public void testFitnesseUrlReturningInvalidHttpStatus() throws IOException
    {
        try (final MockWebServer mockServer = new MockWebServer())
        {
            final HttpUrl url = mockServer.url("/");
            mockServer.enqueue(new MockResponse().setResponseCode(500));

            final FormValidation result = this.descriptor.doTestConnection(url.toString());

            Assert.assertEquals(Kind.ERROR, result.kind);
            Assert.assertEquals(
                    Util.escape(Messages.FitnessePageBuilder_errors_unreachableRemoteURL(url)),
                    result.getMessage());
        }
    }

    /** Valid HTTP timeout */
    @Test
    public void testValidHttpTimeout()
    {
        Assert.assertEquals(Kind.OK, this.descriptor.doCheckHttpTimeout("120").kind);
    }

    /** Invalid HTTP timeout */
    @Test
    public void testInvalidHttpTimeout()
    {
        FormValidation result;

        String input = "-1";
        result = this.descriptor.doCheckHttpTimeout(input);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Util.escape(Messages.FitnessePageBuilder_errors_invalidHttpTimeoutRange(input)), result.getMessage());

        input = "test";
        result = this.descriptor.doCheckHttpTimeout(input);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Util.escape(Messages.FitnessePageBuilder_errors_invalidHttpTimeoutFormat()), result.getMessage());
    }

    /** Empty HTTP timeout */
    @Test
    public void testEmptyOrNullHttpTimeout()
    {
        FormValidation result;

        result = this.descriptor.doCheckHttpTimeout("");
        Assert.assertEquals(Kind.OK, result.kind);

        result = this.descriptor.doCheckHttpTimeout(null);
        Assert.assertEquals(Kind.OK, result.kind);
    }

    /** Valid target file */
    @Test
    public void testValidTargetFile()
    {
        Assert.assertEquals(Kind.OK, this.descriptor.doCheckTargetFile("test.txt").kind);
    }

    /** Empty target file */
    @Test
    public void testEmptyOrNullTargetFile()
    {
        FormValidation result = this.descriptor.doCheckTargetFile("");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingTargetFile(), result.getMessage());

        result = this.descriptor.doCheckTargetFile(null);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingTargetFile(), result.getMessage());
    }

    /** Valid FitNesse pages */
    @Test
    public void testValidTargetPages()
    {
        Assert.assertEquals(Kind.OK, this.descriptor.doCheckTargetPages("Test.Page1\nTest.Page2\nTest.Page3").kind);
    }

    /** Empty FitNesse pages */
    @Test
    public void testEmptyOrNullTargetPages()
    {
        FormValidation result = this.descriptor.doCheckTargetPages("");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingTargetPages(), result.getMessage());

        result = this.descriptor.doCheckTargetPages(null);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingTargetPages(), result.getMessage());
    }

    /** Valid FitNesse suite */
    @Test
    public void testValidTargetSuite()
    {
        Assert.assertEquals(Kind.OK, this.descriptor.doCheckTargetSuite("Test.Suite").kind);
    }

    /** Empty FitNesse suite */
    @Test
    public void testEmptyOrNullTargetSuite()
    {
        FormValidation result;

        result = this.descriptor.doCheckTargetSuite("");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingTargetSuite(), result.getMessage());

        result = this.descriptor.doCheckTargetSuite(null);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingTargetSuite(), result.getMessage());
    }

    /** Valide filename output format */
    @Test
    public void testValidFilenameOutputFormat()
    {
        Assert.assertEquals(Kind.OK, this.descriptor.doCheckFilenameOutputFormat("%s-fitnesse-results.txt").kind);
    }

    /** Invalid filename output format */
    @Test
    public void testInvalidFilenameOutputFormat()
    {
        FormValidation result;

        result = this.descriptor.doCheckFilenameOutputFormat("%d-fitnesse-results.txt");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_invalidFilenameOutputFormat(), result.getMessage());

        result = this.descriptor.doCheckFilenameOutputFormat("fitnesse-results.txt");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_invalidFilenameOutputFormat(), result.getMessage());
    }

    /** Empty filename output format */
    @Test
    public void testNullOrEmptyFilenameOutputFormat()
    {
        FormValidation result;

        result = this.descriptor.doCheckFilenameOutputFormat("");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingFilenameOutputFormat(), result.getMessage());

        result = this.descriptor.doCheckFilenameOutputFormat(null);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageBuilder_errors_missingFilenameOutputFormat(), result.getMessage());
    }
}