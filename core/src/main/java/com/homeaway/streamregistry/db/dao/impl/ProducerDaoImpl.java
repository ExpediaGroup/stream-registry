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
package com.homeaway.streamregistry.db.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

import com.homeaway.streamregistry.Actor;
import com.homeaway.streamregistry.AvroStream;
import com.homeaway.streamregistry.AvroStreamKey;
import com.homeaway.streamregistry.OperationType;
import com.homeaway.streamregistry.Producer;
import com.homeaway.streamregistry.RegionStreamConfiguration;
import com.homeaway.streamregistry.db.dao.AbstractDao;
import com.homeaway.streamregistry.db.dao.KafkaManager;
import com.homeaway.streamregistry.db.dao.RegionDao;
import com.homeaway.streamregistry.db.dao.StreamClientDao;
import com.homeaway.streamregistry.dto.AvroToJsonDTO;
import com.homeaway.streamregistry.exceptions.ProducerNotFoundException;
import com.homeaway.streamregistry.exceptions.StreamNotFoundException;
import com.homeaway.streamregistry.exceptions.UnknownRegionException;
import com.homeaway.streamregistry.provider.InfraManager;
import com.homeaway.streamregistry.streams.ManagedKStreams;
import com.homeaway.streamregistry.streams.ManagedKafkaProducer;

@Slf4j
public class ProducerDaoImpl extends AbstractDao implements StreamClientDao<com.homeaway.streamregistry.model.Producer> {

    private final List<String> topicPostFixes = Collections.singletonList("");
    private final static String ACTOR_TYPE = "producer";

    public ProducerDaoImpl(ManagedKafkaProducer managedKafkaProducer,
        ManagedKStreams kStreams,
        String env,
        RegionDao regionDao,
        InfraManager infraManager,
        KafkaManager kafkaManager) {
        super(managedKafkaProducer, kStreams, env, regionDao, infraManager, kafkaManager);
    }

    @Override
    public Optional<com.homeaway.streamregistry.model.Producer> update(String streamName, String actorName, String region) {
        log.info("Processing stream {} in local instance.", streamName);
        return updateProducer(streamName, actorName, region);
    }

    @Override
    public Optional<com.homeaway.streamregistry.model.Producer> get(String streamName, String actorName) {
        return getProducer(streamName, actorName);
    }

    @Override
    public void delete(String streamName, String actorName) {
        deleteProducer(streamName, actorName);
    }

    @Override
    public List<com.homeaway.streamregistry.model.Producer> getAll(String streamName) {
        return getProducers(streamName);
    }

    private Optional<com.homeaway.streamregistry.model.Producer> updateProducer(String streamName, String producerName, String region) {
        Optional<AvroStream> avroStream = getAvroStreamKeyValue(streamName).getValue();

        if (avroStream.isPresent()) {
            List<Producer> producers = avroStream.get().getProducers();
            if (producers != null) {
                for (com.homeaway.streamregistry.Producer producer : producers) {
                    if (producer.getActor().getName().equalsIgnoreCase(producerName)) {
                        for (RegionStreamConfiguration config : producer.getActor().getRegionStreamConfigurations()) {
                            if (config.getRegion().equals(region)) {
                                // Existing producer for this region
                                String streamHint = avroStream.get().getTags().getHint();
                                String hint = (streamHint == null || streamHint.trim().matches("(?i:string)?")) ? AbstractDao.PRIMARY_HINT : streamHint.trim().toLowerCase();
                                Actor producerActor = populateActorStreamConfig(streamName, region, producer.getActor(), OPERATION.CREATE.name(), topicPostFixes, hint,
                                    ACTOR_TYPE, avroStream.get().getTopicConfig());
                                producer.setActor(producerActor);
                                updateAvroStream(avroStream.get());
                                log.info("Producer updated in source-processor-topic. streamName={} ; producerName={} ; region={}",
                                    streamName, producerName, region);
                                return Optional.of(AvroToJsonDTO.getJsonProducer(producer));
                            }
                        }
                    }
                }
            }
            // Register the new producer
            log.info("Registering new Producer. Stream={} Producer={} ; region={}", streamName, producerName, region);
            return registerProducer(avroStream.get(), producerName, region);
        }
        log.info("Local instance returning empty result and stream {} cannot be found", streamName);
        return Optional.empty();
    }

