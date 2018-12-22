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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Collections;
import java.util.Properties;

import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.homeaway.streamingplatform.configuration.KafkaProducerConfig;
import com.homeaway.streamingplatform.exceptions.StreamCreationException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KafkaManagerImpl.class, AdminUtils.class})
public class KafkaManagerImplTest {

    private static final String topic = "kafka-manager-test";
    private static final int partitions = 2;
    private static final int replicationFactor = 3;
    private static Properties props = new Properties();

    @Mock private ZkUtils zkUtils;
    @Mock private ZkClient zkClient;
    @Mock private ZkConnection zkConnection;

    @InjectMocks
    private KafkaManagerImpl kafkaManager;

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() throws Exception{
        props.put("key1", "val1");
        props.put("key2", 2);
        props.put(KafkaProducerConfig.ZOOKEEPER_QUORUM, "");

        kafkaManager = new KafkaManagerImpl();

        mockStatic(AdminUtils.class);

        whenNew(ZkClient.class).withAnyArguments().thenReturn(zkClient);
        whenNew(ZkConnection.class).withArguments(Mockito.anyString()).thenReturn(zkConnection);
        whenNew(ZkUtils.class).withAnyArguments().thenReturn(zkUtils);

        when(AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topic)).thenReturn(props);
    }

    @Test(expected = StreamCreationException.class)
    public void testUpsertTopicsForNewStream(){
        // Mock it as an existing topic
        when(AdminUtils.topicExists(zkUtils, topic)).thenReturn(true);

        //New Stream
        kafkaManager.upsertTopics(Collections.singleton(topic), partitions, replicationFactor, props, true);
        // FIXME!
    }

    @Test
    public void testUpsertTopicsForExistingStream() {
        // Mock it as an existing topic
        when(AdminUtils.topicExists(zkUtils, topic)).thenReturn(true);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);
        doNothing().doThrow(new RuntimeException()).when(kafkaManagerSpy).createTopics(Mockito.anyCollection(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.anyMap());

        //Existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(topic), partitions, replicationFactor, props, false);

        //Assert if 0 topic is added to the list to be created
        //Assert.assertEquals(0,kafkaManagerSpy.topicsToCreate.size());
        // FIXME!
    }


    @Test
    public void testUpsertTopicsForNewTopic() {
        // Mock it as a new topic
        when(AdminUtils.topicExists(zkUtils, topic)).thenReturn(false);

        KafkaManagerImpl kafkaManagerSpy = spy(kafkaManager);
        doNothing().doThrow(new RuntimeException()).when(kafkaManagerSpy).createTopics(Mockito.anyCollection(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.anyMap());

        //Existing Stream
        kafkaManagerSpy.upsertTopics(Collections.singleton(topic), partitions, replicationFactor, props, false);

        //Assert if 1 topic is added to the list to be created
        //Assert.assertEquals(1, kafkaManagerSpy.topicsToCreate.size());
        // FIXME!
    }
}
