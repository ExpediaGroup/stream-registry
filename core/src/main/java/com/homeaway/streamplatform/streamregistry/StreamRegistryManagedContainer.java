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
package com.homeaway.streamplatform.streamregistry;

import io.dropwizard.lifecycle.Managed;

import com.homeaway.streamplatform.streamregistry.health.StreamRegistryHealthCheck;
import com.homeaway.streamplatform.streamregistry.streams.ManagedInfraManager;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamplatform.streamregistry.streams.ManagedKafkaProducer;

/**
 * This is the container for all infrastructure components that wires up the app,
 * and invokes the start / stop lifecycle of the components in order.
 * 
 * Note: We decide to take this route to avoid any HK2 and Jetty lifecycle dependency in server flow
 */
public class StreamRegistryManagedContainer implements Managed {

    private ManagedKStreams managedKStreams;
    private ManagedInfraManager managedInfraManager;
    private ManagedKafkaProducer managedKafkaProducer;
    private StreamRegistryHealthCheck streamRegistryHealthCheck;

    public StreamRegistryManagedContainer(ManagedKStreams managedKStreams, ManagedInfraManager managedInfraManager, ManagedKafkaProducer managedKafkaProducer, StreamRegistryHealthCheck streamRegistryHealthCheck) {
        this.managedKStreams = managedKStreams;
        this.managedInfraManager = managedInfraManager;
        this.managedKafkaProducer = managedKafkaProducer;
        this.streamRegistryHealthCheck = streamRegistryHealthCheck;
    }

    @Override
    public void start() throws Exception {
        this.managedKStreams.start();
        this.managedInfraManager.start();
        this.managedKafkaProducer.start();
        this.streamRegistryHealthCheck.start();
    }

    @Override
    public void stop() throws Exception {
        this.streamRegistryHealthCheck.stop();
        this.managedKafkaProducer.stop();
        this.managedInfraManager.stop();
        this.managedKStreams.stop();
    }

}
