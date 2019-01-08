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
package com.homeaway.streamingplatform.streamregistry.db.dao.impl;

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

import com.homeaway.streamingplatform.streamregistry.configuration.KafkaProducerConfig;
import com.homeaway.streamingplatform.streamregistry.exceptions.StreamCreationException;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({KafkaManagerImpl.class, AdminUtils.class})
public class KafkaManagerImplTest {

    private static final String TOPIC = "kafka-manager-test";
    private static final int PARTITIONS = 2;
    private static final int REPLICATION_FACTOR = 3;
    private static final Properties PROPS = new Properties();
    private static final Properties FILTERED_PROPS = new Properties();
    private static final Properties TOPIC_PROPS = new Properties();
    private static final Properties TOPIC_WITH_CNXN_PROPS = new Properties();

    @Mock private ZkUtils zkUtils;
    @Mock private ZkClient zkClient;
    @Mock private ZkConnection zkConnection;

    @InjectMocks
    private KafkaManagerImpl kafkaManager = new KafkaManagerImpl();

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws Exception{
        PROPS.setProperty("key1", "val1");
        PROPS.setProperty("key2", "2");  // Properties should be strings only... not ints
        PROPS.setProperty(KafkaProducerConfig.ZOOKEEPER_QUORUM, "127.0.0.1:2181");
        PROPS.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        FILTERED_PROPS.setProperty("key1", "val1");
        FILTERED_PROPS.setProperty("key2", "2");
        TOPIC_PROPS.setProperty("key1", "actualVal1"); // different from "val1"
        TOPIC_PROPS.setProperty("key2", "2");
        TOPIC_WITH_CNXN_PROPS.putAll(TOPIC_PROPS);
        TOPIC_WITH_CNXN_PROPS.setProperty(KafkaProducerConfig.ZOOKEEPER_QUORUM, "127.0.0.1:2181");
        TOPIC_WITH_CNXN_PROPS.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");

        // setup kafkaManager
        // kafkaManager.init(PROPS);
        // un-necessary since powermock has setup mocks for zookeeper in KafkaManagerImpl

        // make sure that the * direct-caller * of these classes are in @PrepareForTest annotation above on this test
        whenNew(ZkClient.class).withAnyArguments().thenReturn(zkClient);
        whenNew(ZkConnection.class).withArguments(Mockito.anyString()).thenReturn(zkConnection);
        whenNew(ZkUtils.class).withAnyArguments().thenReturn(zkUtils);

        // using power mock to allow for mocking of static classes
        mockStatic(AdminUtils.class);
        when(AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), TOPIC)).thenReturn(TOPIC_PROPS);
    }

    @Test(expected = StreamCreationException.class)
    public void testUpsertTopicsForNewStream(){
        // Mock it as an existing topic
        when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(true);

        //New Stream
        kafkaManager.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, true);

        // expecting an exception to be thrown because topic exists but request doesn't match the config
    }

    @Test
    public void testUpsertTopicsForExistingStreamWithMatchingConfig() {
        // Mock it as an existing topic
        when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(true);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);

        // Existing Stream, but PROPS match!! should not have an exception
        kafkaManagerSpy.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, TOPIC_WITH_CNXN_PROPS, true);

        // verify change topic DOES NOT HAPPEN because props match
        verifyStatic(AdminUtils.class, times(0));
        AdminUtils.changeTopicConfig(zkUtils, TOPIC, TOPIC_PROPS);

        // verify create topic DOES NOT HAPPEN because props match
        verifyStatic(AdminUtils.class, times(0));
        AdminUtils.createTopic(zkUtils, TOPIC, PARTITIONS, REPLICATION_FACTOR, TOPIC_PROPS, RackAwareMode.Enforced$.MODULE$);
    }

    @Test
    public void testUpsertTopicsForExistingStream() {
        // Mock it as an existing topic
        when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(true);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);

        //Existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, false);

        //verify change topic happens because isNewStream=false
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

        // Not existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, true);

        //verify create topic happens when requested topic does not exist
        verifyStatic(AdminUtils.class, times(0));
        AdminUtils.changeTopicConfig(zkUtils, TOPIC, FILTERED_PROPS);
        verifyStatic(AdminUtils.class, times(1));
        AdminUtils.createTopic(zkUtils, TOPIC, PARTITIONS, REPLICATION_FACTOR, FILTERED_PROPS, RackAwareMode.Enforced$.MODULE$);
    }

    @Test
    public void testUpsertTopicsForNewTopicExistsInSR() {
        // Mock it as a new topic
        when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(false);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);

        //Existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(TOPIC), PARTITIONS, REPLICATION_FACTOR, PROPS, false);

        // note: this is a weird case, because somehow the stream exists in SR, but the underlying topic does NOT
        // might want to consider this corner case a bit more. Currently the behavior honors the request and creates the topic
        // with the requested config

        //verify create topic happens when requested topic does not exist
        verifyStatic(AdminUtils.class, times(0));
        AdminUtils.changeTopicConfig(zkUtils, TOPIC, FILTERED_PROPS);
        verifyStatic(AdminUtils.class, times(1));
        AdminUtils.createTopic(zkUtils, TOPIC, PARTITIONS, REPLICATION_FACTOR, FILTERED_PROPS, RackAwareMode.Enforced$.MODULE$);
    }
}
