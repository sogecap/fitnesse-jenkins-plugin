/*
 * Copyright (C) 2019 Société Générale.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
