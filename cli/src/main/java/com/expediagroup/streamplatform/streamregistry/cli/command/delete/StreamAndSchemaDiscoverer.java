/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PACKAGE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import com.google.common.collect.Sets;

import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;

@RequiredArgsConstructor(access = PACKAGE)
class StreamAndSchemaDiscoverer {
  private final EntityClient client;

  Map<StreamKey, Optional<SchemaKey>> discover(String domain, String stream, Integer version) {
    Map<StreamKey, SchemaKey> candidates = client.getStreamKeyWithSchemaKeys(domain, stream, version, null, null);
    Map<SchemaKey, Set<StreamKey>> groupedCandidates = groupBySchema(candidates);

    Map<SchemaKey, Set<StreamKey>> groupedAll = groupedAll(groupedCandidates.keySet());

    Set<SchemaKey> deletableSchemas = groupedCandidates.entrySet().stream()
        .filter(groupedCandidate -> canDeleteSchema(groupedAll, groupedCandidate))
        .map(Entry::getKey)
        .collect(toSet());

    return candidates.entrySet().stream()
        .collect(toMap(
            Entry::getKey,
            e -> Optional.of(e.getValue()).filter(deletableSchemas::contains)
        ));
  }

  private Map<SchemaKey, Set<StreamKey>> groupedAll(Set<SchemaKey> candidateSchemas) {
    Map<StreamKey, SchemaKey> all = candidateSchemas.stream()
        .map(x -> client.getStreamKeyWithSchemaKeys(null, null, null, x.getDomainKey().getName(), x.getName()))
        .flatMap(x -> x.entrySet().stream())
        .collect(toMap(Entry::getKey, Entry::getValue));
    return groupBySchema(all);
  }

  private boolean canDeleteSchema(Map<SchemaKey, Set<StreamKey>> groupedAll, Entry<SchemaKey, Set<StreamKey>> groupedCandidate) {
    Set<StreamKey> candidateStreams = groupedCandidate.getValue();
    Set<StreamKey> allStreams = groupedAll.get(groupedCandidate.getKey());
    int remainingStreamsCount = Sets.difference(allStreams, candidateStreams).size();
    return remainingStreamsCount == 0;
  }

  private Map<SchemaKey, Set<StreamKey>> groupBySchema(Map<StreamKey, SchemaKey> streams) {
    return streams.entrySet().stream()
        .collect(groupingBy(
            Entry::getValue,
            mapping(Entry::getKey, toSet())
        ));
  }
}
