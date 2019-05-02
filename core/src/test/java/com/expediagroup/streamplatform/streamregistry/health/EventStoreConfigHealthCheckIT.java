/* Copyright (c) 2018-2019 Expedia, Inc.
 * All rights reserved.  http://www.expediagroup.com

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
package com.expediagroup.streamplatform.streamregistry.health;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.codahale.metrics.health.HealthCheck;

import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.configuration.EventStoreTopic;
import com.expediagroup.streamplatform.streamregistry.resource.BaseResourceIT;

/**
 * Test class to validate whether the StreamRegistryHealthCheck could properly insert
 * a valid Stream, producer and consumer, and validate whether the StateStore is active
 *
 */
public class EventStoreConfigHealthCheckIT extends BaseResourceIT {

    @Test
    public void testHealthy() throws ExecutionException, InterruptedException {
        HealthCheck.Result healthCheckResult = eventStoreConfigHealthCheck.check();

        Assert.assertTrue(healthCheckResult.isHealthy());
        Assert.assertTrue(eventStoreConfigHealthCheck.isEventStoreTopicConfigsValid());
        Assert.assertEquals(1, getCurrentReadingForMetric(Metrics.IS_EVENT_STORE_KAFKA_TOPIC_CONFIG_VALID.getName()).getValue());
    }

    @Test
    public void testUnHealthyIfCompactionDisabled() throws ExecutionException, InterruptedException {
        EventStoreTopic eventStoreTopic = eventStoreConfigHealthCheck.getEventStoreTopic();
        Map<String, String> eventStoreTopicProperties = eventStoreTopic.getProperties();

        // update "cleanup.policy=delete" to the EventStore Topic
        Properties wrongProperties = new Properties();
        eventStoreTopicProperties.forEach(wrongProperties::put);
        wrongProperties.put("cleanup.policy", "delete");
        BaseResourceIT.changeTopicConfig(eventStoreTopic.getName(), wrongProperties);

        // test
        HealthCheck.Result healthCheckResult = eventStoreConfigHealthCheck.check();

        // assert
        Assert.assertFalse(healthCheckResult.isHealthy());
        Assert.assertFalse(eventStoreConfigHealthCheck.isEventStoreTopicConfigsValid());
        Assert.assertEquals(2, getCurrentReadingForMetric(Metrics.IS_EVENT_STORE_KAFKA_TOPIC_CONFIG_VALID.getName()).getValue());

        // put "cleanup.policy=compact" back to the EventStore Topic
        Properties originalProperties = new Properties();
        eventStoreTopicProperties.forEach(originalProperties::put);
        BaseResourceIT.changeTopicConfig(eventStoreTopic.getName(), originalProperties);

        // test again
        Assert.assertTrue(eventStoreConfigHealthCheck.check().isHealthy());
    }

}
