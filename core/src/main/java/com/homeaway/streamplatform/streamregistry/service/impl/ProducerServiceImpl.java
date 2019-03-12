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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;

import com.homeaway.digitalplatform.streamregistry.Actor;
import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.Producer;
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
public class ProducerServiceImpl extends AbstractService implements StreamClientService<com.homeaway.streamplatform.streamregistry.model.Producer> {

    private final static String ACTOR_TYPE = "producer";
    // TODO should this be static final ??
    private final List<String> topicPostFixes = Collections.singletonList("");
    private StreamDao streamDao;

    public ProducerServiceImpl(StreamDao streamDao,
                               String env,
                               RegionService regionService,
                               ClusterService clusterService,
                               InfraManager infraManager) {
        super(env, regionService, clusterService, infraManager);
        this.streamDao = streamDao;
    }

    @Override
    public Optional<com.homeaway.streamplatform.streamregistry.model.Producer> update(String streamName, String actorName, String region)
            throws StreamNotFoundException, RegionNotFoundException, ClusterNotFoundException {
        log.info("Processing stream {} in local instance.", streamName);
        return updateProducer(streamName, actorName, region);
    }

    @Override
    public Optional<com.homeaway.streamplatform.streamregistry.model.Producer> get(String streamName, String actorName) throws StreamNotFoundException {
        return getProducer(streamName, actorName);
    }

    @Override
    public void delete(String streamName, String actorName) throws StreamNotFoundException, ActorNotFoundException {
        deleteProducer(streamName, actorName);
    }

    @Override
    public List<com.homeaway.streamplatform.streamregistry.model.Producer> getAll(String streamName) throws StreamNotFoundException {
        return getProducers(streamName);
    }

    private Optional<com.homeaway.streamplatform.streamregistry.model.Producer> updateProducer(String streamName, String producerName, String region)
            throws StreamNotFoundException, RegionNotFoundException, ClusterNotFoundException {
        Pair<AvroStreamKey, Optional<AvroStream>> streamKeyValueOptional = streamDao.getStream(streamName);
        AvroStreamKey key = streamKeyValueOptional.getKey();
        Optional<AvroStream> streamOptional = streamKeyValueOptional.getValue();

        if (!streamOptional.isPresent()) {
            throw new StreamNotFoundException(String.format("StreamName=%s not found. Please create the Stream before registering a Producer", streamName));
        }

        AvroStream stream = streamOptional.get();
        List<Producer> producers = stream.getProducers();
        if (producers != null) {
            for (com.homeaway.digitalplatform.streamregistry.Producer producer : producers) {
                if (producer.getActor().getName().equalsIgnoreCase(producerName)) {
                    for (RegionStreamConfiguration config : producer.getActor().getRegionStreamConfigurations()) {
                        if (config.getRegion().equals(region)) {
                            // Existing producer for this region
                            String hint = getDefaultHint(streamOptional.get());
                            Actor producerActor = populateActorStreamConfig(streamName, region, producer.getActor(), OPERATION.CREATE.name(), topicPostFixes, hint,
                                    ACTOR_TYPE);
                            producer.setActor(producerActor);
                            streamDao.upsertStream(key, stream);
                            log.info("Producer updated in source-processor-topic. streamName={} ; producerName={} ; region={}", streamName, producerName, region);
                            return Optional.of(AvroToJsonDTO.getJsonProducer(producer));
                        }
                    }
                }
            }
        }
        // Register the new producer
        return registerProducer(key, stream, producerName, region);
    }

