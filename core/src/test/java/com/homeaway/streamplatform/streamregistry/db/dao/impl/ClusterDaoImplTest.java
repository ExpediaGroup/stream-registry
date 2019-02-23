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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.ClusterDao;
import com.homeaway.streamplatform.streamregistry.dto.AvroToJsonDTO;
import com.homeaway.streamplatform.streamregistry.model.JsonCluster;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;

/**
 * The type Cluster dao impl test.
 */
public class ClusterDaoImplTest {

    /**
     * The Infra manager.
     */
    static InfraManager infraManager;
    /**
     * The Cluster dao.
     */
    static ClusterDao clusterDao;

    /**
     * The constant CLUSTER_NAME.
     */
    public static final String CLUSTER_NAME = "cluster.name";

    /**
     * The Cluster properties.
     */
    static Map<String, String> clusterProperties;


    private static JsonCluster.Value expectedClusterValue;

    /**
     * Setup method.
     */
    @BeforeClass
    public static void setup(){
        infraManager = mock(InfraManager.class);
        clusterDao = new ClusterDaoImpl(infraManager);

        clusterProperties = new HashMap<>();
        clusterProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        clusterProperties.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "localhost:2181");
        clusterProperties.put(CLUSTER_NAME, "cluster_name");
        clusterProperties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "localhost:8081");

        expectedClusterValue = JsonCluster.Value.builder()
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
    public void testGetCluster() {
        ClusterValue expectedClusterValue = new ClusterValue(clusterProperties);
        when(infraManager.getClusterByKey(any(ClusterKey.class))).thenReturn(Optional.of(expectedClusterValue));

        ClusterValue actualClusterValue = clusterDao.getCluster("vpc", "env", "hint", "");
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
        Map<JsonCluster.Key, JsonCluster.Value> expectedClusterMap = new HashMap<>();

        JsonCluster.Key jsonClusterKey = JsonCluster.Key.builder()
            .vpc("vpc")
            .env("env")
            .hint("hint")
            .type("")
            .build();

        expectedClusterMap.put(jsonClusterKey, expectedClusterValue);

        Map<ClusterKey, ClusterValue> avroClusterMap = new HashMap<>();

        avroClusterMap.put(new ClusterKey("vpc", "env", "hint", ""), new ClusterValue(clusterProperties));

        when(infraManager.getAllClusters()).thenReturn(avroClusterMap);
        Map<JsonCluster.Key, JsonCluster.Value> allClusters = clusterDao.getAllClusters();

        Assert.assertEquals(1, allClusters.size());
    }

    @Test
    public void testGetClusterByClusterName() {
        ClusterValue expectedClusterValue = new ClusterValue(clusterProperties);

        Map<ClusterKey, ClusterValue> expectedClusterMap = new HashMap<>();
        expectedClusterMap.put(new ClusterKey("vpc", "env", "hint", ""), expectedClusterValue);

        when(infraManager.getAllClusters()).thenReturn(expectedClusterMap);

        Optional<JsonCluster.Value> actualClusterValue = clusterDao.getCluster("cluster_name");

        JsonCluster.Value jsonClusterValue = AvroToJsonDTO.getJsonClusterValue(expectedClusterValue);

        Assert.assertTrue(actualClusterValue.isPresent());

        JsonCluster.Value value = actualClusterValue.get();

        Assert.assertEquals(jsonClusterValue.getBootstrapServers(), value.getBootstrapServers());
        Assert.assertEquals(jsonClusterValue.getSchemaRegistryURL(), value.getSchemaRegistryURL());

        Assert.assertEquals(jsonClusterValue.getClusterName(), value.getClusterName());
        Assert.assertEquals(jsonClusterValue.getZookeeperQuorum(), value.getZookeeperQuorum());
    }


    @Test
    public void testUpsertCluster() {
        ClusterKey clusterKey = new ClusterKey("vpc", "env", "hint", "");
        ClusterValue expectedClusterValue = new ClusterValue(clusterProperties);

        JsonCluster jsonCluster = JsonCluster.builder()
            .clusterKey(AvroToJsonDTO.getJsonClusterKey(clusterKey))
            .clusterValue(AvroToJsonDTO.getJsonClusterValue(expectedClusterValue))
            .build();

        clusterDao.upsertCluster(jsonCluster);

        ArgumentCaptor<ClusterValue> clusterValueArgumentCaptor = ArgumentCaptor.forClass(ClusterValue.class);

        verify(infraManager).upsertCluster(any(ClusterKey.class), clusterValueArgumentCaptor.capture());

        assertEquals("cluster_name", clusterValueArgumentCaptor.getValue().getClusterProperties().get(CLUSTER_NAME));
        assertEquals( "localhost:9092", clusterValueArgumentCaptor.getValue().getClusterProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }
}
