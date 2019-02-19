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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.homeaway.digitalplatform.streamregistry.ClusterKey;
import com.homeaway.digitalplatform.streamregistry.ClusterValue;
import com.homeaway.streamplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamplatform.streamregistry.db.dao.ClusterDao;
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
        clusterProperties.put(CLUSTER_NAME, "cluster_dao_test_primary");
        clusterProperties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "localhost:8081");
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
        ClusterValue expectedClusterValue = new ClusterValue(clusterProperties);

        Map<ClusterKey, ClusterValue> expectedClusterMap = new HashMap<>();
        expectedClusterMap.put(new ClusterKey("vpc", "env", "hint", ""), expectedClusterValue);

        when(infraManager.getAllClusters()).thenReturn(expectedClusterMap);
        Map<ClusterKey, ClusterValue> allClusters = clusterDao.getAllClusters();

        Assert.assertEquals(1, allClusters.size());
    }

    @Test
    public void testGetClusterByClusterName() {
        ClusterValue expectedClusterValue = new ClusterValue(clusterProperties);

        Map<ClusterKey, ClusterValue> expectedClusterMap = new HashMap<>();
        expectedClusterMap.put(new ClusterKey("vpc", "env", "hint", ""), expectedClusterValue);

        when(infraManager.getAllClusters()).thenReturn(expectedClusterMap);

        Optional<ClusterValue> actualClusterValue = clusterDao.getCluster("cluster_dao_test_primary");
        Assert.assertEquals(expectedClusterValue, actualClusterValue.get());
    }

}
