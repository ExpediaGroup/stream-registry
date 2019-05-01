/* Copyright (c) 2018-Present Expedia Group.
 * All rights reserved.  http://www.expediagroup.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.Actor;
import com.expediagroup.streamplatform.streamregistry.Consumer;

public class StreamRegistryUtilsTest {

    @Test
    public void testPagination_IdealCase() {
        // Ideal case
        final int pageSize = 3;
        final int numElements = 12;
        List<Integer> elements = getTestList(numElements - 1);

        Map<Integer, List<Integer>> pages = StreamRegistryUtils.paginate(elements, pageSize);

        int numPages = (numElements / pageSize);
        List<Integer> obtained = pages.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        // Test number of pages
        assertTrue(String.format("Num pages should be %d", numPages), pages.keySet().size() == numPages);
        assertTrue(String.format("Obtained list %s should be equal to defined %s", obtained, elements), obtained.equals(elements));

        List<StreamRegistryUtils.EntriesPage<Integer>> entriesPages = pages.entrySet()
                .stream().map(e -> StreamRegistryUtils.toEntriesPage(e.getValue(), numElements, pageSize, e.getKey()))
                .collect(Collectors.toList());

        List<Integer> obtainedFromEntriesPages = entriesPages.stream().flatMap(e -> e.getEntries().stream()).collect(Collectors.toList());

        assertTrue(String.format("Obtained list %s should be equal to defined %s", obtainedFromEntriesPages, elements), obtainedFromEntriesPages.equals(elements));
    }

    @Test
    public void testPagination_InvalidPageSizeCase() {
        // Invalid case
        final int pageSize = 0; // Invalid page size
        final int numElements = 12;
        List<Integer> elements = getTestList(numElements - 1);

        Map<Integer, List<Integer>> pages = StreamRegistryUtils.paginate(elements, pageSize);

        assertTrue("Result should be an empty page!", StreamRegistryUtils.emptyPage().equals(pages));

        List<StreamRegistryUtils.EntriesPage<Integer>> entriesPages = pages.entrySet()
                .stream().map(e -> StreamRegistryUtils.toEntriesPage(e.getValue(), numElements, pageSize, e.getKey()))
                .collect(Collectors.toList());

        StreamRegistryUtils.EntriesPage<Integer> obtained = entriesPages.get(0);
        StreamRegistryUtils.EntriesPage<Integer> empty = StreamRegistryUtils.EntriesPage.<Integer>builder()
                .page(0)
                .pageSize(0)
                .total(0)
                .entries(Collections.emptyList())
                .build();

        assertTrue("Result should be an empty page!",
                obtained.equals(empty));
    }

    public static List<Integer> getTestList(int numElements) {
        return IntStream.iterate(0, i -> i + 1)
                .limit(numElements + 1)
                .boxed()
                .collect(Collectors.toList());
    }

    @Test
    public void testHasConsumerActor() {
        final String existentActorName = "FOO-NAME";
        final String nonExistentActorName = "NON-EXISTEN-FOO-NAME";

        Consumer consumer1 = Consumer.newBuilder()
                .setActor(Actor.newBuilder()
                        .setName(existentActorName)
                        .build())
                .build();

        // Ideal existence case
        assertTrue("Actor " + existentActorName + " should exists in consumer", StreamRegistryUtils.hasActorNamed(existentActorName, consumer1::getActor));

        // Ideal non existence case
        assertFalse("Actor " + nonExistentActorName + " should not exists in consumer", StreamRegistryUtils.hasActorNamed(nonExistentActorName, consumer1::getActor));

        // Null consumer case
        assertFalse("Actor " + nonExistentActorName + " should not exists in consumer", StreamRegistryUtils.hasActorNamed(nonExistentActorName, null));

        // Null name case
        assertFalse("Actor " + nonExistentActorName + " should not exists in consumer", StreamRegistryUtils.hasActorNamed(null, consumer1::getActor));

        // All null case
        assertFalse("Actor " + nonExistentActorName + " should not exists in consumer", StreamRegistryUtils.hasActorNamed(null, null));
    }
}
