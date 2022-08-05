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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProcessBindingKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProcessBindingValidatorTest {

  @Mock
  private StreamBindingRepository streamBindingRepository;

  @Mock
  private DomainRepository domainRepository;

  @Mock
  private ZoneRepository zoneRepository;

  ObjectMapper mapper = new ObjectMapper();

  private ProcessBindingValidator processBindingValidator;

  @Before
  public void initialize() {
    processBindingValidator = new ProcessBindingValidator(
      new StreamBindingView(streamBindingRepository),
      new ZoneView(zoneRepository),
      new DomainView(domainRepository),
      new ProcessBindingKeyValidator(),
      new SpecificationValidator()
    );
  }

  @Test
  public void processNameIsInvalid() {
    ProcessBinding pb = new ProcessBinding();
    pb.setKey(new ProcessBindingKey("domain","zone", "1_name"));
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForCreate(pb));
    Assertions.assertEquals(ex.getMessage(), "Invalid name '1_name'. Names must conform to pattern ^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");
  }

  @Test
  public void domainIsNotExist() {
    ProcessBinding pb = createTestProcessBinding();
    when(domainRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForCreate(pb));
    Assertions.assertEquals(ex.getMessage(), "Domain [domain] does not exist");
  }

  @Test
  public void zoneIsNotExist() {
    ProcessBinding pb = createTestProcessBinding();
    final Domain entity = mock(Domain.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(entity));
    when(zoneRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForCreate(pb));
    Assertions.assertEquals(ex.getMessage(), "Zone [ZoneKey(name=aws_us_east_1)] does not exist");
  }

  @Test
  public void inputIsNotExists() {
    ProcessBinding pb = createTestProcessBinding();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamBindingRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForCreate(pb));
    Assertions.assertEquals(ex.getMessage(), "Input StreamBinding Key [StreamBindingKey(streamDomain=inputDomain, streamName=streamInputName, streamVersion=1, infrastructureZone=zone, infrastructureName=infraName)] does not exist");
  }

  @Test
  public void outputIsNotExists() {
    ProcessBinding pb = createTestProcessBinding();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final StreamBinding streamBindingEntity = mock(StreamBinding.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamBindingRepository.findById(any())).thenReturn(Optional.of(streamBindingEntity)).thenReturn(Optional.empty());
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForCreate(pb));
    Assertions.assertEquals(ex.getMessage(), "Output StreamBinding Key [StreamBindingKey(streamDomain=inputDomain, streamName=streamOutputName, streamVersion=1, infrastructureZone=zone, infrastructureName=infraName)] does not exist");
  }

  @Test
  public void specificationIsNotValidForCreate() {
    ProcessBinding pb = createTestProcessBinding();
    pb.getSpecification().setConfiguration(null);
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final StreamBinding streamBindingEntity = mock(StreamBinding.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamBindingRepository.findById(any())).thenReturn(Optional.of(streamBindingEntity)).thenReturn(Optional.of(streamBindingEntity));
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForCreate(pb));
    Assertions.assertEquals(ex.getMessage(), "Configuration must not be null.");
  }

  @Test
  public void specificationIsNotValidForUpdate() {
    ProcessBinding pb = createTestProcessBinding();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final StreamBinding streamBindingEntity = mock(StreamBinding.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamBindingRepository.findById(any())).thenReturn(Optional.of(streamBindingEntity)).thenReturn(Optional.of(streamBindingEntity));
    ProcessBinding existingPb = createTestProcessBinding();
    existingPb.getSpecification().setType("type");
    ValidationException ex = Assertions.assertThrows(ValidationException.class, () -> processBindingValidator.validateForUpdate(pb, existingPb));
    Assertions.assertEquals(ex.getMessage(), "Configuration must be of the same type as the existing.");
  }

  @Test
  public void validateBindingWithNoError() {
    ProcessBinding pb = createTestProcessBinding();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final StreamBinding streamBindingEntity = mock(StreamBinding.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamBindingRepository.findById(any())).thenReturn(Optional.of(streamBindingEntity)).thenReturn(Optional.of(streamBindingEntity));
    Assertions.assertDoesNotThrow(() -> processBindingValidator.validateForCreate(pb));
    verify(domainRepository).findById(any());
    verify(zoneRepository).findById(any());
    verify(streamBindingRepository, times(2)).findById(any());
  }

  private ProcessBinding createTestProcessBinding() {
    ProcessBinding pb = new ProcessBinding();
    pb.setKey(new ProcessBindingKey("domain", "aws_us_east_1", "process"));
    pb.setStatus(new Status(mapper.createObjectNode()));
    pb.setZone(new ZoneKey("aws_us_east_1"));
    pb.setSpecification(
      new Specification(
        "description",
        Collections.emptyList(),
        "generic",
        mapper.createObjectNode(),
        Collections.emptyList(), ""
      )
    );
    pb.setInputs(Collections.singletonList(new ProcessInputStreamBinding(new StreamBindingKey("inputDomain", "streamInputName", 1, "zone", "infraName"), mapper.createObjectNode())));
    pb.setOutputs(Collections.singletonList(new ProcessOutputStreamBinding(new StreamBindingKey("inputDomain", "streamOutputName", 1, "zone", "infraName"), mapper.createObjectNode())));
    return pb;
  }
}
