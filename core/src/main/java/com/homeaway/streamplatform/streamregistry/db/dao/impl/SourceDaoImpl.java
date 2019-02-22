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

import static com.homeaway.streamplatform.streamregistry.model.SourceType.SOURCE_TYPES;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Singleton;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.dropwizard.lifecycle.Managed;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import com.homeaway.digitalplatform.streamregistry.sources.CreateRequested;
import com.homeaway.digitalplatform.streamregistry.sources.FailRequested;
import com.homeaway.digitalplatform.streamregistry.sources.Header;
import com.homeaway.digitalplatform.streamregistry.sources.PauseRequested;
import com.homeaway.digitalplatform.streamregistry.sources.ResumeRequested;
import com.homeaway.digitalplatform.streamregistry.sources.StartRequested;
import com.homeaway.digitalplatform.streamregistry.sources.StopRequested;
import com.homeaway.digitalplatform.streamregistry.sources.UpdateRequested;
import com.homeaway.streamplatform.streamregistry.db.dao.SourceDao;
import com.homeaway.streamplatform.streamregistry.exceptions.SourceNotFoundException;
import com.homeaway.streamplatform.streamregistry.exceptions.UnsupportedSourceTypeException;
import com.homeaway.streamplatform.streamregistry.model.Source;
import com.homeaway.streamplatform.streamregistry.streams.KStreamsProcessorListener;


/**
 * KStreams event processor implementation of the SourceDao
 * All calls to this Dao represent asynchronous/eventually consistent actions.
 */
@Singleton
@Slf4j
public class SourceDaoImpl implements SourceDao, Managed {


    // FSM States
    private static final Map<String, Map<String, String>> MAP_OF_COMMAND_TO_STATE_TRANSITIONS;

    private static final Map<String, String> CREATE_REQUEST_TRANSITIONS;

    private static final Map<String, String> UPDATE_REQUEST_TRANSITIONS;

    private static final Map<String, String> START_REQUEST_TRANSITIONS;

    private static final Map<String, String> STOP_REQUEST_TRANSITIONS;

    private static final Map<String, String> PAUSE_REQUEST_TRANSITIONS;

    private static final Map<String, String> RESUME_REQUEST_TRANSITIONS;

    private static final Map<String, String> FAIL_REQUEST_TRANSITIONS;

