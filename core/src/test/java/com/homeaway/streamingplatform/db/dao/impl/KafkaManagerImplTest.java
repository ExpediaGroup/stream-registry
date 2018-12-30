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
package com.homeaway.streamingplatform.db.dao.impl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.*;

import java.util.Collections;
import java.util.Properties;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.homeaway.streamingplatform.configuration.KafkaProducerConfig;
import com.homeaway.streamingplatform.exceptions.StreamCreationException;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({KafkaManagerImpl.class, AdminUtils.class})
public class KafkaManagerImplTest {

    private static final String TOPIC = "kafka-manager-test";
    private static final int PARTITIONS = 2;
    private static final int REPLICATION_FACTOR = 3;
    private static final Properties PROPS = new Properties();
    private static final Properties FILTERED_PROPS = new Properties();

    @Mock private ZkUtils zkUtils;
    @Mock private ZkClient zkClient;
    @Mock private ZkConnection zkConnection;

    @InjectMocks
    private KafkaManagerImpl kafkaManager = new KafkaManagerImpl();

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws Exception{
        PROPS.put("key1", "val1");
        PROPS.put("key2", 2);
        PROPS.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "127.0.0.1:2181");
        PROPS.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        FILTERED_PROPS.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "127.0.0.1:2181");
        FILTERED_PROPS.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");

        // setup kafkaManager
        // kafkaManager.init(PROPS);
        // un-necessary since powermock has setup mocks for zookeeper in KafkaManagerImpl

        // make sure that the * direct-caller * of these classes are in @PrepareForTest annotation above on this test
        whenNew(ZkClient.class).withAnyArguments().thenReturn(zkClient);
        whenNew(ZkConnection.class).withArguments(Mockito.anyString()).thenReturn(zkConnection);
        whenNew(ZkUtils.class).withAnyArguments().thenReturn(zkUtils);

        // using power mock to allow for mocking of static classes
        mockStatic(AdminUtils.class);
        when(AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), TOPIC)).thenReturn(PROPS);
    }

    @Test(expected = StreamCreationException.class)
    public void testUpsertTopicsForNewStream(){
        // Mock it as an existing topic
        when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(true);

        //New Stream
        kafkaManager.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, true);
    }

    @Test
    public void testUpsertTopicsForExistingStream() {
        // Mock it as an existing topic
        when(AdminUtils.topicExists(eq(zkUtils), eq(TOPIC))).thenReturn(true);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);

        //Existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, false);

        //verify change topic happens only when requested config is exact
        verifyStatic(AdminUtils.class, times(1));
        AdminUtils.changeTopicConfig(zkUtils, TOPIC, FILTERED_PROPS);
        verifyStatic(AdminUtils.class, times(0));
        AdminUtils.createTopic(zkUtils, TOPIC, PARTITIONS, REPLICATION_FACTOR, FILTERED_PROPS, RackAwareMode.Enforced$.MODULE$);
    }


    @Test
    public void testUpsertTopicsForNewTopic() {
        // Mock it as a new topic
        when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(false);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);

        //Existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, false);

        //verify create topic happens when requested topic does not exist
        verifyStatic(AdminUtils.class, times(0));
        AdminUtils.changeTopicConfig(zkUtils, TOPIC, FILTERED_PROPS);
        verifyStatic(AdminUtils.class, times(1));
        AdminUtils.createTopic(zkUtils, TOPIC, PARTITIONS, REPLICATION_FACTOR, FILTERED_PROPS, RackAwareMode.Enforced$.MODULE$);
    }
}