    private Optional<com.homeaway.streamplatform.streamregistry.model.Producer> registerProducer(AvroStreamKey key, AvroStream avroStream, String producerName, String region)
            throws RegionNotFoundException, ClusterNotFoundException {
        log.info("Registering new Producer. Stream={} Producer={} ; region={}", avroStream.getName(), producerName, region);
        if (!regionService.getSupportedRegions(avroStream.getTags().getHint()).contains(region))
            throw new RegionNotFoundException(String.format("Region=%s not supported for hint=%s", region, avroStream.getTags().getHint()));

        List<com.homeaway.digitalplatform.streamregistry.Producer> listProducers = avroStream.getProducers();
        if (listProducers == null) {
            listProducers = new ArrayList<>();
        }
        com.homeaway.digitalplatform.streamregistry.Producer producer =
            com.homeaway.digitalplatform.streamregistry.Producer.newBuilder()
                .setActor(Actor.newBuilder()
                    .setName(producerName)
                    .build())
                .build();

        String hint = getDefaultHint(avroStream);

        Actor actor = populateActorStreamConfig(avroStream.getName(), region, producer.getActor(), OPERATION.CREATE.name(),
            topicPostFixes, hint, ACTOR_TYPE);
        Producer newProducer = com.homeaway.digitalplatform.streamregistry.Producer.newBuilder()
            .setActor(actor)
            .build();

        listProducers.add(newProducer);
        avroStream.setProducers(listProducers);

        streamDao.upsertStream(key, avroStream);

        return Optional.of(AvroToJsonDTO.getJsonProducer(newProducer));
    }

    private void deleteProducer(String streamName, String producerName) throws ActorNotFoundException, StreamNotFoundException {
        Pair<AvroStreamKey, Optional<AvroStream>> streamKeyValueOptional = streamDao.getStream(streamName);
        AvroStreamKey key = streamKeyValueOptional.getKey();
        Optional<AvroStream> avroStreamOptional = streamKeyValueOptional.getValue();

        if(avroStreamOptional==null || !avroStreamOptional.isPresent()) {
            throw new StreamNotFoundException(String.format("Stream=%s not found. Please create the Stream before deleting a Producer", streamName));
        }

        AvroStream avroStream = avroStreamOptional.get();

        final List<com.homeaway.digitalplatform.streamregistry.Producer> withProducer = avroStream.getProducers();

        if (withProducer == null || withProducer.isEmpty() ) {
            throw new ActorNotFoundException(String.format("Producer=%s not found for Stream=%s", producerName, streamName));
        }


        // Obtains filtered producer list not containing the consumer we want to remove
        List<com.homeaway.digitalplatform.streamregistry.Producer> withoutProducer = withProducer
                .stream()
                .filter(producer -> !StreamRegistryUtils.hasActorNamed(producerName, producer::getActor))
                .collect(Collectors.toList());

        if (withoutProducer.size() >= withProducer.size()) {
            throw new ActorNotFoundException(String.format("Producer=%s not found for Stream=%s", producerName, streamName));
        }

        // Update stream's producer list and update
        avroStream.setProducers(withoutProducer);
        streamDao.upsertStream(key, avroStream);

    }

    private Optional<com.homeaway.streamplatform.streamregistry.model.Producer> getProducer(String streamName,
                                                                                            String producerName)
            throws StreamNotFoundException {
        // pull data from state store of this instance.
        log.info("Pulling stream information from local instance's state-store for streamName={} ; producerName={}",
                streamName, producerName);
        Optional<AvroStream> streamOptional = streamDao.getStream(streamName).getValue();

        if (!streamOptional.isPresent()) {
            throw new StreamNotFoundException(String.format("Stream=%s not found. Please create the Stream before getting a Producer", streamName));
        }

        AvroStream stream = streamOptional.get();

        if (stream.getProducers() == null) {
            return Optional.empty();
        }

        return stream.getProducers().stream()
                .filter(producer -> StreamRegistryUtils.hasActorNamed(producerName, producer::getActor))
                .findAny()
                .map(AvroToJsonDTO::getJsonProducer);
    }

    private List<com.homeaway.streamplatform.streamregistry.model.Producer> getProducers(String streamName) throws StreamNotFoundException {
        // pull data from state store of this instance.
        log.info("Pulling stream information from local instance's state-store for streamName={} ; managedKafkaProducer=all", streamName);
        Optional<AvroStream> streamOptional = streamDao.getStream(streamName).getValue();
        if (!streamOptional.isPresent()) {
            throw new StreamNotFoundException(String.format("Stream=%s not found. Please create the Stream before retrieving the producers", streamName));
        }

        AvroStream stream = streamOptional.get();
        if (stream.getProducers() == null) {
            return new ArrayList<>();
        }
        return stream.getProducers().stream()
                .map(AvroToJsonDTO::getJsonProducer)
                .collect(Collectors.toList());
    }

}
