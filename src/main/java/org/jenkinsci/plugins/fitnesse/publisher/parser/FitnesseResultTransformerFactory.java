package org.jenkinsci.plugins.fitnesse.publisher.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import org.jenkinsci.plugins.fitnesse.publisher.parser.exceptions.TestParsingException;

/**
 * Factory which instantiates the {@link Transformer} needed to execute the XSLT
 * transform on the FitNesse tests results prior to their parsing
 * 
 */
public final class FitnesseResultTransformerFactory
{

    /** Name of the file containg the XSLT transform */
    private static final String FITNESSE_RESULTS_XSL = "fitnesse-results.xsl";

    /**
     * Instantiate a new configured {@link Transformer}
     * 
     * @return a {@link Transformer} instance
     * @throws TransformerFactoryConfigurationError
     */
    public static Transformer newInstance()
    {
        Transformer transformer;

        try (InputStream transform = FitnesseResultTransformerFactory.class.getResourceAsStream(FitnesseResultTransformerFactory.FITNESSE_RESULTS_XSL))
        {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformer = transformerFactory.newTransformer(new StreamSource(transform));
        } catch (final IOException | TransformerConfigurationException | TransformerFactoryConfigurationError e)
        {
            throw new TestParsingException("Could not initialize XLST transformer", e);
        }

        return transformer;
    }

    /** no-op */
    private FitnesseResultTransformerFactory()
    {
        // no-op
    }
}
