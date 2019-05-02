/* Copyright (c) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.expediagroup.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.expediagroup.streamplatform.streamregistry.dto.AvroToJsonDTO;
import com.expediagroup.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.expediagroup.streamplatform.streamregistry.exceptions.InvalidClusterException;
import com.expediagroup.streamplatform.streamregistry.model.JsonCluster;
import com.expediagroup.streamplatform.streamregistry.provider.InfraManager;
import com.expediagroup.streamplatform.streamregistry.service.impl.ClusterServiceImpl;

/**
 * The type Cluster dao impl test.
 */
public class ClusterServiceImplTest {

    /**
     * The Infra manager.
     */
    private static InfraManager INFRA_MANAGER;
    /**
     * The Cluster dao.
     */
    private static ClusterService clusterService;

    /**
     * The constant CLUSTER_NAME.
     */
    private static final String CLUSTER_NAME = "cluster.name";

    /**
     * The Cluster properties.
     */
    private static Map<String, String> clusterProperties;

    /**
     * Setup method.
     */
    @BeforeClass
    public static void setup() {
        INFRA_MANAGER = mock(InfraManager.class);
        clusterService = new ClusterServiceImpl(INFRA_MANAGER);

        clusterProperties = new HashMap<>();
        clusterProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        clusterProperties.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "localhost:2181");
        clusterProperties.put(CLUSTER_NAME, "cluster_name");
        clusterProperties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "localhost:8081");
    }

    /**
     * Test get cluster.
     */
    @Test
    public void testGetCluster() throws ClusterNotFoundException {
        com.expediagroup.streamplatform.streamregistry.ClusterValue expectedClusterValue = new com.expediagroup.streamplatform.streamregistry.ClusterValue(clusterProperties);
        when(INFRA_MANAGER.getClusterByKey(any(com.expediagroup.streamplatform.streamregistry.ClusterKey.class))).thenReturn(Optional.of(expectedClusterValue));

        com.expediagroup.streamplatform.streamregistry.ClusterValue actualClusterValue = clusterService.getCluster("vpc", "env", "hint", "");
        Assert.assertEquals(expectedClusterValue, actualClusterValue);

        // Change the cluster properties to make the assert fail
        Map<String, String> newClusterProperties = new HashMap<>();
        newClusterProperties.put(CLUSTER_NAME, "cluster_dao_test_primary_notwork");
        actualClusterValue.setClusterProperties(newClusterProperties);

        Assert.assertNotEquals(expectedClusterValue, actualClusterValue);
    }

    /**
     * Test get all clusters.
     */
    @Test
    public void testGetAllClusters() {
        Map<com.expediagroup.streamplatform.streamregistry.ClusterKey, com.expediagroup.streamplatform.streamregistry.ClusterValue> avroClusterMap = new HashMap<>();

        avroClusterMap.put(new com.expediagroup.streamplatform.streamregistry.ClusterKey("vpc", "env", "hint", ""), new com.expediagroup.streamplatform.streamregistry.ClusterValue(clusterProperties));

        when(INFRA_MANAGER.getAllClusters()).thenReturn(avroClusterMap);
        List<JsonCluster> allClusters = clusterService.getAllClusters();

        Assert.assertEquals(1, allClusters.size());
    }

    /**
     * Test upsert cluster.
     *
     * @throws InvalidClusterException - if the cluster fails to upsert
     */
    @Test
    public void testUpsertCluster() throws InvalidClusterException{
        com.expediagroup.streamplatform.streamregistry.ClusterKey clusterKey = new com.expediagroup.streamplatform.streamregistry.ClusterKey("vpc", "env", "hint", "");
        com.expediagroup.streamplatform.streamregistry.ClusterValue expectedClusterValue = new com.expediagroup.streamplatform.streamregistry.ClusterValue(clusterProperties);

        JsonCluster jsonCluster = JsonCluster.builder()
            .clusterKey(AvroToJsonDTO.getJsonClusterKey(clusterKey))
            .clusterValue(AvroToJsonDTO.getJsonClusterValue(expectedClusterValue))
            .build();

        clusterService.upsertCluster(jsonCluster);

        ArgumentCaptor<com.expediagroup.streamplatform.streamregistry.ClusterValue> clusterValueArgumentCaptor = ArgumentCaptor.forClass(com.expediagroup.streamplatform.streamregistry.ClusterValue.class);

        verify(INFRA_MANAGER).upsertCluster(any(com.expediagroup.streamplatform.streamregistry.ClusterKey.class), clusterValueArgumentCaptor.capture());

        assertEquals("cluster_name", clusterValueArgumentCaptor.getValue().getClusterProperties().get(CLUSTER_NAME));
        assertEquals("localhost:9092", clusterValueArgumentCaptor.getValue().getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }
}
