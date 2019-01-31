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
package com.homeaway.streamplatform.streamregistry.health;

import com.codahale.metrics.health.HealthCheck;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.homeaway.streamplatform.streamregistry.resource.BaseResourceIT;

/**
 * Test class to validate whether the StreamRegistryHealthCheck could properly insert
 * a valid Stream, producer and consumer, and validate whether the StateStore is active
 *
 */
public class StreamRegistryHealthCheckIT extends BaseResourceIT {

    @Before
    public void setup() {
        // Integration-Test environment has only one broker, so overriding the default value 3 with 1
        healthCheck.setHealthcheckStreamReplicationFactor(1);
        // Build environment does have the env variable MPAAS_REGION
        healthCheck.setRegion(US_EAST_REGION);
    }

    @Test
    public void testHealthCheck() {
        HealthCheck.Result healthCheckResult = healthCheck.check();

        Assert.assertTrue(healthCheckResult.isHealthy());
    }
}
