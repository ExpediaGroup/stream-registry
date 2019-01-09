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
package com.homeaway.streamplatform.streamregistry.streams;

import java.util.AbstractMap;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import com.homeaway.digitalplatform.streamregistry.AvroStream;
import com.homeaway.digitalplatform.streamregistry.AvroStreamKey;
import com.homeaway.digitalplatform.streamregistry.OperationType;
import com.homeaway.streamplatform.streamregistry.resource.BaseResourceIT;
import com.homeaway.streamplatform.streamregistry.utils.AvroModelBuilder;

@Slf4j
public class ManagedKStreamsIT extends BaseResourceIT {

    private static final AvroModelBuilder sampleMessageBuilder = new AvroModelBuilder();

    /**
     * This test class inserts messages to the PRODUCER_TOPIC
     * - Expects messages in the Global State Store
     *
     * @throws InterruptedException
     */
    @Test
    public void testCreateAndReadStream() throws InterruptedException {
        String streamName = "managed-kstreams-test-create-and-read-stream";

        // CREATE a stream
        AbstractMap.SimpleEntry<AvroStreamKey, AvroStream> avroMessage = sampleMessageBuilder.buildSampleMessage(streamName, OperationType.UPSERT);
        managedKafkaProducer.log(avroMessage.getKey(), avroMessage.getValue());

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // Validate whether the stream is in KV-Store
        AvroStream avroStream = managedKStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build()).get();
        Assert.assertEquals(streamName, avroStream.getName());
        Assert.assertEquals(avroMessage.getValue(), avroStream);
    }

    /**
     * This test class inserts messages to the PRODUCER_TOPIC
     * - Expects messages in the Global State Store
     * - Sends another message with same stream name and null values to the PRODUCER_TOPIC
     * - Reads messages from the Global State store and validates if the key is DELETED
     *
     * @throws InterruptedException
     */
    @Test
    public void testCreateAndDeleteStream() throws InterruptedException {
        String streamName = "managed-kstreams-test-create-and-delete-stream";

        // CREATE a stream
        AbstractMap.SimpleEntry<AvroStreamKey, AvroStream> avroMessage = sampleMessageBuilder.buildSampleMessage(streamName, OperationType.UPSERT);
        managedKafkaProducer.log(avroMessage.getKey(), avroMessage.getValue());

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // Validate whether the stream is in KV-Store
        AvroStream avroStream = managedKStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build()).get();
        Assert.assertEquals(streamName, avroStream.getName());
        Assert.assertEquals(avroMessage.getValue(), avroStream);

        // DELETE the stream
        managedKafkaProducer.log(avroMessage.getKey(), null);

        Thread.sleep(TEST_SLEEP_WAIT_MS);

        // Validate whether the stream is null in KV-Store
        Optional<AvroStream> nullAvroStream = managedKStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build());

        Assert.assertEquals(Optional.empty(), nullAvroStream);
    }

    /**
     * This test class inserts messages to the PRODUCER_TOPIC
     * - Expects messages in the Global State Store
     * - Update the Message. Sample: update Owner
     * - Reads messages from the Global State store and validates if the updated message is available
     *
     * @throws InterruptedException
     */
    @Test
    public void testCreateAndUpdateAStream() throws InterruptedException {
        String streamName = "managed-kstreams-test-create-and-update-stream";

        // CREATE a stream
        AbstractMap.SimpleEntry<AvroStreamKey, AvroStream> avroMessage = sampleMessageBuilder.buildSampleMessage(streamName, OperationType.UPSERT);
        managedKafkaProducer.log(avroMessage.getKey(), avroMessage.getValue());

        // Verify whether the stream is available in KV-Store
        Thread.sleep(TEST_SLEEP_WAIT_MS);
        // Validate whether the stream is null in KV-Store
        AvroStream avroStream = managedKStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build()).get();
        Assert.assertEquals(streamName, avroStream.getName());
        Assert.assertEquals("Existing Owner value is: user-1 ","user-1", avroStream.getOwner());

        // UPDATE the stream
        long currentTimeMillis = System.currentTimeMillis();
        avroMessage.getValue().setOwner("user-2");
        avroMessage.getValue().setUpdated(currentTimeMillis);
        managedKafkaProducer.log(avroMessage.getKey(), avroMessage.getValue());

        // Verify whether the stream owner is updated.
        Thread.sleep(TEST_SLEEP_WAIT_MS);
        avroStream = managedKStreams.getAvroStreamForKey(AvroStreamKey.newBuilder().setStreamName(streamName).build()).get();
        Assert.assertEquals("Owner Value has been updated.","user-2", avroStream.getOwner());
        Assert.assertEquals("Message from the state store and input message are same.",avroMessage.getValue(), avroStream);
    }
}
