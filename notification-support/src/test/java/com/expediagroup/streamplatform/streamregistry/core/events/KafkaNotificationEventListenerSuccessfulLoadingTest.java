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

import static com.expediagroup.streamplatform.streamregistry.core.events.KafkaNotificationListenerConfig.*;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = MockListenerConfiguration.class,
        properties = {
                KAFKA_NOTIFICATIONS_ENABLED_PROPERTY + "=true",
                KAFKA_TOPIC_NAME_PROPERTY + "=my-topic",
                KAFKA_TOPIC_SETUP_PROPERTY + "=false", // We don't test setup topic here but in  the integration test
                KAFKA_BOOTSTRAP_SERVERS_PROPERTY + "=localhost:9092",
                KAFKA_SCHEMA_REGISTRY_URL_PROPERTY + "=foo:8081"})
public class KafkaNotificationEventListenerSuccessfulLoadingTest {

    @Autowired(required = false)
    private Optional<KafkaNotificationEventListener> kafkaNotificationEventListener;

    @Test
    public void having_notifications_enabled_verify_that_KafkaNotificationEventListener_is_being_loaded() {
        Assert.assertNotNull("Optional container of kafkaNotificationEventListener shouldn't be null!", kafkaNotificationEventListener);
        Assert.assertTrue(String.format("Kafka notification should be loaded since %s == true", KAFKA_NOTIFICATIONS_ENABLED_PROPERTY), kafkaNotificationEventListener.isPresent());
    }

    @Test
    public void having_setup_disabled_verify_that_KafkaSetupHandler_is_not_being_loaded() {
        Assert.assertNotNull("Optional container of kafkaNotificationEventListener shouldn't be null!", kafkaNotificationEventListener);
        Assert.assertFalse(String.format("Since %s == false, KafkaSetupHandler shouldn't be set", KAFKA_TOPIC_SETUP_PROPERTY), kafkaNotificationEventListener.flatMap(KafkaNotificationEventListener::getKafkaSetupHandler).isPresent());
    }
}