/* Copyright (c) 2018-Present Expedia Group.
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
package com.homeaway.streamplatform.streamregistry.service;

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

import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.dto.AvroToJsonDTO;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.InvalidClusterException;
import com.homeaway.streamplatform.streamregistry.model.ClusterKey;
import com.homeaway.streamplatform.streamregistry.model.ClusterValue;
import com.homeaway.streamplatform.streamregistry.model.JsonCluster;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.service.ClusterService;
import com.homeaway.streamplatform.streamregistry.service.impl.ClusterServiceImpl;

/**
 * The type Cluster dao impl test.
 */
public class ClusterServiceImplTest {

    /**
     * The Infra manager.
     */
    static InfraManager infraManager;
    /**
     * The Cluster dao.
     */
    static ClusterService clusterService;

    /**
     * The constant CLUSTER_NAME.
     */
    public static final String CLUSTER_NAME = "cluster.name";

    /**
     * The Cluster properties.
     */
    static Map<String, String> clusterProperties;


    private static ClusterValue expectedClusterValue;

    /**
     * Setup method.
     */
    @BeforeClass
    public static void setup() {
        infraManager = mock(InfraManager.class);
        clusterService = new ClusterServiceImpl(infraManager);

        clusterProperties = new HashMap<>();
        clusterProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        clusterProperties.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "localhost:2181");
        clusterProperties.put(CLUSTER_NAME, "cluster_name");
        clusterProperties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "localhost:8081");

        expectedClusterValue = ClusterValue.builder()
            .bootstrapServers("localhost:9092")
            .schemaRegistryURL("localhost:8081")
            .zookeeperQuorum("localhost:2181")
            .clusterName("cluster_name")
            .build();
    }

    /**
     * Test get cluster.
     */
    @Test
    public void testGetCluster() throws ClusterNotFoundException {
        com.homeaway.digitalplatform.streamregistry.ClusterValue expectedClusterValue = new com.homeaway.digitalplatform.streamregistry.ClusterValue(clusterProperties);
        when(infraManager.getClusterByKey(any(com.homeaway.digitalplatform.streamregistry.ClusterKey.class))).thenReturn(Optional.of(expectedClusterValue));

        com.homeaway.digitalplatform.streamregistry.ClusterValue actualClusterValue = clusterService.getCluster("vpc", "env", "hint", "");
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
        Map<ClusterKey, ClusterValue> expectedClusterMap = new HashMap<>();

        ClusterKey jsonClusterKey = ClusterKey.builder()
            .vpc("vpc")
            .env("env")
            .hint("hint")
            .type("")
            .build();

        expectedClusterMap.put(jsonClusterKey, expectedClusterValue);

        Map<com.homeaway.digitalplatform.streamregistry.ClusterKey, com.homeaway.digitalplatform.streamregistry.ClusterValue> avroClusterMap = new HashMap<>();

        avroClusterMap.put(new com.homeaway.digitalplatform.streamregistry.ClusterKey("vpc", "env", "hint", ""), new com.homeaway.digitalplatform.streamregistry.ClusterValue(clusterProperties));

        when(infraManager.getAllClusters()).thenReturn(avroClusterMap);
        List<JsonCluster> allClusters = clusterService.getAllClusters();

        Assert.assertEquals(1, allClusters.size());
    }

    /**
     * Test upsert cluster.
     *
     * @throws InvalidClusterException the invalid cluster exception
     */
    @Test
    public void testUpsertCluster() throws InvalidClusterException{
        com.homeaway.digitalplatform.streamregistry.ClusterKey clusterKey = new com.homeaway.digitalplatform.streamregistry.ClusterKey("vpc", "env", "hint", "");
        com.homeaway.digitalplatform.streamregistry.ClusterValue expectedClusterValue = new com.homeaway.digitalplatform.streamregistry.ClusterValue(clusterProperties);

        JsonCluster jsonCluster = JsonCluster.builder()
            .clusterKey(AvroToJsonDTO.getJsonClusterKey(clusterKey))
            .clusterValue(AvroToJsonDTO.getJsonClusterValue(expectedClusterValue))
            .build();

        clusterService.upsertCluster(jsonCluster);

        ArgumentCaptor<com.homeaway.digitalplatform.streamregistry.ClusterValue> clusterValueArgumentCaptor = ArgumentCaptor.forClass(com.homeaway.digitalplatform.streamregistry.ClusterValue.class);

        verify(infraManager).upsertCluster(any(com.homeaway.digitalplatform.streamregistry.ClusterKey.class), clusterValueArgumentCaptor.capture());

        assertEquals("cluster_name", clusterValueArgumentCaptor.getValue().getClusterProperties().get(CLUSTER_NAME));
        assertEquals("localhost:9092", clusterValueArgumentCaptor.getValue().getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }
}
