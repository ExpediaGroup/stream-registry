/**
 * Copyright (C) 2018-2023 Expedia, Inc.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.ProcessKeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ZoneRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProcessValidatorTest {

  @Mock
  private StreamRepository streamRepository;

  @Mock
  private DomainRepository domainRepository;

  @Mock
  private ZoneRepository zoneRepository;

  private final ObjectMapper mapper = new ObjectMapper();

  private ProcessValidator processValidator;

  @Before
  public void initialize() {
    processValidator = new ProcessValidator(
      new StreamView(streamRepository),
      new ZoneView(zoneRepository),
      new DomainView(domainRepository),
      new ProcessKeyValidator(),
      new SpecificationValidator()
    );
  }

  @Test
  public void processNameIsInvalid() {
    Process p = new Process();
    p.setKey(new ProcessKey("domain","1_name"));
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForCreate(p));
    assertEquals(ex.getMessage(), "Invalid name '1_name'. Names must conform to pattern ^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");
  }

  @Test
  public void domainIsNotExist() {
    Process p = createTestProcess();
    when(domainRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForCreate(p));
    assertEquals(ex.getMessage(), "Domain [domain] does not exist");
  }

  @Test
  public void zoneIsNotExist() {
    Process p = createTestProcess();
    final Domain entity = mock(Domain.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(entity));
    when(zoneRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForCreate(p));
    assertEquals(ex.getMessage(), "Zone [ZoneKey(name=aws_us_east_1)] does not exist");
  }

  @Test
  public void inputIsNotExists() {
    Process p = createTestProcess();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForCreate(p));
    assertEquals(ex.getMessage(), "Input stream [StreamKey(domain=inputDomain, name=streamInputName, version=1)] does not exist");
  }

  @Test
  public void outputIsNotExists() {
    Process p = createTestProcess();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity)).thenReturn(Optional.empty());
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForCreate(p));
    assertEquals(ex.getMessage(), "Output stream [StreamKey(domain=outputDomain, name=streamOutputName, version=1)] does not exist");
  }

  @Test
  public void specificationIsNotValidForCreate() {
    Process p = createTestProcess();
    p.getSpecification().setConfiguration(null);
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity)).thenReturn(Optional.of(streamEntity));
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForCreate(p));
    assertEquals(ex.getMessage(), "Configuration must not be null.");
  }

  @Test
  public void specificationIsNotValidForUpdate() {
    Process p = createTestProcess();
    p.getSpecification().setConfiguration(mapper.createObjectNode());
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity)).thenReturn(Optional.of(streamEntity));
    Process existingProcess = createTestProcess();
    existingProcess.getSpecification().setType("egsp.kstream");
    ValidationException ex = assertThrows(ValidationException.class, () -> processValidator.validateForUpdate(p, existingProcess));
    assertEquals(ex.getMessage(), "Configuration must be of the same type as the existing.");
  }

  @Test
  public void validateBindingWithNoError() {
    Process p = createTestProcess();
    final Domain domainEntity = mock(Domain.class);
    final Zone zoneEntity = mock(Zone.class);
    final Stream streamEntity = mock(Stream.class);
    when(domainRepository.findById(any())).thenReturn(Optional.of(domainEntity));
    when(zoneRepository.findById(any())).thenReturn(Optional.of(zoneEntity));
    when(streamRepository.findById(any())).thenReturn(Optional.of(streamEntity)).thenReturn(Optional.of(streamEntity));
    assertDoesNotThrow(() -> processValidator.validateForCreate(p));
    verify(domainRepository, times(1)).findById(any());
    verify(zoneRepository, times(1)).findById(any());
    verify(streamRepository, times(2)).findById(any());
  }
  private Process createTestProcess() {
    Process p = new Process();
    Specification specification = new Specification();
    specification.setConfiguration(mapper.createObjectNode());
    specification.setType("egsp.kafka");
    p.setSpecification(specification);
    List<ProcessInputStream> inputs = new ArrayList<>();
    ProcessInputStream pis = new ProcessInputStream(new StreamKey("inputDomain","streamInputName",1), mapper.createObjectNode());
    inputs.add(pis);
    p.setInputs(inputs);
    List<ProcessOutputStream> outputs = new ArrayList<>();
    ProcessOutputStream pos = new ProcessOutputStream(new StreamKey("outputDomain","streamOutputName",1), mapper.createObjectNode());
    outputs.add(pos);
    p.setOutputs(outputs);
    p.setKey(new ProcessKey("domain","name"));
    List<ZoneKey> zones = new ArrayList<>();
    zones.add(new ZoneKey("aws_us_east_1"));
    p.setZones(zones);
    return p;
  }
}
