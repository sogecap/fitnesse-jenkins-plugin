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
package org.jenkinsci.plugins.fitnesse.builder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.IllegalFormatException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Holds the configuration values of {@link FitnesseResultsBuilder}, and provides
 * basic validation of the user-submitted input values
 * 
 * @see FitnesseResultsBuilder
 */
@Symbol("fitnesseBuilder")
@Extension
public class DescriptorImpl extends BuildStepDescriptor<Builder>
{

    /**
     * Default constructor
     */
    public DescriptorImpl()
    {
        super(FitnesseResultsBuilder.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
        return Messages.FitnessePageBuilder_displayName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType)
    {
        return true;
    }

    /**
     * FitNesse URL validation
     * 
     * @param value user-submitted value
     * @return validation result
     */
    public FormValidation doCheckRemoteFitnesseUrl(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_missingRemoteURL());
        }

        try
        {
            new URL(value);
        } catch (final MalformedURLException e)
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_invalidRemoteURL(value));
        }

        return FormValidation.ok();
    }

    /**
     * FitNesse URL connection validation
     * 
     * @param value user-submitted value
     * @return validation result
     * @throws MalformedURLException if the remote FitNesse URL is malformed
     */
    public FormValidation doTestConnection(@QueryParameter("remoteFitnesseUrl") final String value) throws MalformedURLException
    {
        final URL rootUrl = new URL(String.join("/", value, "root"));
        final Request request = new Request.Builder().url(rootUrl).build();

        try (Response response = new OkHttpClient().newCall(request).execute())
        {
            return response.code() == 200 ? FormValidation.ok(Messages.FitnessePageBuilder_validRemoteURL(value))
                                          : FormValidation.error(Messages.FitnessePageBuilder_errors_unreachableRemoteURL(value));
        } catch (final IOException e)
        {
            return FormValidation.error(
                    String.format("%s: %s",
                            Messages.FitnessePageBuilder_errors_unreachableRemoteURL(value),
                            e.getMessage()));
        }
    }

    /**
     * HTTP timeout validation
     * 
     * @param value user-submitted value
     * @return validation result
     */
    public FormValidation doCheckHttpTimeout(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.ok();
        }

        final Integer timeout;

        try
        {
            timeout = Integer.valueOf(value);
        } catch (final NumberFormatException nfe)
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_invalidHttpTimeoutFormat());
        }

        if (timeout < 0)
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_invalidHttpTimeoutRange(value));
        }

        return FormValidation.ok();
    }

    /**
     * FitNesse targets validation
     * 
     * @param value user-submitted value
     * @return validation result
     */
    public FormValidation doCheckTargetPages(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_missingTargetPages());
        }

        return FormValidation.ok();
    }

    /**
     * FitNesse suite validation
     * 
     * @param value user-submitted value
     * @return validation result
     */
    public FormValidation doCheckTargetSuite(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_missingTargetSuite());
        }

        return FormValidation.ok();
    }

    /**
     * FitNesse targets file validation
     * 
     * @param value user-submitted value
     * @return validation result
     */
    public FormValidation doCheckTargetFile(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_missingTargetFile());
        }

        return FormValidation.ok();
    }

    /**
     * Output filenames pattern validation
     * 
     * @param value user-submitted value
     * @return validation result
     */
    public FormValidation doCheckFilenameOutputFormat(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_missingFilenameOutputFormat());
        }

        final String fakePage = "FitNessePageOrSuite";
        String out;

        try
        {
            out = String.format(value, fakePage);
        } catch (final IllegalFormatException ife)
        {
            return FormValidation.error(
                    String.format("%s: %s",
                            Messages.FitnessePageBuilder_errors_invalidFilenameOutputFormat(),
                            ife.getMessage()));
        }

        if (!out.contains(fakePage))
        {
            return FormValidation.error(Messages.FitnessePageBuilder_errors_invalidFilenameOutputFormat());
        }

        return FormValidation.ok();
    }
}
