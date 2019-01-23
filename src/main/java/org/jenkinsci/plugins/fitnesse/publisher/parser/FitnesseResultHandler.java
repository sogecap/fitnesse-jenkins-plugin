package org.jenkinsci.plugins.fitnesse.publisher.parser;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.io.SAXContentHandler;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnesseResult;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * {@link SAXContentHandler} implementation which parses transformed XML FitNesse test results files
 * 
 */
public class FitnesseResultHandler extends DefaultHandler
{

    // Handler state

    private FitnesseResult summary;

    private final List<FitnesseResult> results = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        // ignore non-meaningful elements
        if (!ParseableElements.contains(qName))
        {
            return;
        }

        String targetPage = "";

        if (ParseableElements.SUMMARY.getName().equals(qName))
        {
            targetPage = this.getAttributeValue(attributes, ParseableAttributes.PAGE);
        }

        if (ParseableElements.DETAIL.getName().equals(qName))
        {
            final String page = this.getAttributeValue(attributes, ParseableAttributes.PAGE);
            targetPage = ((page == null) || page.isEmpty()) ? this.summary.getPage() : page;
        }

        // test results counts extraction
        final String rightStr = this.getAttributeValue(attributes, ParseableAttributes.RIGHT);
        final String wrongStr = this.getAttributeValue(attributes, ParseableAttributes.WRONG);
        final String ignoredStr = this.getAttributeValue(attributes, ParseableAttributes.IGNORED);
        final String exceptionsStr = this.getAttributeValue(attributes, ParseableAttributes.EXCEPTIONS);
        final String durationStr = this.getAttributeValue(attributes, ParseableAttributes.DURATION);

        // HTML content, when available
        final String htmlContentFile = this.getAttributeValue(attributes, ParseableAttributes.CONTENT);

        final int right = Integer.parseInt(rightStr);
        final int wrong = Integer.parseInt(wrongStr);
        final int ignored = Integer.parseInt(ignoredStr);
        final int exceptions = Integer.parseInt(exceptionsStr);
        final long duration = ((durationStr == null) || durationStr.isEmpty()) ? 0L : Long.parseLong(durationStr);

        // create the result and add it to the existing ones
        final FitnesseResult result = new FitnesseResult(targetPage, duration, right, wrong, ignored, exceptions, htmlContentFile);

        if (ParseableElements.SUMMARY.getName().equals(qName))
        {
            this.summary = result;
        } else
        {
            this.results.add(result);
        }
    }

    /**
     * @return tests execution summary
     */
    public FitnesseResult getSummary()
    {
        return this.summary;
    }

    /**
     * @return tests results
     */
    public List<FitnesseResult> getDetails()
    {
        return this.results;
    }

    /**
     * Retrieve an XML element's attribute value
     * 
     * @param attributes the found attributes
     * @param targetAttribute the searched attribute
     * @return the attribute's value, or {@code null} if it is not found
     */
    private String getAttributeValue(final Attributes attributes, final ParseableAttributes targetAttribute)
    {
        return attributes.getValue(targetAttribute.getName());
    }

    /**
     * XML elements needed for the parsing
     */
    private enum ParseableElements
    {
        SUMMARY("summary"), DETAIL("detail");

        private final String name;

        ParseableElements(final String name)
        {
            this.name = name;
        }

        /**
         * Checks if an element matches with one present in this enumeration
         * 
         * @param name name of the searched element
         * @return {@code true} if it is found, {@code false} otherwise
         */
        public static boolean contains(final String name)
        {
            for (final ParseableElements element : ParseableElements.values())
            {
                if (element.getName().equals(name))
                {
                    return true;
                }
            }

            return false;
        }

        public String getName()
        {
            return this.name;
        }
    }

    /**
     * XML attributes needed for the parsing
     */
    private enum ParseableAttributes
    {
        RIGHT("right"),
        WRONG("wrong"),
        IGNORED("ignored"),
        EXCEPTIONS("exceptions"),
        DURATION("duration"),
        PAGE("page"),
        NAME("name"),
        APPROX_RESULT_DATE("approxResultDate"),
        CONTENT("content");

        private final String name;

        ParseableAttributes(final String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }
    }
}
