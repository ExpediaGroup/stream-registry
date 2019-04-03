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
package com.homeaway.streamplatform.streamregistry;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.homeaway.streamplatform.streamregistry.configuration.EventStoreTopic;

public class StreamRegistryApplicationTest {

    private String topicName = "eventStoreV1";

    private final StreamRegistryApplication streamRegistryApplication = new StreamRegistryApplication();
    private AdminClient adminClient = Mockito.mock(AdminClient.class);

    @Test
    public void testInstanceOfWhenIsTopicExistThrowsUnknownTopicOrPartitionException() throws InterruptedException, ExecutionException {
        KafkaFuture<TopicDescription> topicDescriptionFuture = mockDescribeTopics();
        ExecutionException executionException = new ExecutionException(new UnknownTopicOrPartitionException("Unknown topic name"));
        Mockito.when(topicDescriptionFuture.get()).thenThrow(executionException);

        boolean isKafkaTopicPresent = streamRegistryApplication.isKafkaTopicPresent(adminClient, topicName);
        Assert.assertFalse(isKafkaTopicPresent);
    }

    @Test(expected = RuntimeException.class)
    public void testInstanceOfWhenIsTopicExistThrowsDifferentException() throws InterruptedException, ExecutionException {
        KafkaFuture<TopicDescription> topicDescriptionFuture = mockDescribeTopics();
        ExecutionException executionException = new ExecutionException(new kafka.common.UnknownTopicOrPartitionException("Unknown topic name"));
        Mockito.when(topicDescriptionFuture.get()).thenThrow(executionException);

        streamRegistryApplication.isKafkaTopicPresent(adminClient, topicName);
    }

    @Test(expected = RuntimeException.class)
    public void isTopicExistsThrowsRuntimeException() throws InterruptedException, ExecutionException {
        KafkaFuture<TopicDescription> topicDescriptionFuture = mockDescribeTopics();
        Mockito.when(topicDescriptionFuture.get()).thenThrow(new RuntimeException());

        streamRegistryApplication.isKafkaTopicPresent(adminClient, topicName);
    }

    @Test
    public void testConfigMatch() throws ExecutionException, InterruptedException {
        Map<String, String> expectedConfig = new HashMap<>();
        expectedConfig.put("cleanup.policy", "compact");
        expectedConfig.put("min.insync.replicas", "1");
        EventStoreTopic expectedEventStoreTopic = EventStoreTopic.builder().name(topicName).properties(expectedConfig).build();

        KafkaFuture<Config> configFuture = mockDescribeConfigs();
        Collection<ConfigEntry> entries = new ArrayList<>();
        entries.add(new ConfigEntry("cleanup.policy", "compact"));
        entries.add(new ConfigEntry("min.insync.replicas", "1"));
        Mockito.when(configFuture.get()).thenReturn(new Config(entries));

        streamRegistryApplication.validateTopicConfigs(adminClient, expectedEventStoreTopic);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingConfigMatch() throws ExecutionException, InterruptedException {
        Map<String, String> expectedConfig = new HashMap<>();
        expectedConfig.put("cleanup.policy", "compact");
        expectedConfig.put("min.insync.replicas", "1");
        EventStoreTopic expectedEventStoreTopic = EventStoreTopic.builder().name(topicName).properties(expectedConfig).build();

        KafkaFuture<Config> configFuture = mockDescribeConfigs();
        Collection<ConfigEntry> entries = new ArrayList<>();
        entries.add(new ConfigEntry("min.insync.replicas", "1"));
        Mockito.when(configFuture.get()).thenReturn(new Config(entries));

        streamRegistryApplication.validateTopicConfigs(adminClient, expectedEventStoreTopic);
    }

    private KafkaFuture<TopicDescription> mockDescribeTopics() {
        DescribeTopicsResult describeTopicResult = Mockito.mock(DescribeTopicsResult.class);
        Mockito.when(adminClient.describeTopics(Mockito.anyCollection())).thenReturn(describeTopicResult);
        Map<String, KafkaFuture<TopicDescription>> values = Mockito.mock(Map.class);
        Mockito.when(describeTopicResult.values()).thenReturn(values);
        KafkaFuture<TopicDescription> topicDescription = Mockito.mock(KafkaFuture.class);
        Mockito.when(values.get(Mockito.anyString())).thenReturn(topicDescription);
        return topicDescription;
    }

    private KafkaFuture<Config> mockDescribeConfigs() {
        DescribeConfigsResult describeConfigResult = Mockito.mock(DescribeConfigsResult.class);
        Mockito.when(adminClient.describeConfigs(Mockito.anyCollection())).thenReturn(describeConfigResult);
        Map<ConfigResource, KafkaFuture<Config>> values = Mockito.mock(Map.class);
        Mockito.when(describeConfigResult.values()).thenReturn(values);
        KafkaFuture<Config> config = Mockito.mock(KafkaFuture.class);
        Mockito.when(values.get(Mockito.any(ConfigResource.class))).thenReturn(config);
        return config;
    }
}