    static {

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS = new HashMap<>();

        CREATE_REQUEST_TRANSITIONS = new HashMap<>();

        CREATE_REQUEST_TRANSITIONS.put(Status.UNASSIGNED.toString(), Status.UNASSIGNED.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(CreateRequested.class.getName(), CREATE_REQUEST_TRANSITIONS);

        UPDATE_REQUEST_TRANSITIONS = new HashMap<>();
        UPDATE_REQUEST_TRANSITIONS.put(Status.NOT_RUNNING.toString(), Status.TRANSITIONING.toString());
        UPDATE_REQUEST_TRANSITIONS.put(Status.FAILED.toString(), Status.TRANSITIONING.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(UpdateRequested.class.getName(), UPDATE_REQUEST_TRANSITIONS);

        START_REQUEST_TRANSITIONS = new HashMap<>();
        START_REQUEST_TRANSITIONS.put(Status.UNASSIGNED.toString(), Status.TRANSITIONING.toString());
        START_REQUEST_TRANSITIONS.put(Status.NOT_RUNNING.toString(), Status.TRANSITIONING.toString());
        START_REQUEST_TRANSITIONS.put(Status.FAILED.toString(), Status.TRANSITIONING.toString());
        START_REQUEST_TRANSITIONS.put(Status.TRANSITIONING.toString(), Status.RUNNING.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(StartRequested.class.getName(), START_REQUEST_TRANSITIONS);

        STOP_REQUEST_TRANSITIONS = new HashMap<>();
        STOP_REQUEST_TRANSITIONS.put(Status.RUNNING.toString(), Status.TRANSITIONING.toString());
        STOP_REQUEST_TRANSITIONS.put(Status.FAILED.toString(), Status.TRANSITIONING.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(StopRequested.class.getName(), STOP_REQUEST_TRANSITIONS);

        PAUSE_REQUEST_TRANSITIONS = new HashMap<>();
        PAUSE_REQUEST_TRANSITIONS.put(Status.RUNNING.toString(), Status.TRANSITIONING.toString());
        PAUSE_REQUEST_TRANSITIONS.put(Status.FAILED.toString(), Status.TRANSITIONING.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(PauseRequested.class.getName(), PAUSE_REQUEST_TRANSITIONS);

        RESUME_REQUEST_TRANSITIONS = new HashMap<>();
        RESUME_REQUEST_TRANSITIONS.put(Status.NOT_RUNNING.toString(), Status.TRANSITIONING.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(ResumeRequested.class.getName(), RESUME_REQUEST_TRANSITIONS);

        FAIL_REQUEST_TRANSITIONS = new HashMap<>();
        FAIL_REQUEST_TRANSITIONS.put(Status.RUNNING.toString(), Status.TRANSITIONING.toString());

        MAP_OF_COMMAND_TO_STATE_TRANSITIONS.put(FailRequested.class.getName(), FAIL_REQUEST_TRANSITIONS);
    }

    /**
     * Application id for the Source entity processor
     */
    public static final String SOURCE_ENTITY_PROCESSOR_APP_ID = "source-entity-processor-v1";

    /**
     * Source entity store name
     */
    public static final String SOURCE_ENTITY_STORE_NAME = "source-entity-store-v1";

    /**
     * Source entity topic name
     */
    public static final String SOURCE_ENTITY_TOPIC_NAME = "source-entity-v1";

    /**
     * Source command topic name
     */
    public static final String SOURCE_COMMANDS_TOPIC_NAME = "source-command-events-v1";

    /**
     * Source processor dir name
     */
    public static final String SOURCE_PROCESSOR_DIRNAME = "/tmp/stream-registry/streams/sourceEntity";

    private static final File SOURCE_PROCESSOR_DIR = new File(SOURCE_PROCESSOR_DIRNAME);

    private final Properties commonConfig;
    private final KStreamsProcessorListener testListener;
    private boolean isRunning = false;

    @Getter
    private KafkaStreams sourceProcessor;

    @Getter
    private GlobalKTable<String, com.homeaway.digitalplatform.streamregistry.sources.Source> sourceEntityKTable;

    private KafkaProducer<String, CreateRequested> createRequestProducer;
    private KafkaProducer<String, UpdateRequested> updateRequestProducer;
    private KafkaProducer<String, StartRequested> startRequestProducer;
    private KafkaProducer<String, PauseRequested> pauseRequestProducer;
    private KafkaProducer<String, StopRequested> stopRequestProducer;
    private KafkaProducer<String, ResumeRequested> resumeRequestProducer;
    private KafkaProducer<String, Source> deleteProducer;

    @Getter
    private ReadOnlyKeyValueStore<String, com.homeaway.digitalplatform.streamregistry.sources.Source> sourceEntityStore;

    /**
     * Instantiates a new Source dao.
     *
     * @param commonConfig the common config
     */
    public SourceDaoImpl(Properties commonConfig) {
        this(commonConfig, null);
    }

    /**
     * Instantiates a new Source dao.
     *
     * @param commonConfig the common config
     * @param testListener the test listener
     */
    public SourceDaoImpl(Properties commonConfig, KStreamsProcessorListener testListener) {
        this.commonConfig = commonConfig;
        this.testListener = testListener;
    }

    @Override
    public void inserting(Source source) {

        Optional<Source> optionalSource = get(source.getSourceName());

        if (!optionalSource.isPresent()) {
            validateSourceIsSupported(source);

            ProducerRecord<String, CreateRequested> record = new ProducerRecord<>(SOURCE_COMMANDS_TOPIC_NAME, source.getSourceName(),
                    CreateRequested.newBuilder()
                            .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                            .setSourceName(source.getSourceName())
                            .setSource(modelToAvroSource(source, Status.UNASSIGNED.toString()))
                            .build());
            Future<RecordMetadata> future = createRequestProducer.send(record);
            // Wait for the message synchronously
            try {
                future.get();
                log.info("inserting - {}", source.getSourceName());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error producing message", e);
            }
        } else {
            throw new IllegalStateException("Source already exists. Cannot inserting source - " + source.getSourceName());
        }
    }

    @Override
    public void updating(Source source) {

        Optional<Source> optionalSource = get(source.getSourceName());

        if (optionalSource.isPresent()) {

            String status = optionalSource.get().getStatus();

            boolean stateTransitionAllowed = UPDATE_REQUEST_TRANSITIONS
                    .keySet()
                    .stream()
                    .anyMatch((status::equalsIgnoreCase));

            if (stateTransitionAllowed) {
                ProducerRecord<String, UpdateRequested> record = new ProducerRecord<>(SOURCE_COMMANDS_TOPIC_NAME, source.getSourceName(),
                        UpdateRequested.newBuilder()
                                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                                .setSourceName(source.getSourceName())
                                .setSource(modelToAvroSource(source, optionalSource.get().getStatus()))
                                .build());
                Future<RecordMetadata> future = updateRequestProducer.send(record);
                updateRequestProducer.flush();
                // Wait for the message synchronously
                try {
                    future.get();
                    log.info("updating - {}", source.getSourceName());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error producing message", e);
                }
            } else {
                throw new IllegalStateException("Source in transition. Please try again later");
            }
        } else {
            throw new IllegalStateException("Source does not exist. Cannot updating non-existent source - " + source.getSourceName());
        }

    }

    @Override
    public Optional<Source> get(String sourceName) {
        Preconditions.checkNotNull(sourceEntityStore, "Entity store cannot be null");
        com.homeaway.digitalplatform.streamregistry.sources.Source avroSource = sourceEntityStore.get(sourceName);
        if (avroSource == null) {
            return Optional.empty();
        }
        return Optional.of(avroToModelSource(avroSource));
    }

    @Override
    public void starting(String sourceName) throws SourceNotFoundException {

        Optional<Source> source = get(sourceName);
        if (source.isPresent()) {

            String status = source.get().getStatus();

            boolean stateTransitionAllowed = START_REQUEST_TRANSITIONS
                    .keySet()
                    .stream()
                    .anyMatch(status::equalsIgnoreCase);


            if (stateTransitionAllowed) {
                ProducerRecord<String, StartRequested> record = new ProducerRecord<>(SOURCE_COMMANDS_TOPIC_NAME, source.get().getSourceName(),
                        StartRequested.newBuilder()
                                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                                .setSourceName(source.get().getSourceName())
                                .build());
                Future<RecordMetadata> future = startRequestProducer.send(record);
                startRequestProducer.flush();

                // Wait for the message synchronously
                try {
                    future.get();
                    log.info("starting - {}", sourceName);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error producing message", e);
                }
            } else {
                throw new IllegalStateException("Source in transition. Please try again later");
            }
        } else {
            throw new SourceNotFoundException(sourceName);
        }
    }

    @Override
    public void pausing(String sourceName) throws SourceNotFoundException {

        Optional<Source> source = get(sourceName);
        if (source.isPresent()) {

            String status = source.get().getStatus();

            boolean stateTransitionAllowed = PAUSE_REQUEST_TRANSITIONS
                    .keySet()
                    .stream()
                    .anyMatch((status::equalsIgnoreCase));

            if (stateTransitionAllowed) {

                ProducerRecord<String, PauseRequested> record = new ProducerRecord<>(SOURCE_COMMANDS_TOPIC_NAME, source.get().getSourceName(),
                        PauseRequested.newBuilder()
                                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                                .setSourceName(source.get().getSourceName())
                                .build());
                Future<RecordMetadata> future = pauseRequestProducer.send(record);
                pauseRequestProducer.flush();

                // Wait for the message synchronously
                try {
                    future.get();
                    log.info("pausing - {}", sourceName);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error producing message", e);
                }
            } else {
                throw new IllegalStateException("Source in transition. Please try again later");
            }
        } else {
            throw new SourceNotFoundException(sourceName);
        }

    }

    @Override
    public void resuming(String sourceName) throws SourceNotFoundException {

        Optional<Source> source = get(sourceName);
        if (source.isPresent()) {

            String status = source.get().getStatus();

            boolean stateTransitionAllowed = RESUME_REQUEST_TRANSITIONS
                    .keySet()
                    .stream()
                    .anyMatch((status::equalsIgnoreCase));

            if (stateTransitionAllowed) {

                ProducerRecord<String, ResumeRequested> record = new ProducerRecord<>(SOURCE_COMMANDS_TOPIC_NAME, source.get().getSourceName(),
                        ResumeRequested.newBuilder()
                                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                                .setSourceName(source.get().getSourceName())
                                .build());
                Future<RecordMetadata> future = resumeRequestProducer.send(record);
                resumeRequestProducer.flush();
                // Wait for the message synchronously
                try {
                    future.get();
                    log.info("resuming - {}", sourceName);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error producing message", e);
                }
            } else {
                throw new IllegalStateException("Source in transition. Please try again later");
            }
        } else {
            throw new SourceNotFoundException(sourceName);
        }
    }

    @Override
    public void stopping(String sourceName) throws SourceNotFoundException {

        Optional<Source> source = get(sourceName);
        if (source.isPresent()) {

            String status = source.get().getStatus();

            boolean stateTransitionAllowed = RESUME_REQUEST_TRANSITIONS
                    .keySet()
                    .stream()
                    .anyMatch((status::equalsIgnoreCase));

            if (stateTransitionAllowed) {

                ProducerRecord<String, StopRequested> record = new ProducerRecord<>(SOURCE_COMMANDS_TOPIC_NAME, source.get().getSourceName(),
                        StopRequested.newBuilder()
                                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                                .setSourceName(source.get().getSourceName())
                                .build());
                Future<RecordMetadata> future = stopRequestProducer.send(record);
                stopRequestProducer.flush();
                // Wait for the message synchronously
                try {
                    future.get();
                    log.info("stopping - {}", sourceName);
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error producing message", e);
                }
            } else {
                throw new IllegalStateException("Source in transition. Please try again later");
            }
        } else {
            throw new SourceNotFoundException(sourceName);
        }
    }

    @Override
    public String getStatus(String sourceName) {
        Optional<com.homeaway.digitalplatform.streamregistry.sources.Source> source =
                Optional.ofNullable(sourceEntityStore.get(sourceName));

        if (!source.isPresent()) {
            throw new SourceNotFoundException(sourceName);
        }
        return source.get().getStatus();
    }

    @Override
    public void deleting(String sourceName) {
        ProducerRecord<String, Source> record = new ProducerRecord<>(SOURCE_ENTITY_TOPIC_NAME, sourceName, null);
        Future<RecordMetadata> future = deleteProducer.send(record);

        deleteProducer.flush();

        // Wait for the message synchronously
        try {
            future.get();
            log.info("deleting - {}", sourceName);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error producing message", e);
        }

    }

    @Override
    public List<Source> getAll() {
        List<Source> sources = new ArrayList<>();
        KeyValueIterator<String, com.homeaway.digitalplatform.streamregistry.sources.Source> iterator =
                sourceEntityStore.all();
        iterator.forEachRemaining(keyValue -> sources.add(avroToModelSource(keyValue.value)));
        return sources;
    }


    private void initiateProcessor() {
        Properties sourceProcessorConfig = new Properties();
        commonConfig.forEach(sourceProcessorConfig::put);
        sourceProcessorConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, SOURCE_ENTITY_PROCESSOR_APP_ID);
        sourceProcessorConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        sourceProcessorConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        sourceProcessorConfig.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());
        sourceProcessorConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        sourceProcessorConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        sourceProcessorConfig.put(StreamsConfig.STATE_DIR_CONFIG, SOURCE_PROCESSOR_DIR.getPath());
        sourceProcessorConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
        sourceProcessorConfig.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");


        final Map<String, String> serdeConfig =
                Collections.singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                        commonConfig.getProperty(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));

        final Serde<SpecificRecord> commandEventSerde = new SpecificAvroSerde<>();
        commandEventSerde.configure(serdeConfig, false);

        final Serde<com.homeaway.digitalplatform.streamregistry.sources.Source> sourceSpecificAvroSerde = new SpecificAvroSerde<>();
        sourceSpecificAvroSerde.configure(serdeConfig, false);

        StreamsBuilder builder = new StreamsBuilder();
        declareStreamProcessor(builder, commandEventSerde, sourceSpecificAvroSerde);

        sourceProcessor = new KafkaStreams(builder.build(), sourceProcessorConfig);

        sourceProcessor.setStateListener((newState, oldState) -> {
            if (!isRunning && newState == KafkaStreams.State.RUNNING) {
                isRunning = true;
                if (testListener != null) {
                    testListener.stateStoreInitialized();
                }
            }
        });

        sourceProcessor.setUncaughtExceptionHandler((t, e) -> log.error("Source entity processor job failed", e));
        sourceProcessor.start();
        log.info("Topology started with properties - {}", sourceProcessorConfig);
        log.info("Source entity state Store Name: {}", SOURCE_ENTITY_STORE_NAME);
        sourceEntityStore = sourceProcessor.store(sourceEntityKTable.queryableStoreName(), QueryableStoreTypes.keyValueStore());

    }

    /**
     * Declares the stream topology and declares the corresponding GlobalKTable
     *
     * @param builder             Empty StreamsBuilder instance
     * @param specificRecordSerde The Serde to use for the command events
     * @param sourceSerde         The Serde to use for the global ktable state store
     */
    @SuppressWarnings("unchecked")
    public void declareStreamProcessor(StreamsBuilder builder, Serde<SpecificRecord> specificRecordSerde,
                                       Serde<com.homeaway.digitalplatform.streamregistry.sources.Source> sourceSerde) {
        Serde<String> keySerde = Serdes.String();

        // first declare our stream with command events as the input, note we specify our serdes here
        KStream<String, SpecificRecord> kstream = builder.stream(SOURCE_COMMANDS_TOPIC_NAME, Consumed.with(keySerde, specificRecordSerde));

        // now create our source entities (with compacted topic)
        final CommandTypeProcessor commandTypeProcessor = new CommandTypeProcessor();
        kstream.map((sourceName, command) -> commandTypeProcessor.process(command))
                .to(SOURCE_ENTITY_TOPIC_NAME, Produced.with(keySerde, sourceSerde));

        // finally bind a store to the source entity topic
        sourceEntityKTable = builder.globalTable(SOURCE_ENTITY_TOPIC_NAME, Consumed.with(keySerde, sourceSerde), Materialized.<String, com.homeaway.digitalplatform.streamregistry.sources.Source,
                KeyValueStore<Bytes, byte[]>>as(SOURCE_ENTITY_STORE_NAME)
                .withKeySerde(keySerde)
                .withValueSerde(sourceSerde));
    }

    public enum Status {
        UNASSIGNED("UNASSIGNED"),
        NOT_RUNNING("NOT_RUNNING"),
        RUNNING("RUNNING"),
        TRANSITIONING("TRANSITIONING"),
        FAILED("FAILED");

        private final String status;

        /**
         * @param status string
         */
        Status(final String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return status;
        }
    }


    private KeyValue<String, com.homeaway.digitalplatform.streamregistry.sources.Source>
    getSourceKeyValue(com.homeaway.digitalplatform.streamregistry.sources.Source source, String canonicalClassName) {

        Preconditions.checkNotNull(MAP_OF_COMMAND_TO_STATE_TRANSITIONS.get(canonicalClassName), "Unsupported command type");
        Preconditions.checkNotNull(MAP_OF_COMMAND_TO_STATE_TRANSITIONS.get(canonicalClassName).get(source.getStatus()), "Unsupported state transition");

        String newStatus = MAP_OF_COMMAND_TO_STATE_TRANSITIONS.get(canonicalClassName).get(source.getStatus());

        return new KeyValue<>(source.getSourceName(), com.homeaway.digitalplatform.streamregistry.sources.Source.newBuilder()
                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                .setSourceName(source.getSourceName())
                .setStreamName(source.getStreamName())
                .setSourceType(source.getSourceType())
                .setStatus(newStatus) // State transition happens here
                .setConfiguration(source.getConfiguration())
                .setTags(source.getTags())
                .build());
    }

    private KeyValue<String, com.homeaway.digitalplatform.streamregistry.sources.Source>
    getSourceKeyValueForExistingSource(String sourceName, String canonicalClassName) {

        Optional<Source> optionalExistingSource = get(sourceName);
        if (!optionalExistingSource.isPresent()) {
            return null;
        }

        Source existingSource = optionalExistingSource.get();

        com.homeaway.digitalplatform.streamregistry.sources.Source avroSource = new com.homeaway.digitalplatform.streamregistry.sources.Source();
        avroSource.setSourceName(existingSource.getSourceName());
        avroSource.setStreamName(existingSource.getStreamName());
        avroSource.setSourceType(existingSource.getSourceType());
        avroSource.setConfiguration(existingSource.getConfiguration());
        avroSource.setTags(existingSource.getTags());

        return getSourceKeyValue(avroSource, canonicalClassName);
    }


    private void validateSourceIsSupported(Source source) {
        boolean supportedSource = SOURCE_TYPES.stream()
                .anyMatch(sourceType -> sourceType.equalsIgnoreCase(source.getSourceType()));

        if (!supportedSource) {
            throw new UnsupportedSourceTypeException(source.getSourceType());
        }
    }

    @Override
    public void start() {
        initiateProcessor();

        Properties producerConfig = new Properties();
        commonConfig.forEach(producerConfig::put);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        producerConfig.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

        createRequestProducer = new KafkaProducer<>(producerConfig);
        updateRequestProducer = new KafkaProducer<>(producerConfig);
        startRequestProducer = new KafkaProducer<>(producerConfig);
        pauseRequestProducer = new KafkaProducer<>(producerConfig);
        stopRequestProducer = new KafkaProducer<>(producerConfig);
        resumeRequestProducer = new KafkaProducer<>(producerConfig);
        deleteProducer = new KafkaProducer<>(producerConfig);

        log.info("All the producers were initiated with the following common configuration - {}", producerConfig);
    }


    @Override
    public void stop() {
        sourceProcessor.close();
        createRequestProducer.close();
        updateRequestProducer.close();
        deleteProducer.close();
        log.info("Source command processor closed");
        log.info("Source entity processor closed");
        log.info("Source create producer closed");
        log.info("Source updating producer closed");
        log.info("Source deleting producer closed");
    }

    private Source avroToModelSource(com.homeaway.digitalplatform.streamregistry.sources.Source avroSource) {
        return Source.builder()
                .sourceName(avroSource.getSourceName())
                .sourceType(avroSource.getSourceType())
                .streamName(avroSource.getStreamName())
                .status(avroSource.getStatus())
                .created(avroSource.getHeader().getTime())
                .configuration(avroSource.getConfiguration())
                .tags(avroSource.getTags())
                .build();
    }

    private com.homeaway.digitalplatform.streamregistry.sources.Source modelToAvroSource(Source source, String status) {
        return com.homeaway.digitalplatform.streamregistry.sources.Source.newBuilder()
                .setHeader(Header.newBuilder().setTime(System.currentTimeMillis()).build())
                .setSourceName(source.getSourceName())
                .setSourceType(source.getSourceType())
                .setStreamName(source.getStreamName())
                .setStatus(status)
                .setConfiguration(source.getConfiguration())
                .setTags(source.getTags())
                .build();
    }

    private class CommandTypeProcessor<T> {

        private CommandTypeProcessor() {}

        private KeyValue<String, com.homeaway.digitalplatform.streamregistry.sources.Source> process(T entity) {
            if (entity instanceof CreateRequested) {
                CreateRequested createRequested = (CreateRequested) entity;
                return getSourceKeyValue(createRequested.getSource(), createRequested.getClass().getCanonicalName());
            } else if (entity instanceof UpdateRequested) {
                UpdateRequested updateRequested = (UpdateRequested) entity;
                return getSourceKeyValue(updateRequested.getSource(), updateRequested.getClass().getCanonicalName());
            } else if (entity instanceof StartRequested) {
                StartRequested startRequested = (StartRequested) entity;
                return getSourceKeyValueForExistingSource(startRequested.getSourceName(), startRequested.getClass().getCanonicalName());
            } else if (entity instanceof StopRequested) {
                StopRequested stopRequested = (StopRequested) entity;
                return getSourceKeyValueForExistingSource(stopRequested.getSourceName(), stopRequested.getClass().getCanonicalName());
            } else if (entity instanceof PauseRequested) {
                PauseRequested pauseRequested = (PauseRequested) entity;
                return getSourceKeyValueForExistingSource(pauseRequested.getSourceName(), pauseRequested.getClass().getCanonicalName());
            } else if (entity instanceof ResumeRequested) {
                ResumeRequested resumeRequested = new ResumeRequested();
                return getSourceKeyValueForExistingSource(resumeRequested.getSourceName(), resumeRequested.getClass().getCanonicalName());
            } else {
                throw new IllegalStateException("Unsupported command type");
            }

        }
    }
}