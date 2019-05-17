/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.expediagroup.streamplatform.streamregistry.model.ClusterKey;
import com.expediagroup.streamplatform.streamregistry.model.Hint;
import com.expediagroup.streamplatform.streamregistry.service.RegionService;
import com.expediagroup.streamplatform.streamregistry.repository.InfraManager;

/**
 * Specific implementation of {@link RegionService} that uses {@link InfraManager}
 */
@Slf4j
public class RegionServiceImpl implements RegionService {

  private final String env;

  private final InfraManager infraManager;

  /**
   * Instantiates a new Region dao.
   *
   * @param env          the env
   * @param infraManager the infra manager
   */
  public RegionServiceImpl(String env, InfraManager infraManager) {
    this.env = env;
    this.infraManager = infraManager;
  }

  private Map<String, Set<String>> getHintToVpcsMap() {
    return infraManager
        .getAllClusters()
        .keySet()
        .stream()
        .filter((ClusterKey key) -> env.equalsIgnoreCase(key.getEnv()))
        .collect(Collectors.groupingBy(ClusterKey::getHint,
            Collectors.mapping(ClusterKey::getVpc, Collectors.toSet())));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Hint> getHints() {
    return getHintToVpcsMap().entrySet().stream()
        .map((hintVpcsEntry) -> {
          String hint = hintVpcsEntry.getKey();
          Set<String> vpcs = hintVpcsEntry.getValue();
          return Hint.builder().hint(hint).vpcs(vpcs).build();
        })
        .collect(Collectors.toSet());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getSupportedRegions(String hint) {
    return getHintToVpcsMap().get(hint);
  }
}