    private Optional<com.homeaway.streamregistry.model.Producer> registerProducer(AvroStream avroStream, String producerName, String region) {
        if (!regionDao.getSupportedRegions(avroStream.getTags().getHint()).contains(region))
            throw new UnknownRegionException(region);

        List<com.homeaway.streamregistry.Producer> listProducers = avroStream.getProducers();
        if (listProducers == null) {
            listProducers = new ArrayList<>();
        }
        com.homeaway.streamregistry.Producer producer =
            com.homeaway.streamregistry.Producer.newBuilder()
                .setActor(Actor.newBuilder()
                    .setName(producerName)
                    .build())
                .build();

        String streamHint = avroStream.getTags().getHint();
        String hint = (streamHint == null || streamHint.trim().matches("(?i:string)?")) ? AbstractDao.PRIMARY_HINT : streamHint.trim().toLowerCase();

        Actor actor = populateActorStreamConfig(avroStream.getName(), region, producer.getActor(), OPERATION.CREATE.name(),
            topicPostFixes, hint, ACTOR_TYPE, avroStream.getTopicConfig());
        Producer newProducer = com.homeaway.streamregistry.Producer.newBuilder()
            .setActor(actor)
            .build();

        listProducers.add(newProducer);
        avroStream.setProducers(listProducers);

        updateAvroStream(avroStream);

        return Optional.of(AvroToJsonDTO.getJsonProducer(newProducer));
    }

    private void deleteProducer(String streamName, String producerName) {
        Optional<AvroStream> avroStream = getAvroStreamKeyValue(streamName).getValue();

        if (avroStream.isPresent()) {
            List<com.homeaway.streamregistry.Producer> listProducer = avroStream.get().getProducers();
            int producerInitialSize = listProducer.size();
            for (Iterator<com.homeaway.streamregistry.Producer> iter = listProducer.listIterator(); iter.hasNext(); ) {
                com.homeaway.streamregistry.Producer producerEntity = iter.next();
                if (producerEntity.getActor().getName().equalsIgnoreCase(producerName)) {
                    // Remove the managedKafkaProducer
                    iter.remove();
                    avroStream.get().setProducers(Lists.newArrayList(iter));
                }
            }
            if (avroStream.get().getProducers().size() < producerInitialSize)
                updateAvroStream(avroStream.get());
            else
                throw new ProducerNotFoundException(producerName);
        } else {
            throw new StreamNotFoundException(streamName);
        }
    }

    private Optional<com.homeaway.streamregistry.model.Producer> getProducer(String streamName, String producerName) {
        // pull data from state store of this instance.
        log.info("Pulling stream information from local instance's state-store for streamName={} ; producerName={}", streamName,
            producerName);
        Optional<AvroStream> streamValue =
            kStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build());
        if (streamValue.isPresent() && streamValue.get().getProducers() != null) {
            streamValue.get().setOperationType(OperationType.GET);

            for (com.homeaway.streamregistry.Producer producer : streamValue.get().getProducers()) {
                if (producer.getActor().getName().equals(producerName))
                    return Optional.of(AvroToJsonDTO.getJsonProducer(producer));
            }
        }
        return Optional.empty();
    }

    private List<com.homeaway.streamregistry.model.Producer> getProducers(String streamName) {
        List<com.homeaway.streamregistry.model.Producer> producers = new ArrayList<>();
        // pull data from state store of this instance.
        log.info("Pulling stream information from local instance's state-store for streamName={} ; managedKafkaProducer=all", streamName);
        Optional<AvroStream> streamValue =
            kStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build());
        if (streamValue.isPresent() && streamValue.get().getProducers() != null) {
            streamValue.get().setOperationType(OperationType.GET);
            for (com.homeaway.streamregistry.Producer producer : streamValue.get().getProducers()) {
                producers.add(AvroToJsonDTO.getJsonProducer(producer));
            }
        }
        return producers;
    }

}
