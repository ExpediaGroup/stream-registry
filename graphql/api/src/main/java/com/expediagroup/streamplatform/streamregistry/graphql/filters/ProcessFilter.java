/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matches;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.FilterUtility.matchesSpecification;
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamFilter.matchesStreamKey;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProcessKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

public class ProcessFilter implements Predicate<Process> {

  private final ProcessKeyQuery keyQuery;
  private final SpecificationQuery specQuery;
  private final List<ZoneKeyQuery> zoneKeyQueries;
  private final List<StreamKeyQuery> inputQueries;
  private final List<StreamKeyQuery> outputQueries;

  public ProcessFilter(ProcessKeyQuery keyQuery, SpecificationQuery specQuery, List<ZoneKeyQuery> zoneKeyQueries,
                       List<StreamKeyQuery> inputQueries, List<StreamKeyQuery> outputQueries) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
    this.zoneKeyQueries = zoneKeyQueries;
    this.inputQueries = inputQueries;
    this.outputQueries = outputQueries;
  }

  @Override
  public boolean test(Process process) {
    return matchesProcessKey(process.getKey(), keyQuery)
      && matchesZone(process.getZones(), zoneKeyQueries)
      && matchesInput(process.getInputs(), inputQueries)
      && matchesOutput(process.getOutputs(), outputQueries)
      && matchesSpecification(process.getSpecification(), specQuery);
  }

  public static boolean matchesZone(List<ZoneKey> zones, List<ZoneKeyQuery> zoneKeyQueries) {
    if (zoneKeyQueries == null || zoneKeyQueries.isEmpty()) {
      return true;
    }

    List<ZoneKey> defaultedZones = (zones == null) ? Collections.emptyList() : zones;
    return zoneKeyQueries.stream().allMatch(zoneKeyQuery ->
      defaultedZones.stream().anyMatch(zone -> matches(zone.getName(), zoneKeyQuery.getNameRegex()))
    );
  }

  public static boolean matchesInput(List<ProcessInputStream> inputs, List<StreamKeyQuery> streamKeyQueries) {
    if (streamKeyQueries == null || streamKeyQueries.isEmpty()) {
      return true;
    }

    List<ProcessInputStream> safeInputs = (inputs == null) ? Collections.emptyList() : inputs;
    return streamKeyQueries.stream().allMatch(streamKeyQuery ->
      safeInputs.stream().anyMatch(input -> matchesStreamKey(input.getStream(), streamKeyQuery))
    );
  }

  public static boolean matchesOutput(List<ProcessOutputStream> outputs, List<StreamKeyQuery> streamKeyQueries) {
    if (streamKeyQueries == null || streamKeyQueries.isEmpty()) {
      return true;
    }

    List<ProcessOutputStream> safeOutputs = (outputs == null) ? Collections.emptyList() : outputs;
    return streamKeyQueries.stream().allMatch(streamKeyQuery ->
      safeOutputs.stream().anyMatch(output -> matchesStreamKey(output.getStream(), streamKeyQuery))
    );
  }

  public static boolean matchesProcessKey(ProcessKey key, ProcessKeyQuery processKeyQuery) {
    if (processKeyQuery == null) {
      return true;
    }

    ProcessKey safeKey = (key == null) ? new ProcessKey() : key;
    return matches(safeKey.getDomain(), processKeyQuery.getDomainRegex())
        && matches(safeKey.getName(), processKeyQuery.getNameRegex());
  }
}
