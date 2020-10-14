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
package com.expediagroup.streamplatform.streamregistry.core.accesscontrol.resourcemapper;

import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.DOMAIN_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.PRODUCER_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.SPECIFICATION_TYPE;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.STREAM_NAME;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.STREAM_VERSION;
import static com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AcessControlledResourceFields.ZONE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.expediagroup.streamplatform.streamregistry.accesscontrol.domain.AccessControlledResource;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;

public class ProducerAccessControlledResourceMapperTest {

  private ProducerAccessControlledResourceMapper underTest = new ProducerAccessControlledResourceMapper();

  @Test
  public void canMap() {
    Producer producer = createTestProducer();
    assertEquals(true, underTest.canMap(producer));
  }

  @Test
  public void map() {
    Producer producer = createTestProducer();
    Specification specification = producer.getSpecification();
    String specificationType = "spType";
    when(specification.getType()).thenReturn(specificationType);
    AccessControlledResource accessControlledResource = underTest.map(producer);

    assertEquals("domain", accessControlledResource.getKeys().get(DOMAIN_NAME));
    assertEquals("streamName", accessControlledResource.getKeys().get(STREAM_NAME));
    assertEquals("1", accessControlledResource.getKeys().get(STREAM_VERSION));
    assertEquals("zone", accessControlledResource.getKeys().get(ZONE_NAME));
    assertEquals("producer", accessControlledResource.getKeys().get(PRODUCER_NAME));
    assertEquals(specificationType, accessControlledResource.getAdditionalInfo().get(SPECIFICATION_TYPE));
  }

  private Producer createTestProducer() {
    ProducerKey producerKey = new ProducerKey("domain", "streamName", 1, "zone", "producer");
    Specification specification = mock(Specification.class);
    Status status = mock(Status.class);
    return new Producer(producerKey, specification, status);
  }
}