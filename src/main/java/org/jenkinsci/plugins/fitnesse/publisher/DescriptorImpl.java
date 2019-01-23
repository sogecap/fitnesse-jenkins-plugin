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
package org.jenkinsci.plugins.fitnesse.publisher;

import java.nio.file.FileSystems;
import java.util.regex.PatternSyntaxException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

/**
 * Holds the configuration values of {@link FitnesseResultsPublisher}, and provides
 * basic validation of the user-submitted input values
 * 
 * @see FitnesseResultsPublisher
 */
@Symbol("fitnessePublisher")
@Extension
public class DescriptorImpl extends BuildStepDescriptor<Publisher>
{

    /**
     * Default constructor
     */
    public DescriptorImpl()
    {
        super(FitnesseResultsPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName()
    {
        return Messages.FitnessePageResultsPublisher_displayName();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> aClass)
    {
        return true;
    }

    /**
     * Xml results file pattern validation
     * 
     * @param value valeur saisie par l'utilisateur
     * @return le résultat de la validation
     */
    public FormValidation doCheckXmlResultsPath(@QueryParameter final String value)
    {
        if ((value == null) || value.isEmpty())
        {
            return FormValidation.error(Messages.FitnessePageResultsPublisher_errors_missingResultsPath());
        }

        try
        {
            FileSystems.getDefault().getPathMatcher(String.format("glob:%s", value));
        } catch (final PatternSyntaxException pse)
        {
            return FormValidation.error(
                    String.format("%s: %s",
                            Messages.FitnessePageResultsPublisher_errors_invalidGlob(),
                            pse.getMessage()));
        }

        return FormValidation.ok();
    }
}
