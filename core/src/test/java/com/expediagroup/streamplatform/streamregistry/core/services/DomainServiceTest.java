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
package com.expediagroup.streamplatform.streamregistry.core.services;


import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.DomainValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.DomainRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;
import com.expediagroup.streamplatform.streamregistry.repository.SchemaRepository;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

@RunWith(MockitoJUnitRunner.class)
public class DomainServiceTest {

  @Mock
  private HandlerService handlerService;

  @Mock
  private DomainValidator domainValidator;

  @Mock
  private DomainRepository domainRepository;

  private DomainService domainService;

  @Mock
  private SchemaRepository schemaRepository;

  @Mock
  private ProducerRepository producerRepository;

  @Mock
  private ConsumerRepository consumerRepository;

  @Mock
  private ProcessRepository processRepository;

  @Mock
  private StreamRepository streamRepository;

  @Before
  public void before() {
    domainService = new DomainService(
      new DomainView(domainRepository),
      handlerService,
      domainValidator,
      domainRepository,
      new StreamView(streamRepository),
      new ProducerView(producerRepository),
      new ConsumerView(consumerRepository),
      new SchemaView(schemaRepository),
      new ProcessView(processRepository)
    );
  }

  @Test
  public void create() {
    final Domain entity = mock(Domain.class);
    final DomainKey key = mock(DomainKey.class);
    final Specification specification = mock(Specification.class);

    Mockito.when(domainRepository.findById(key)).thenReturn(Optional.empty());
    Mockito.doNothing().when(domainValidator).validateForCreate(entity);
    Mockito.when(handlerService.handleInsert(entity)).thenReturn(specification);

    Mockito.when(domainRepository.save(any())).thenReturn(entity);
    when(entity.getKey()).thenReturn(key);

    domainService.create(entity);

    verify(entity).getKey();
    verify(domainRepository).findById(key);
    verify(domainValidator).validateForCreate(entity);
    verify(handlerService).handleInsert(entity);
    verify(domainRepository).save(entity);
  }

  @Test
  public void update() {
    final Domain entity = mock(Domain.class);
    final DomainKey key = mock(DomainKey.class);
    final Domain existingEntity = mock(Domain.class);
    final Specification specification = mock(Specification.class);

    when(entity.getKey()).thenReturn(key);

    when(domainRepository.findById(key)).thenReturn(Optional.of(existingEntity));
    doNothing().when(domainValidator).validateForUpdate(entity, existingEntity);
    when(handlerService.handleUpdate(entity, existingEntity)).thenReturn(specification);

    when(domainRepository.save(entity)).thenReturn(entity);

    domainService.update(entity);

    verify(entity).getKey();
    verify(domainRepository).findById(key);
    verify(domainValidator).validateForUpdate(entity, existingEntity);
    verify(handlerService).handleUpdate(entity, existingEntity);
    verify(domainRepository).save(entity);
  }

  @Test
  public void updateStatus() {
    final Domain entity = mock(Domain.class);
    final Status status = mock(Status.class);

    when(domainRepository.save(entity)).thenReturn(entity);

    domainService.updateStatus(entity, status);

    verify(domainRepository).save(entity);
  }

  @Test
  public void deleteWithNoError() {
    final Domain entity = mock(Domain.class);

    when(producerRepository.findAll()).thenReturn(emptyList());
    when(consumerRepository.findAll()).thenReturn(emptyList());
    when(schemaRepository.findAll()).thenReturn(emptyList());
    when(streamRepository.findAll()).thenReturn(emptyList());
    when(processRepository.findAll()).thenReturn(emptyList());

    domainService.delete(entity);
    verify(domainRepository).delete(entity);
  }

  @Test
  public void deleteDomainUsedInProcess() {

    final DomainKey key = mock(DomainKey.class);
    final Domain entity = mock(Domain.class);
    when(entity.getKey()).thenReturn(key);

    final ProcessKey processKey = mock(ProcessKey.class);
    final Process process = mock(Process.class);
    when(processKey.getDomainKey()).thenReturn(key);
    when(process.getKey()).thenReturn(processKey);
    when(processRepository.findAll()).thenReturn(asList(process));
    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      domainService.delete(entity);
    });
    Assertions.assertEquals(ex.getMessage(), "Domain used in process");
  }

  @Test
  public void deleteDomainUsedInStream() {
    final DomainKey key = mock(DomainKey.class);
    final Domain entity = mock(Domain.class);
    when(entity.getKey()).thenReturn(key);

    final StreamKey streamkey = mock(StreamKey.class);
    final Stream stream = mock(Stream.class);
    when(streamkey.getDomainKey()).thenReturn(key);
    when(stream.getKey()).thenReturn(streamkey);

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamRepository.findAll()).thenReturn(asList(stream));
    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      domainService.delete(entity);
    });
    Assertions.assertEquals(ex.getMessage(), "Domain used in stream");
  }

  @Test
  public void deleteDomainUsedInSchema() {
    final DomainKey key = mock(DomainKey.class);
    final Domain entity = mock(Domain.class);
    when(entity.getKey()).thenReturn(key);

    final SchemaKey schemaKey = mock(SchemaKey.class);
    final Schema schema = mock(Schema.class);
    when(schemaKey.getDomainKey()).thenReturn(key);
    when(schema.getKey()).thenReturn(schemaKey);

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamRepository.findAll()).thenReturn(emptyList());
    when(schemaRepository.findAll()).thenReturn(asList(schema));
    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      domainService.delete(entity);
    });
    Assertions.assertEquals(ex.getMessage(), "Domain used in schema");
  }

  @Test
  public void deleteDomainUsedInConsumer() {
    final DomainKey key = mock(DomainKey.class);
    final Domain entity = mock(Domain.class);
    final StreamKey streamkey = mock(StreamKey.class);
    when(entity.getKey()).thenReturn(key);

    final ConsumerKey consumerKey = mock(ConsumerKey.class);
    final Consumer consumer = mock(Consumer.class);
    when(consumerKey.getStreamKey()).thenReturn(streamkey);
    when(consumerKey.getStreamKey().getDomainKey()).thenReturn(key);
    when(consumer.getKey()).thenReturn(consumerKey);

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamRepository.findAll()).thenReturn(emptyList());
    when(producerRepository.findAll()).thenReturn(emptyList());
    when(consumerRepository.findAll()).thenReturn(asList(consumer));
    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      domainService.delete(entity);
    });
    Assertions.assertEquals(ex.getMessage(), "Domain used in consumer");
  }

  @Test
  public void deleteDomainUsedInProducer() {
    final DomainKey key = mock(DomainKey.class);
    final Domain entity = mock(Domain.class);
    final StreamKey streamkey = mock(StreamKey.class);
    when(entity.getKey()).thenReturn(key);

    final ProducerKey producerKey = mock(ProducerKey.class);
    final Producer producer = mock(Producer.class);
    when(producerKey.getStreamKey()).thenReturn(streamkey);
    when(producerKey.getStreamKey().getDomainKey()).thenReturn(key);
    when(producer.getKey()).thenReturn(producerKey);

    when(processRepository.findAll()).thenReturn(emptyList());
    when(streamRepository.findAll()).thenReturn(emptyList());
    when(producerRepository.findAll()).thenReturn(asList(producer));
    IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      domainService.delete(entity);
    });
    Assertions.assertEquals(ex.getMessage(), "Domain used in producer");
  }
}
