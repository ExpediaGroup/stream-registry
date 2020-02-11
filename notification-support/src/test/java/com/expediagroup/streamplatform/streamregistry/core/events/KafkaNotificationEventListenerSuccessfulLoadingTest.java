/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.events;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)// Explicitly defined prop with true as value
@SpringBootTest(classes = KafkaNotificationListenerConfig.class, properties = {"notification.events.kafka.enabled=true", "notification.events.kafka.setup=false"})
public class KafkaNotificationEventListenerSuccessfulLoadingTest {

    @Autowired(required = false)
    private Optional<KafkaNotificationEventListener> kafkaNotificationEventListener;

    @Test
    public void having_notifications_enabled_verify_that_KafkaNotificationEventListener_is_being_loaded() {
        Assert.assertTrue("Optional container of kafkaNotificationEventListener shouldn't be null!", kafkaNotificationEventListener != null);
        Assert.assertTrue("Kafka notification should be loaded since notification.events.kafka.enabled == true", kafkaNotificationEventListener.isPresent());

    }

    @Test
    public void having_setup_disabled_verify_that_KafkaSetupHandler_is_not_being_loaded() {
        Assert.assertTrue("Optional container of kafkaNotificationEventListener shouldn't be null!", kafkaNotificationEventListener != null);
        Assert.assertFalse("Since notification.events.kafka.setup == false, KafkaSetupHandler shouldn't be set", kafkaNotificationEventListener.get().getKafkaSetupHandler().isPresent());
    }
}