/**
 * Copyright (C) 2018-2023 Expedia, Inc.
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
import static com.expediagroup.streamplatform.streamregistry.graphql.filters.StreamBindingFilter.matchesStreamBindingKey;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ProcessBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.SpecificationQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.StreamBindingKeyQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.ZoneKeyQuery;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

public class ProcessBindingFilter implements Predicate<ProcessBinding> {

  private final ProcessBindingKeyQuery keyQuery;
  private final SpecificationQuery specQuery;
  private final ZoneKeyQuery zoneKeyQuery;
  private final List<StreamBindingKeyQuery> inputQueries;
  private final List<StreamBindingKeyQuery> outputQueries;

  public ProcessBindingFilter(ProcessBindingKeyQuery keyQuery, SpecificationQuery specQuery, ZoneKeyQuery zoneKeyQuery,
                              List<StreamBindingKeyQuery> inputQueries, List<StreamBindingKeyQuery> outputQueries) {
    this.keyQuery = keyQuery;
    this.specQuery = specQuery;
    this.zoneKeyQuery = zoneKeyQuery;
    this.inputQueries = inputQueries;
    this.outputQueries = outputQueries;
  }

  @Override
  public boolean test(ProcessBinding processBinding) {
    return matchesProcessBindingKey(processBinding.getKey(), keyQuery)
      && matchesZone(processBinding.getZone(), zoneKeyQuery)
      && matchesInput(processBinding.getInputs(), inputQueries)
      && matchesOutput(processBinding.getOutputs(), outputQueries)
      && matchesSpecification(processBinding.getSpecification(), specQuery);
  }

  public static boolean matchesZone(ZoneKey zone, ZoneKeyQuery zoneKeyQuery) {
    if (zoneKeyQuery == null) {
      return true;
    }
    ZoneKey safeZone = (zone == null) ? new ZoneKey() : zone;
    return matches(safeZone.getName(), zoneKeyQuery.getNameRegex());
  }

  public static boolean matchesInput(List<ProcessInputStreamBinding> inputs, List<StreamBindingKeyQuery> inputQueries) {
    if (inputQueries == null || inputQueries.isEmpty()) {
      return true;
    }

    List<ProcessInputStreamBinding> safeInputs = (inputs == null) ? Collections.emptyList() : inputs;
    return inputQueries.stream().allMatch(inputQuery ->
      safeInputs.stream().anyMatch(input -> matchesStreamBindingKey(input.getStreamBindingKey(), inputQuery))
    );
  }

  public static boolean matchesOutput(List<ProcessOutputStreamBinding> outputs, List<StreamBindingKeyQuery> outputQueries) {
    if (outputQueries == null || outputQueries.isEmpty()) {
      return true;
    }

    List<ProcessOutputStreamBinding> safeOutputs = (outputs == null) ? Collections.emptyList() : outputs;
    return outputQueries.stream().allMatch(outputQuery ->
      safeOutputs.stream().anyMatch(output -> matchesStreamBindingKey(output.getStreamBindingKey(), outputQuery))
    );
  }

  public static boolean matchesProcessBindingKey(ProcessBindingKey key, ProcessBindingKeyQuery processBindingKeyQuery) {
    if (processBindingKeyQuery == null) {
      return true;
    }

    ProcessBindingKey safeKey = (key == null) ? new ProcessBindingKey() : key;
    return matches(safeKey.getDomainName(), processBindingKeyQuery.getDomainNameRegex())
        && matches(safeKey.getInfrastructureZone(), processBindingKeyQuery.getInfrastructureZoneRegex())
        && matches(safeKey.getProcessName(), processBindingKeyQuery.getProcessNameRegex());
  }
}
