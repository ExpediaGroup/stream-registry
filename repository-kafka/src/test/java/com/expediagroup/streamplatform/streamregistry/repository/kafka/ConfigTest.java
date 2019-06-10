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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static java.util.stream.Collectors.toList;
import static kafka.log.LogConfig.CleanupPolicyProp;
import static kafka.log.LogConfig.Compact;
import static org.apache.kafka.common.config.ConfigResource.Type.TOPIC;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigTest {
  private final String bootstrapServers = "bootstrapServers";
  private final String topic = "topic";
  private final short replicationFactor = 1;
  private final String schemaRegistryUrl = "schemaRegistryUrl";
  @Mock
  private AdminClient client;

  private Config underTest;

  @Before
  public void before() {
    underTest = new Config(
        bootstrapServers,
        topic,
        replicationFactor,
        schemaRegistryUrl,
        client);
  }

  @Test
  public void topicExists() throws Exception {
    stubDescribeTopics(1);
    describeConfigs(true);

    underTest.verifyOrCreateTopic();

    verify(client, never()).createTopics(any());
  }

  @Test(expected = IllegalStateException.class)
  public void topicExistsButNotOnePartition() throws Exception {
    stubDescribeTopics(2);

    underTest.verifyOrCreateTopic();

    verify(client, never()).createTopics(any());
  }

  @Test(expected = IllegalStateException.class)
  public void topicExistsButNotNotCompact() throws Exception {
    stubDescribeTopics(1);
    describeConfigs(false);

    underTest.verifyOrCreateTopic();

    verify(client, never()).createTopics(any());
  }

  @Test
  public void topicCreated() throws Exception {
    stubDescribeTopicsError();
    CreateTopicsResult createTopicsResult = mock(CreateTopicsResult.class);
    when(client.createTopics(any())).thenReturn(createTopicsResult);
    when(createTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(null));

    underTest.verifyOrCreateTopic();

    ArgumentCaptor<List<NewTopic>> captor = ArgumentCaptor.forClass(List.class);
    verify(client).createTopics(captor.capture());
    List<NewTopic> topics = captor.getValue();

    assertThat(topics.size(), is(1));
    NewTopic topic = topics.get(0);
    assertThat(topic.name(), is("topic"));
    assertThat(topic.numPartitions(), is(1));
    assertThat(topic.replicationFactor(), is((short) 1));
    assertThat(topic.configs(), is(Map.of(CleanupPolicyProp(), Compact())));
  }

  private void stubDescribeTopics(int size) {
    DescribeTopicsResult describeTopicsResult = mock(DescribeTopicsResult.class);
    when(client.describeTopics(List.of(topic))).thenReturn(describeTopicsResult);
    Node node = new Node(0, "host", 0);
    TopicDescription description = new TopicDescription(
        topic,
        false,
        IntStream
            .range(0, size)
            .mapToObj(i -> new TopicPartitionInfo(i, node, List.of(node), List.of(node)))
            .collect(toList()));
    when(describeTopicsResult.all()).thenReturn(KafkaFuture.completedFuture(Map.of(topic, description)));
  }

  private void stubDescribeTopicsError() {
    DescribeTopicsResult describeTopicsResult = mock(DescribeTopicsResult.class);
    when(client.describeTopics(List.of(topic))).thenReturn(describeTopicsResult);
    KafkaFutureImpl<Map<String, TopicDescription>> future = new KafkaFutureImpl<>();
    future.completeExceptionally(new UnknownTopicOrPartitionException());
    when(describeTopicsResult.all()).thenReturn(future);
  }

  private void describeConfigs(boolean compact) {
    List<ConfigEntry> entries = List.of();
    if (compact) {
      entries = List.of(new ConfigEntry(CleanupPolicyProp(), Compact()));
    }
    ConfigResource resource = new ConfigResource(TOPIC, topic);
    DescribeConfigsResult describeConfigsResult = mock(DescribeConfigsResult.class);
    when(client.describeConfigs(List.of(resource))).thenReturn(describeConfigsResult);
    org.apache.kafka.clients.admin.Config config = new org.apache.kafka.clients.admin.Config(entries);
    when(describeConfigsResult.all()).thenReturn(KafkaFuture.completedFuture(Map.of(resource, config)));
  }
}
