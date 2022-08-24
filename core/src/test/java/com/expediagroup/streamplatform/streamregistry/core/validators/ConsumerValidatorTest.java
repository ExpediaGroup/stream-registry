/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.ConsumerKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerValidatorTest {
  @Mock
  private StreamRepository streamRepository;

  @Mock
  private DomainRepository domainRepository;

  @Mock
  private ZoneRepository zoneRepository;

  private final ObjectMapper mapper = new ObjectMapper();

  private ConsumerValidator consumerValidator;

  @Before
  public void initialize() {
    consumerValidator = new ConsumerValidator(
      new StreamView(streamRepository),
      new ZoneView(zoneRepository),
      new DomainView(domainRepository),
      new ConsumerKeyValidator(),
      new SpecificationValidator()
    );
  }

  @Test
  public void consumerNameIsValid() {
    Consumer consumer = createTestConsumer();
    consumer.getKey().setName("1_name");
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Invalid name '1_name'. Names must conform to pattern ^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");
  }

  @Test
  public void domainIsNotExist() {
    Consumer consumer = createTestConsumer();
    when(domainRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Domain does not exist");
  }

  @Test
  public void streamIsNotExist() {
    Consumer consumer = createTestConsumer();
    final Domain domainEntity = mock(Domain.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Stream does not exist");
  }

  @Test
  public void ZoneIsNotExist() {
    Consumer consumer = createTestConsumer();
    final Domain domainEntity = mock(Domain.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> consumerValidator.validateForCreate(consumer));
    assertEquals(ex.getMessage(), "Zone does not exist");
  }

  public Consumer createTestConsumer() {
    Consumer consumer = new Consumer();
    consumer.setKey(new ConsumerKey("domain", "stream", 1, "zone", "consumer"));
    Specification specification = new Specification();
    specification.setType("egsp.kafka");
    specification.setConfiguration(mapper.createObjectNode());
    specification.setTags(Collections.emptyList());
    consumer.setSpecification(specification);
    Status status = new Status(mapper.createObjectNode());
    consumer.setStatus(status);
    return consumer;
  }
}
