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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.expediagroup.streamplatform.streamregistry.model.*;

@Configuration
public class NotificationEventBusConfig {

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return eventMulticaster;
    }


    @Bean(name = "consumerBindingServiceEventEmitter")
    public NotificationEventEmitter<ConsumerBinding> consumerBindingServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<ConsumerBinding>builder()
                .classType(ConsumerBinding.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "consumerServiceEventEmitter")
    public NotificationEventEmitter<Consumer> consumerServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Consumer>builder()
                .classType(Consumer.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "domainServiceEventEmitter")
    public NotificationEventEmitter<Domain> domainServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Domain>builder()
                .classType(Domain.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "infrastructureServiceEventEmitter")
    public NotificationEventEmitter<Infrastructure> infrastructureServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Infrastructure>builder()
                .classType(Infrastructure.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "producerBindingServiceEventEmitter")
    public NotificationEventEmitter<ProducerBinding> producerBindingServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<ProducerBinding>builder()
                .classType(ProducerBinding.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "producerServiceEventEmitter")
    public NotificationEventEmitter<Producer> producerServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Producer>builder()
                .classType(Producer.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "schemaServiceEventEmitter")
    public NotificationEventEmitter<Schema> schemaServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Schema>builder()
                .classType(Schema.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "streamBindingServiceEventEmitter")
    public NotificationEventEmitter<StreamBinding> streamBindingServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<StreamBinding>builder()
                .classType(StreamBinding.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "streamServiceEventEmitter")
    public NotificationEventEmitter<Stream> streamServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Stream>builder()
                .classType(Stream.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }

    @Bean(name = "zoneServiceEventEmitter")
    public NotificationEventEmitter<Zone> zoneServiceEventEmitter(ApplicationEventMulticaster applicationEventMulticaster) {
        return DefaultNotificationEventEmitter.<Zone>builder()
                .classType(Zone.class)
                .applicationEventMulticaster(applicationEventMulticaster)
                .build();
    }
}