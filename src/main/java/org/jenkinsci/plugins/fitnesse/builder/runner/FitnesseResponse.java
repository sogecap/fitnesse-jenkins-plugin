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
package org.jenkinsci.plugins.fitnesse.builder.runner;

import java.io.Serializable;

/**
 * POJO that encapsulates a FitNesse page execution response
 * 
 */
public class FitnesseResponse implements Serializable
{

    private static final long serialVersionUID = -5187666078633930941L;

    private final String page;

    private final String content;

    /**
     * Create a new Fitnesse response
     * 
     * @param page name of the FitNesse page that was run
     * @param content body of the FitNesse response
     */
    public FitnesseResponse(final String page, final String content)
    {
        this.page = page;
        this.content = content;
    }

    /**
     * @return name of the FitNesse page that was run
     */
    public String getPage()
    {
        return this.page;
    }

    /**
     * @return body of the FitNesse response
     */
    public String getContent()
    {
        return this.content;
    }
}
