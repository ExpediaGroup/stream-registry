/* Copyright (c) 2018-Present Expedia Group.
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
package com.homeaway.streamplatform.streamregistry.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;

import com.homeaway.digitalplatform.streamregistry.Actor;
import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.Consumer;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.digitalplatform.streamregistry.RegionStreamConfiguration;
import com.homeaway.streamplatform.streamregistry.db.dao.StreamDao;
import com.homeaway.streamplatform.streamregistry.dto.AvroToJsonDTO;
import com.homeaway.streamplatform.streamregistry.exceptions.ActorNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.ClusterNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.RegionNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamplatform.streamregistry.provider.InfraManager;
import com.homeaway.streamplatform.streamregistry.service.AbstractService;
import com.homeaway.streamplatform.streamregistry.service.ClusterService;
import com.homeaway.streamplatform.streamregistry.service.RegionService;
import com.homeaway.streamplatform.streamregistry.service.StreamClientService;
import com.homeaway.streamplatform.streamregistry.utils.StreamRegistryUtils;

@Slf4j
public class ConsumerServiceImpl extends AbstractService implements StreamClientService<com.homeaway.streamplatform.streamregistry.model.Consumer> {

    private static final List<String> TOPIC_POST_FIXES = Collections.unmodifiableList(Arrays.asList("", ".global"));

    private static final String ACTOR_TYPE = "consumer";

    private StreamDao streamDao;

    public ConsumerServiceImpl(StreamDao streamDao,
                               String env,
                               RegionService regionService,
                               ClusterService clusterService,
                               InfraManager infraManager) {
        super(env, regionService, clusterService, infraManager);
        this.streamDao = streamDao;
    }

    @Override
    public Optional<com.homeaway.streamplatform.streamregistry.model.Consumer> update(String streamName, String actorName, String region)
            throws StreamNotFoundException, RegionNotFoundException, ClusterNotFoundException {
        return updateConsumer(streamName, actorName, region);
    }

    @Override
    public Optional<com.homeaway.streamplatform.streamregistry.model.Consumer> get(String streamName, String actorName) throws StreamNotFoundException {
        return getConsumer(streamName, actorName);
    }

    @Override
    public void delete(String streamName, String actorName) throws StreamNotFoundException, ActorNotFoundException {
        deleteConsumer(streamName, actorName);
    }

    @Override
    public List<com.homeaway.streamplatform.streamregistry.model.Consumer> getAll(String streamName) throws StreamNotFoundException {
        return getAllConsumers(streamName);
    }

    private Optional<com.homeaway.streamplatform.streamregistry.model.Consumer> updateConsumer(String streamName, String consumerName, String region)
            throws StreamNotFoundException, RegionNotFoundException, ClusterNotFoundException {
        Pair<AvroStreamKey, Optional<AvroStream>> streamKeyValueOptional = streamDao.getStream(streamName);
        AvroStreamKey key = streamKeyValueOptional.getKey();
        Optional<AvroStream> avroStream = streamKeyValueOptional.getValue();
        // Try to do exceptions first, reduces cyclomatic complexity
        if (!avroStream.isPresent()) {
            throw new StreamNotFoundException(String.format("StreamName=%s not found. Please create the Stream before registering a Consumer", streamName));
        }

        // if consumers exist try to update the matching consumer
        List<Consumer> consumers = avroStream.get().getConsumers();
        if (consumers != null) {
            for (com.homeaway.digitalplatform.streamregistry.Consumer consumer : consumers) {
                if (consumer.getActor().getName().equalsIgnoreCase(consumerName)) {
                    for (RegionStreamConfiguration streamConfiguration : consumer.getActor().getRegionStreamConfigurations()) {
                        if (streamConfiguration.getRegion().equals(region)) {
                            // Existing consumer for this region
                            String streamHint = avroStream.get().getTags().getHint();
                            String hint = (streamHint == null || streamHint.trim().matches("(?i:string)?")) ? AbstractService.PRIMARY_HINT
                                : streamHint.trim().toLowerCase();
                            Actor consumerActor = populateActorStreamConfig(streamName, region, consumer.getActor(),
                                OPERATION.UPDATE.name(), TOPIC_POST_FIXES, hint,
                                ACTOR_TYPE);
                            consumer.setActor(consumerActor);
                            streamDao.upsertStream(key, avroStream.get());
                            log.info(
                                "Consumer updated in source-processor-topic. streamName={} ; consumerName={} ; consumer={} ; region={}",
                                streamName, consumerName, consumer, region);
                            return Optional.of(AvroToJsonDTO.getJsonConsumer(consumer));
                        }
                    }
                }
            }
        }
        // no consumer and/or region matches
        return createConsumer(key, avroStream.get(), consumerName, region);
    }

    private Optional<com.homeaway.streamplatform.streamregistry.model.Consumer> createConsumer(AvroStreamKey key, AvroStream avroStream, String consumerName,
        String region) throws RegionNotFoundException, ClusterNotFoundException {
        log.info("Registering new Consumer. Stream={} Consumer={} ; region={}", avroStream.getName(), consumerName, region);

        List<String> possibleRegions = avroStream.getVpcList();
        if (avroStream.getReplicatedVpcList() != null) {
            possibleRegions = Stream.concat(avroStream.getVpcList().stream(), avroStream.getReplicatedVpcList().stream())
                    .distinct()
                    .collect(Collectors.toList());
        }

        if (!possibleRegions.contains(region))
            throw new RegionNotFoundException(String.format("Region=%s is not supported for regions=%s", region, possibleRegions));

        List<com.homeaway.digitalplatform.streamregistry.Consumer> listConsumers = avroStream.getConsumers();
        if (listConsumers == null) {
            listConsumers = new ArrayList<>();
        }

        com.homeaway.digitalplatform.streamregistry.Consumer consumer = com.homeaway.digitalplatform.streamregistry.Consumer
            .newBuilder()
            .setActor(Actor.newBuilder()
                .setName(consumerName)
                .build())
            .build();

        String hint = getDefaultHint(avroStream);

        Actor actor = populateActorStreamConfig(avroStream.getName(), region, consumer.getActor(), OPERATION.CREATE.name(), TOPIC_POST_FIXES, hint,
                ACTOR_TYPE);

        consumer = Consumer.newBuilder()
            .setActor(actor)
            .build();

        listConsumers.add(consumer);
        avroStream.setConsumers(listConsumers);

        streamDao.upsertStream(key, avroStream);

        return Optional.of(AvroToJsonDTO.getJsonConsumer(consumer));
    }

    private Optional<com.homeaway.streamplatform.streamregistry.model.Consumer> getConsumer(String streamName, String consumerName) throws StreamNotFoundException {
        // pull data from state store of this instance.
        log.info("Pulling stream information from local instance's state-store for streamName={} ; consumerName={}", streamName, consumerName);
        Optional<AvroStream> streamValue = streamDao.getStream(streamName).getValue();
        if (!streamValue.isPresent()) {
            throw new StreamNotFoundException(String.format("StreamName=%s not found. Please create the Stream before getting a Consumer", streamName));
        }

        streamValue.get().setOperationType(OperationType.GET);
        if (streamValue.get().getConsumers() != null) {
            for (com.homeaway.digitalplatform.streamregistry.Consumer consumer : streamValue.get().getConsumers()) {
                if (consumer.getActor().getName().equals(consumerName))
                    return Optional.of(AvroToJsonDTO.getJsonConsumer(consumer));
            }
        }
        return Optional.empty();
    }

    private List<com.homeaway.streamplatform.streamregistry.model.Consumer> getAllConsumers(String streamName) throws StreamNotFoundException {
        // pull data from state store of this instance.
        log.info("Pulling stream information from local instance's state-store for stream={} ; consumers=all", streamName);
        Optional<AvroStream> streamValue = streamDao.getStream(streamName).getValue();
        if (!streamValue.isPresent()) {
            throw new StreamNotFoundException(String.format("Stream=%s not found. Please create the Stream before retrieving a Consumers", streamName));
        }

        List<com.homeaway.streamplatform.streamregistry.model.Consumer> consumers = new ArrayList<>();
        if (streamValue.get().getConsumers() != null) {
            streamValue.get().setOperationType(OperationType.GET);
            for (com.homeaway.digitalplatform.streamregistry.Consumer consumer : streamValue.get().getConsumers()) {
                consumers.add(AvroToJsonDTO.getJsonConsumer(consumer));
            }
        }
        return consumers;
    }

    private void deleteConsumer(String streamName, String consumerName) throws StreamNotFoundException, ActorNotFoundException {
        Pair<AvroStreamKey, Optional<AvroStream>> streamKeyValueOptional = streamDao.getStream(streamName);
        AvroStreamKey key = streamKeyValueOptional.getKey();
        Optional<AvroStream> avroStream = streamKeyValueOptional.getValue();

        if (!avroStream.isPresent()) {
            throw new StreamNotFoundException(String.format("Stream with the name %s not found. Please create the Stream before deleting a Consumer", streamName));
        }

        final List<com.homeaway.digitalplatform.streamregistry.Consumer> withConsumer = avroStream.get().getConsumers();
        if (withConsumer == null || withConsumer.size() == 0 )
            throw new ActorNotFoundException(String.format("Consumer=%s not found for Stream=%s", consumerName, streamName));

        // Obtains consumer list size before  remove consumer
        final int consumerInitialSize = withConsumer.size();
        // Obtains filtered consumer list not containing the consumer we want to remove
        List<com.homeaway.digitalplatform.streamregistry.Consumer> withoutConsumer = withConsumer
                .stream()
                .filter(consumer -> !StreamRegistryUtils.hasActorNamed(consumerName, consumer::getActor))
                .collect(Collectors.toList());
        // Update stream's consumer list
        avroStream.get().setConsumers(withoutConsumer);
        // If filtered consumer list size is less than initial size stream will be updated
        if (avroStream.get().getConsumers().size() < consumerInitialSize) {
            streamDao.upsertStream(key, avroStream.get());
        } else {
            throw new ActorNotFoundException(String.format("Consumer=%s not found for Stream=%s", consumerName, streamName));
        }
    }
}
