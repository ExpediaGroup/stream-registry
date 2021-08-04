/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ConsumerBindingConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ConsumerConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.DomainConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.InfrastructureConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProcessBindingConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProcessConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProducerBindingConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProducerConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.SchemaConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.StreamBindingConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.StreamConverter;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ZoneConverter;

public class ConverterTest {

  private final DomainConverter domainConverter = new DomainConverter();
  private final SchemaConverter schemaConverter = new SchemaConverter(domainConverter);
  private final StreamConverter streamConverter = new StreamConverter(domainConverter, schemaConverter);
  private final ZoneConverter zoneConverter = new ZoneConverter();
  private final ProcessConverter processConverter = new ProcessConverter(domainConverter, zoneConverter, streamConverter);
  private final InfrastructureConverter infrastructureConverter = new InfrastructureConverter(zoneConverter);
  private final ProducerConverter producerConverter = new ProducerConverter(streamConverter, zoneConverter);
  private final ConsumerConverter consumerConverter = new ConsumerConverter(streamConverter, zoneConverter);
  private final StreamBindingConverter streamBindingConverter = new StreamBindingConverter(streamConverter, infrastructureConverter);
  private final ProducerBindingConverter producerBindingConverter = new ProducerBindingConverter(producerConverter, streamBindingConverter);
  private final ConsumerBindingConverter consumerBindingConverter = new ConsumerBindingConverter(consumerConverter, streamBindingConverter);
  private final ProcessBindingConverter processBindingConverter = new ProcessBindingConverter(domainConverter, zoneConverter, streamBindingConverter);

  @Test
  public void domain() {
    assertThat(domainConverter.convertEntity(SampleModel.domain()), is(SampleState.domain()));
    assertThat(domainConverter.convertEntity(SampleState.domain()), is(SampleModel.domain()));
  }

  @Test
  public void schema() {
    assertThat(schemaConverter.convertEntity(SampleModel.schema()), is(SampleState.schema()));
    assertThat(schemaConverter.convertEntity(SampleState.schema()), is(SampleModel.schema()));
  }

  @Test
  public void stream() {
    assertThat(streamConverter.convertEntity(SampleModel.stream()), is(SampleState.stream()));
    assertThat(streamConverter.convertEntity(SampleState.stream()), is(SampleModel.stream()));
  }

  @Test
  public void process() {
    assertThat(processConverter.convertEntity(SampleModel.process()), is(SampleState.process()));
    assertThat(processConverter.convertEntity(SampleState.process()), is(SampleModel.process()));
  }

  @Test
  public void zone() {
    assertThat(zoneConverter.convertEntity(SampleModel.zone()), is(SampleState.zone()));
    assertThat(zoneConverter.convertEntity(SampleState.zone()), is(SampleModel.zone()));
  }

  @Test
  public void infrastructure() {
    assertThat(infrastructureConverter.convertEntity(SampleModel.infrastructure()), is(SampleState.infrastructure()));
    assertThat(infrastructureConverter.convertEntity(SampleState.infrastructure()), is(SampleModel.infrastructure()));
  }

  @Test
  public void producer() {
    assertThat(producerConverter.convertEntity(SampleModel.producer()), is(SampleState.producer()));
    assertThat(producerConverter.convertEntity(SampleState.producer()), is(SampleModel.producer()));
  }

  @Test
  public void consumer() {
    assertThat(consumerConverter.convertEntity(SampleModel.consumer()), is(SampleState.consumer()));
    assertThat(consumerConverter.convertEntity(SampleState.consumer()), is(SampleModel.consumer()));
  }

  @Test
  public void streamBinding() {
    assertThat(streamBindingConverter.convertEntity(SampleModel.streamBinding()), is(SampleState.streamBinding()));
    assertThat(streamBindingConverter.convertEntity(SampleState.streamBinding()), is(SampleModel.streamBinding()));
  }

  @Test
  public void producerBinding() {
    assertThat(producerBindingConverter.convertEntity(SampleModel.producerBinding()), is(SampleState.producerBinding()));
    assertThat(producerBindingConverter.convertEntity(SampleState.producerBinding()), is(SampleModel.producerBinding()));
  }

  @Test
  public void consumerBinding() {
    assertThat(consumerBindingConverter.convertEntity(SampleModel.consumerBinding()), is(SampleState.consumerBinding()));
    assertThat(consumerBindingConverter.convertEntity(SampleState.consumerBinding()), is(SampleModel.consumerBinding()));
  }

  @Test
  public void processBinding() {
    assertThat(processBindingConverter.convertEntity(SampleModel.processBinding()), is(SampleState.processBinding()));
    assertThat(processBindingConverter.convertEntity(SampleState.processBinding()), is(SampleModel.processBinding()));
  }
}
