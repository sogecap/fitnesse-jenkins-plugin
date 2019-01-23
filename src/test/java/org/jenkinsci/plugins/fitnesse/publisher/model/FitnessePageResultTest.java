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
package org.jenkinsci.plugins.fitnesse.publisher.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.jenkinsci.plugins.fitnesse.publisher.model.FitnessePageResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnesseResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import org.jenkinsci.plugins.fitnesse.publisher.model.Messages;

/**
 * {@link FitnessePageResult} tests
 * 
 */
public class FitnessePageResultTest
{

    /** Enables the creation of temporary file/folders during tests */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Read captured HTML content from the filesystem
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void getHtmlContent() throws IOException, InterruptedException
    {
        // given

        final File expectedContentFile = this.tempFolder.newFile();
        Files.write(expectedContentFile.toPath(), "hello world".getBytes(StandardCharsets.UTF_8));

        final FitnesseResult counts = Mockito.mock(FitnesseResult.class);
        Mockito.when(counts.getHtmlContent()).thenReturn(expectedContentFile.getAbsolutePath());

        final FitnessePageResult result = new FitnessePageResult(counts);
        final String expectedContent = new String(Files.readAllBytes(expectedContentFile.toPath()), StandardCharsets.UTF_8);

        // when

        final String actualContent = result.getHtmlContent();

        // then

        Mockito.verify(counts).getHtmlContent();

        Assert.assertEquals(expectedContent, actualContent);
    }

    /**
     * Read non-existent HTML content
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void getEmptyHtmlContent() throws IOException, InterruptedException
    {
        // given

        final FitnesseResult counts = Mockito.mock(FitnesseResult.class);
        Mockito.when(counts.getHtmlContent()).thenReturn(null);

        final FitnessePageResult result = new FitnessePageResult(counts);

        // when

        final String actualContent = result.getHtmlContent();

        // then

        Mockito.verify(counts).getHtmlContent();

        Assert.assertEquals(String.format("<p>%s</p>", Messages.FitnessePageResults_noContent()), actualContent);
    }

    /**
     * Get the details link without existing HTML content
     */
    @Test
    public void getDetailsLinkWithNoContent()
    {
        // given

        final FitnesseResult counts = Mockito.mock(FitnesseResult.class);
        Mockito.when(counts.getHtmlContent()).thenReturn(null);

        final FitnessePageResult result = new FitnessePageResult(counts);

        // when

        final String actualLink = result.getDetailsLink();

        // then

        Mockito.verify(counts).getHtmlContent();

        Assert.assertNull(actualLink);
    }

    /**
     * Get the details link with existing HTML content
     *
     * @throws IOException
     */
    @Test
    public void getDetailsLink() throws IOException
    {
        // given

        final String page = "page";
        final FitnesseResult counts = Mockito.mock(FitnesseResult.class);
        Mockito.when(counts.getHtmlContent()).thenReturn("foo");
        Mockito.when(counts.getPage()).thenReturn(page);

        final FitnessePageResult result = new FitnessePageResult(counts);

        // when

        final String actualLink = result.getDetailsLink();

        // then

        Mockito.verify(counts).getHtmlContent();

        Assert.assertEquals(String.format("<a href=\"%1$s\">%1$s</a>", page), actualLink);
    }
}
