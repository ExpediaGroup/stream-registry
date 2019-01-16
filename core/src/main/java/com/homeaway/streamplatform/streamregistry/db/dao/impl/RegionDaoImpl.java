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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.streamplatform.streamregistry.db.dao.RegionDao;
import com.homeaway.streamplatform.streamregistry.model.Hint;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;

/**
 * Specific implementation of {@link RegionDao} that uses {@link InfraManager}
 */
@Slf4j
public class RegionDaoImpl implements RegionDao {

    private final String env;

    private final InfraManager infraManager;

    /**
     * Instantiates a new Region dao.
     *
     * @param env the env
     * @param infraManager the infra manager
     */
    public RegionDaoImpl(String env, InfraManager infraManager) {
        this.env = env;
        this.infraManager = infraManager;
    }

    private Map<String, Set<String>> getHintToVpcsMap() {
        Set<ClusterKey> clusters = infraManager.getAllClusters().keySet();
        return clusters.stream()
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
