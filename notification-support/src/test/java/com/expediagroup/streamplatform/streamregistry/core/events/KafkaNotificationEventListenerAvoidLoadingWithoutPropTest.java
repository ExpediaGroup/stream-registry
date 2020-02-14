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

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)// Not defined prop!
@SpringBootTest(classes = MockListenerConfiguration.class)
public class KafkaNotificationEventListenerAvoidLoadingWithoutPropTest {

    @Autowired(required = false)
    private Optional<KafkaNotificationEventListener> kafkaNotificationEventListener;

    @Test
    public void having_notifications_disabled_verify_that_KafkaNotificationEventListener_is_not_being_loaded() {
        Assert.assertNotNull("Optional container of kafkaNotificationEventListener shouldn't be null!", kafkaNotificationEventListener);
        Assert.assertFalse("Kafka notification should NOT be loaded since notification.events.kafka.enabled == false", kafkaNotificationEventListener.isPresent());
    }
}