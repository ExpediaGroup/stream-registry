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
package com.homeaway.streamplatform.streamregistry.db.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;

import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.Sources;
import com.homeaway.streamplatform.streamregistry.db.dao.SourceDao;
import com.homeaway.streamplatform.streamregistry.exceptions.SourceNotFoundException;
import com.homeaway.streamplatform.streamregistry.model.Source;
import com.homeaway.streamplatform.streamregistry.streams.GlobalKafkaStore;
import com.homeaway.streamplatform.streamregistry.streams.StreamProducer;


@Slf4j
public class SourceDaoImpl implements SourceDao {

    @NotNull
    private StreamProducer<AvroStreamKey, com.homeaway.digitalplatform.streamregistry.Sources> kafkaProducer;

    @NotNull
    private final GlobalKafkaStore<AvroStreamKey, Sources> kstreams;

    public SourceDaoImpl(StreamProducer<AvroStreamKey, com.homeaway.digitalplatform.streamregistry.Sources> kafkaProducer,
                         GlobalKafkaStore<AvroStreamKey, Sources> kstreams) {
        this.kafkaProducer = kafkaProducer;
        this.kstreams = kstreams;
    }

    @Override
    public void upsert(Source givenSource) {


        AvroStreamKey avroStreamKey = getAvroKeyFromString(
                givenSource.getStreamName());

        Optional<Sources> avroSources = kstreams.getAvroStreamForKey(avroStreamKey);

        if (avroSources.isPresent()) {
            // stream exists, sources exist

            Optional<com.homeaway.digitalplatform.streamregistry.Source> avroSourceOptional = avroSources
                    .get()
                    .getSources()
                    .stream()
                    .filter((sourceAvro) -> sourceAvro.getSourceName()
                            .equalsIgnoreCase(givenSource.getSourceName()))
                    .findAny();

            if (avroSourceOptional.isPresent()) {
                // update source in source list for an existing stream
                com.homeaway.digitalplatform.streamregistry.Source updatedAvroSource = getUpdatedAvroSource(givenSource);

                List<com.homeaway.digitalplatform.streamregistry.Source> avroSourcesWithoutTargetItem = avroSources
                        .get()
                        .getSources()
                        .stream()
                        .filter((sourceAvro) -> !sourceAvro.getSourceName()
                                .equalsIgnoreCase(givenSource.getSourceName())).
                                collect(Collectors.toList());
                avroSourcesWithoutTargetItem.add(updatedAvroSource);

                Sources updateAvroSources = getAvroSourcesFromJsonList(updatedAvroSource, avroSourcesWithoutTargetItem);
                kafkaProducer.log(avroStreamKey, updateAvroSources);
            } else {
                // add to sources list for an existing stream
                com.homeaway.digitalplatform.streamregistry.Source updatedAvroSource = getUpdatedAvroSource(givenSource);

                List<com.homeaway.digitalplatform.streamregistry.Source> avroSourcesList = new ArrayList<>(avroSources.get().getSources());

                avroSourcesList.add(updatedAvroSource);

                Sources updateAvroSources = getAvroSourcesFromJsonList(updatedAvroSource, avroSourcesList);

                kafkaProducer.log(avroStreamKey, updateAvroSources);
            }
        } else {
            // create a new source for new stream

            List<com.homeaway.digitalplatform.streamregistry.Source> tempList = new ArrayList<>();
            com.homeaway.digitalplatform.streamregistry.Source newAvroSource =
                    com.homeaway.digitalplatform.streamregistry.Source.newBuilder()
                            .setStreamName(givenSource.getStreamName())
                            .setSourceName(givenSource.getSourceName())
                            .setSourceType(givenSource.getSourceType())
                            .setStreamSourceConfiguration(givenSource.getStreamSourceConfiguration())
                            .build();

            tempList.add(newAvroSource);
            Sources sources = getAvroSourcesFromJsonList(newAvroSource, tempList);
            kafkaProducer.log(avroStreamKey, sources);
        }


    }

    @Override
    public Optional<Source> get(String streamName, String sourceName) {

        AvroStreamKey avroKey = getAvroKeyFromString(streamName);

        Optional<Sources> sources = kstreams.getAvroStreamForKey(avroKey);

        if (sources.isPresent()) {
            return sources
                    .get()
                    .getSources()
                    .stream()
                    .filter(stream -> stream.getSourceName().equalsIgnoreCase(sourceName))
                    .findAny()
                    .map(this::getModelSourceFromAvroSource);
        }
        return Optional.empty();
    }


    @Override
    public void delete(String streamName, String sourceName) {
        AvroStreamKey stream = getAvroKeyFromString(streamName);

        Optional<com.homeaway.digitalplatform.streamregistry.Sources> avroSources =
                kstreams.getAvroStreamForKey(stream);

        boolean sourceNameMatch = false;

        if (avroSources.isPresent()) {
            sourceNameMatch = avroSources.get()
                    .getSources()
                    .stream()
                    .anyMatch(source -> source.getSourceName().equalsIgnoreCase(sourceName));
        }

        if (sourceNameMatch) {
            // create a list without givenSource and update the list in the producerStateStore
            List<com.homeaway.digitalplatform.streamregistry.Source> updatedSourcesWithoutGivenSource = avroSources.get()
                    .getSources()
                    .stream()
                    .filter(source -> !source.getSourceName().equalsIgnoreCase(sourceName))
                    .collect(Collectors.toList());

            avroSources.get().setSources(updatedSourcesWithoutGivenSource);

            kafkaProducer.log(stream, avroSources.get());
        } else {
            // can't delete what you don't have
            throw new SourceNotFoundException(sourceName);
        }

    }

    @Override
    public List<Source> getAll(String streamName) {

        AvroStreamKey avroStreamKey = getAvroKeyFromString(streamName);

        Optional<Sources> sources =
                kstreams.getAvroStreamForKey(avroStreamKey);

        return sources.map(sourceList -> sourceList.getSources()
                .stream()
                .map(this::getModelSourceFromAvroSource)
                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }


    private Sources getAvroSourcesFromJsonList(com.homeaway.digitalplatform.streamregistry.Source updatedAvroSource,
                                               List<com.homeaway.digitalplatform.streamregistry.Source> avroSourcesWithoutTargetItem) {
        return Sources
                .newBuilder()
                .setStreamName(updatedAvroSource.getStreamName())
                .setSources(avroSourcesWithoutTargetItem)
                .build();
    }

    private com.homeaway.digitalplatform.streamregistry.Source getUpdatedAvroSource(Source givenSource) {
        return com.homeaway.digitalplatform.streamregistry.Source.newBuilder()
                .setStreamName(givenSource.getStreamName())
                .setSourceName(givenSource.getSourceName())
                .setSourceType(givenSource.getSourceType())
                .setStreamSourceConfiguration(givenSource.getStreamSourceConfiguration())
                .build();
    }

    private Source getModelSourceFromAvroSource(
            com.homeaway.digitalplatform.streamregistry.Source avroSource) {
        return Source.builder()
                .streamName(avroSource.getStreamName())
                .sourceName(avroSource.getSourceName())
                .sourceType(avroSource.getSourceType())
                .streamSourceConfiguration(avroSource.getStreamSourceConfiguration())
                .build();
    }

    private AvroStreamKey getAvroKeyFromString(String streamName) {
        return AvroStreamKey.newBuilder()
                .setStreamName(streamName)
                .build();
    }


}
