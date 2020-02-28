/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;

@Slf4j
@Builder
public class KafkaSetupHandler {
    public static final int DEFAULT_CREATE_TOPIC_TIMEOUT_MS = 10000;

    @NonNull
    @Getter
    private final String bootstrapServers;

    @NonNull
    @Getter
    private final Optional<NewTopic> newTopic;

    @NonNull
    @Getter
    private String notificationEventsTopic;

    @Getter
    private final boolean isKafkaSetupEnabled;

    public void setup() throws ExecutionException, InterruptedException {
        log.info("Starting Kafka setup...");
        if(isKafkaSetupEnabled) {
          executeTopicSetup();
        }

        checkTopicExistence();
    }

    private void executeTopicSetup() {
        try (AdminClient client = createAdminClient()) {
            NewTopic topic = newTopic.get();
            Optional<TopicDescription> description = obtainTopicDescription(client, topic.name());

            if (description.isEmpty()) {
                log.info("Trying to create topic {} on cluster {}", topic, bootstrapServers);

                Optional<TopicDescription> created = createTopic(client, topic);

                log.info("Created topic {} on cluster {}", created, bootstrapServers);
            } else {
                log.warn("Topic {} of cluster {} already exists: {}", topic.name(), bootstrapServers, description.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void checkTopicExistence() throws ExecutionException, InterruptedException {
        try (AdminClient client = createAdminClient()) {
            Optional<TopicDescription> topic = obtainTopicDescription(client, notificationEventsTopic);

            log.info("Notification topic {} in cluster {}: {}", notificationEventsTopic, bootstrapServers, topic);

            topic.orElseThrow(() -> new IllegalStateException(String.format("Topic %s wasn't found in cluster", notificationEventsTopic)));
        } catch (InterruptedException | ExecutionException e) {
            log.error("There was an error verifying topic {} existence", notificationEventsTopic);
            throw e;
        }
    }

    private AdminClient createAdminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        log.info("Creating a Kafka Admin client with configuration {}", configs);

        return AdminClient.create(configs);
    }

    private Optional<TopicDescription> obtainTopicDescription(AdminClient client, String topic) throws ExecutionException, InterruptedException {
        try {
            log.info("Verifying existence of topic {}", topic);

            return Optional.ofNullable(client.describeTopics(Collections.singleton(topic)).all().get().get(topic));
        } catch (ExecutionException exception) {
            if (exception.getCause() != null && exception.getCause() instanceof UnknownTopicOrPartitionException) {
                return Optional.empty();
            } else {
              throw exception;
            }
        }
    }

    private Optional<TopicDescription> createTopic(AdminClient client, NewTopic newTopic) throws ExecutionException, InterruptedException {
        CreateTopicsResult result = client.createTopics(Collections.singleton(newTopic), new CreateTopicsOptions().timeoutMs(DEFAULT_CREATE_TOPIC_TIMEOUT_MS));
        result.all().get(); // Force transactional

        return obtainTopicDescription(client, newTopic.name());
    }
}
