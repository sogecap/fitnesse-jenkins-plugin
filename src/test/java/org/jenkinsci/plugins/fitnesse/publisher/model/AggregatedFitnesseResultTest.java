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

import org.jenkinsci.plugins.fitnesse.publisher.model.AggregatedFitnesseResult;
import org.jenkinsci.plugins.fitnesse.publisher.model.FitnessePageResult;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;

/**
 * {@link AggregatedFitnesseResult} tests
 * 
 */
public class AggregatedFitnesseResultTest
{

    /**
     * Find a child result that matches a given id
     */
    @Test
    public void testFindCorrespondingResultWithMatchingChild()
    {
        // given

        final AggregatedFitnesseResult result = new AggregatedFitnesseResult();

        final Run<?, ?> mockBuild = Mockito.mock(Run.class);

        final AbstractTestResultAction<?> mockParentAction = Mockito.mock(AbstractTestResultAction.class);
        mockParentAction.run = mockBuild;

        final FitnessePageResult mockChild = Mockito.mock(FitnessePageResult.class);
        Mockito.when(mockChild.findCorrespondingResult(ArgumentMatchers.anyString())).thenReturn(mockChild);

        result.setParentAction(mockParentAction);
        result.addChild(mockChild);

        // when

        final TestResult found = result.findCorrespondingResult("foo");

        // then

        Mockito.verify(mockChild).findCorrespondingResult(ArgumentMatchers.anyString());

        Assert.assertEquals(mockChild, found);
    }

    /**
     * Find a child result that does not match a given id
     */
    @Test
    public void testFindCorrespondingResultWithoutMatchingChild()
    {
        // given

        final AggregatedFitnesseResult result = new AggregatedFitnesseResult();

        final FitnessePageResult mockChild = Mockito.mock(FitnessePageResult.class);
        Mockito.when(mockChild.findCorrespondingResult(ArgumentMatchers.anyString())).thenReturn(null);

        result.addChild(mockChild);

        // when

        final TestResult found = result.findCorrespondingResult("foo");

        // then

        Mockito.verify(mockChild).findCorrespondingResult(ArgumentMatchers.anyString());

        Assert.assertNull(found);
    }

    /**
     * Find a child result without any existing child result
     */
    @Test
    public void testFindCorrespondingResultWithEmptyChildren()
    {
        final AggregatedFitnesseResult result = new AggregatedFitnesseResult();

        final TestResult found = result.findCorrespondingResult("foo");

        Assert.assertNull(found);
    }

    /**
     * Find a child result with a {@code null} id
     */
    @Test
    public void testFindCorrespondingResultWithNullId()
    {
        final AggregatedFitnesseResult result = new AggregatedFitnesseResult();

        final TestResult found = result.findCorrespondingResult(null);

        Assert.assertEquals(result, found);
    }

    /**
     * Find a child result with a given id that matches that of the top-level result
     */
    @Test
    public void testFindCorrespondingResultWithTopLevelId()
    {
        final AggregatedFitnesseResult result = new AggregatedFitnesseResult();

        final TestResult found = result.findCorrespondingResult("(empty)");

        Assert.assertEquals(result, found);
    }
}
