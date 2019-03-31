package com.homeaway.streamplatform.streamregistry;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StreamRegistryApplicationTest {

    private String topicName = "eventStoreV1";

    private final StreamRegistryApplication streamRegistryApplication = new StreamRegistryApplication();
    private AdminClient adminClient = Mockito.mock(AdminClient.class);

    @Test
    public void testInstanceOfWhenIsTopicExistThrowsUnknownTopicOrPartitionException() throws InterruptedException, ExecutionException {
        KafkaFuture<TopicDescription> topicDescription = mockTopicDescription();
        ExecutionException executionException = new ExecutionException(new UnknownTopicOrPartitionException("Unknown topic name"));
        Mockito.when(topicDescription.get()).thenThrow(executionException);

        boolean isKafkaTopicPresent = streamRegistryApplication.isKafkaTopicAlreadyPresent(adminClient, topicName);
        Assert.assertFalse(isKafkaTopicPresent);
    }

    @Test(expected = ExecutionException.class)
    public void testInstanceOfWhenIsTopicExistThrowsDifferentException() throws InterruptedException, ExecutionException {
        KafkaFuture<TopicDescription> topicDescription = mockTopicDescription();
        ExecutionException executionException = new ExecutionException(new kafka.common.UnknownTopicOrPartitionException("Unknown topic name"));
        Mockito.when(topicDescription.get()).thenThrow(executionException);

        boolean isKafkaTopicPresent = streamRegistryApplication.isKafkaTopicAlreadyPresent(adminClient, topicName);
        Assert.assertFalse(isKafkaTopicPresent);
    }

    @Test(expected = RuntimeException.class)
    public void isTopicExistsThrowsRuntimeException() throws InterruptedException, ExecutionException {
        KafkaFuture<TopicDescription> topicDescription = mockTopicDescription();
        Mockito.when(topicDescription.get()).thenThrow(new RuntimeException());

        boolean isKafkaTopicPresent = streamRegistryApplication.isKafkaTopicAlreadyPresent(adminClient, topicName);
        Assert.assertFalse(isKafkaTopicPresent);
    }

    private KafkaFuture<TopicDescription> mockTopicDescription() {
        DescribeTopicsResult describeResult = Mockito.mock(DescribeTopicsResult.class);
        Mockito.when(adminClient.describeTopics(Mockito.anyCollection())).thenReturn(describeResult);
        Map<String, KafkaFuture<TopicDescription>> values = Mockito.mock(Map.class);
        Mockito.when(describeResult.values()).thenReturn(values);
        KafkaFuture<TopicDescription> topicDescription = Mockito.mock(KafkaFuture.class);
        Mockito.when(values.get(Mockito.anyString())).thenReturn(topicDescription);
        return topicDescription;
    }
}
