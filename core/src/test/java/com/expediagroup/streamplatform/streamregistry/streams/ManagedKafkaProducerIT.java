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
package com.expediagroup.streamplatform.streamregistry.streams;

import java.util.AbstractMap;
import java.util.List;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils;
import org.junit.Assert;
import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.AvroStream;
import com.expediagroup.streamplatform.streamregistry.AvroStreamKey;
import com.expediagroup.streamplatform.streamregistry.OperationType;
import com.expediagroup.streamplatform.streamregistry.resource.BaseResourceIT;
import com.expediagroup.streamplatform.streamregistry.utils.AvroModelBuilder;

public class ManagedKafkaProducerIT extends BaseResourceIT {

    @Test
    public void testManagedProducer() {
        // read to end of log
        IntegrationTestUtils.readKeyValues(topicsConfig.getEventStoreTopic().getName(), consumerConfig, 500, 10000);

        String streamName = "testStream_5689";
        AbstractMap.SimpleEntry<AvroStreamKey, AvroStream> avroMessage = new AvroModelBuilder().buildSampleMessage(streamName, OperationType.UPSERT);

        // Push a message
        managedKafkaProducer.log(avroMessage.getKey(), avroMessage.getValue());

        // Verify whether the message is available in the topic
        List<KeyValue<AvroStreamKey, AvroStream>> keyValues = IntegrationTestUtils.readKeyValues(topicsConfig.getEventStoreTopic().getName(), consumerConfig, 400, 1);
        Assert.assertEquals(1, keyValues.size());
        Assert.assertEquals(avroMessage.getKey().toString(), keyValues.get(0).key.toString());
    }

}
