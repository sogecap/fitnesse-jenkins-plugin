package org.jenkinsci.plugins.fitnesse.publisher;

import org.jenkinsci.plugins.fitnesse.publisher.DescriptorImpl;
import org.junit.Assert;
import org.junit.Test;

import org.jenkinsci.plugins.fitnesse.publisher.Messages;

import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

/**
 * {@link DescriptorImpl} tests
 * 
 */
public class DescriptorImplTest
{

    /** Tested class */
    private final DescriptorImpl descriptor = new DescriptorImpl();

    /** Valid XML results files pattern */
    @Test
    public void testValidXmlResultsPath()
    {
        final String value = "*-results.xml";

        final FormValidation result = this.descriptor.doCheckXmlResultsPath(value);

        Assert.assertEquals(Kind.OK, result.kind);
    }

    /** {@code null} or empty XML results files pattern */
    @Test
    public void testEmptyOrNullXmlResultsPath()
    {
        FormValidation result;

        result = this.descriptor.doCheckXmlResultsPath("");

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageResultsPublisher_errors_missingResultsPath(), result.getMessage());

        result = this.descriptor.doCheckXmlResultsPath(null);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageResultsPublisher_errors_missingResultsPath(), result.getMessage());
    }

    /** Incorrect XML results files pattern */
    @Test
    public void testInvalidXmlResultsPath()
    {
        final String value = "{{}}.xml";

        final FormValidation result = this.descriptor.doCheckXmlResultsPath(value);

        Assert.assertEquals(Kind.ERROR, result.kind);
        Assert.assertEquals(Messages.FitnessePageResultsPublisher_errors_invalidGlob(), result.getMessage());
    }
}
