/* Copyright (c) 2018 Expedia Group.
 * All rights reserved.  http://www.homeaway.com

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
package com.homeaway.streamplatform.streamregistry.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import com.homeaway.digitalplatform.streamregistry.Actor;

public final class StreamRegistryUtils {
    public static <V> Map<Integer, List<V>> paginate(List<V> list, int pageSize) {
        if(pageSize > 0) {
            final int size = list.size();

            return IntStream.iterate(0, i -> i + pageSize)
                    .limit((size + pageSize - 1) / pageSize)
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> i / pageSize,
                            i -> list.subList(i, Integer.min(i + pageSize, size))));
        }
        else return emptyPage();
    }

    public static <V> EntriesPage<V> toEntriesPage(List<V> page, Integer total, Integer pageSize, Integer pageNumber) {
        if (pageSize <= 0) {
            total = 0;
            pageSize = 0;
            pageNumber = 0;
            page = Collections.emptyList();
        }

        return EntriesPage.<V>builder()
                .page(pageNumber)
                .pageSize(pageSize)
                .total(total)
                .entries(page)
                .build();
    }

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @JsonDeserialize(builder = EntriesPage.EntriesPageBuilder.class)
    public static final class EntriesPage<V> {
        @NonNull private final Integer page;
        @NonNull private final Integer pageSize;
        @NonNull private final Integer total;
        @NonNull private final List<V> entries;

        @JsonPOJOBuilder(withPrefix = "")
        public static final class EntriesPageBuilder<V> {}
    }

    public static <V> Map<Integer, List<V>> emptyPage() {
        Map<Integer, List<V>> empty = new HashMap<>();
        empty.put(0, Collections.emptyList());

        return empty;
    }

    public static final boolean hasActorNamed(String name, Supplier<Actor> actorSupplier) {
        return name != null && getActorName(name, actorSupplier).isPresent();
    }

    public static final Optional<String> getActorName(String name, Supplier<Actor> actorSupplier) {
        return Optional.ofNullable(actorSupplier)
                .map(Supplier::get)
                .map(Actor::getName)
                .filter(actorName -> actorName.equalsIgnoreCase(name));
    }
}