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
package com.expediagroup.streamplatform.streamregistry;

import com.hotels.beans.BeanUtils;
import com.hotels.beans.model.FieldMapping;
import com.hotels.beans.model.FieldTransformer;
import com.hotels.beans.transformer.Transformer;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.data.Consumer;
import com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.data.Domain;
import com.expediagroup.streamplatform.streamregistry.data.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper;
import com.expediagroup.streamplatform.streamregistry.data.Producer;
import com.expediagroup.streamplatform.streamregistry.data.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.data.Schema;
import com.expediagroup.streamplatform.streamregistry.data.Specification;
import com.expediagroup.streamplatform.streamregistry.data.Stream;
import com.expediagroup.streamplatform.streamregistry.data.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.data.Zone;
import com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.data.keys.ZoneKey;

@Component
public class ModelToData {

  private static Transformer transformer = new BeanUtils()
      .getTransformer()
      .setFlatFieldNameTransformation(true)
      .withFieldMapping(new FieldMapping("configuration", "configJson"))
      .withFieldTransformer(new FieldTransformer<>("configJson", ObjectNodeMapper::serialise))

      .withFieldMapping(new FieldMapping("objectNode", "statusJson"))
      .withFieldTransformer(new FieldTransformer<>("statusJson", ObjectNodeMapper::serialise))
      ;

  public Object convert(Object in,Class clazz) {
    if (in == null) {
      return null;
    }
    return transformer.transform(in, clazz);
  }

  public ConsumerKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey in) {
    return (ConsumerKey) convert(in,ConsumerKey.class);
  }

  public Consumer convertToData(com.expediagroup.streamplatform.streamregistry.model.Consumer in) {
    return (Consumer) convert(in, Consumer.class);
  }

  public ConsumerBindingKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey in) {
    return (ConsumerBindingKey) convert(in, ConsumerBindingKey.class);
  }

  public ConsumerBinding convertToData(com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding in) {
    return (ConsumerBinding) convert(in, ConsumerBinding.class);
  }

  public DomainKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey in) {
    return (DomainKey) convert(in, DomainKey.class);
  }

  public Domain convertToData(com.expediagroup.streamplatform.streamregistry.model.Domain in) {
    return (Domain) convert(in, Domain.class);
  }

  public InfrastructureKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey in) {
    return (InfrastructureKey) convert(in, InfrastructureKey.class);
  }

  public Infrastructure convertToData(com.expediagroup.streamplatform.streamregistry.model.Infrastructure in) {
    return (Infrastructure) convert(in, Infrastructure.class);
  }

  public ProducerKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey in) {
    return (ProducerKey) convert(in, ProducerKey.class);
  }

  public Producer convertToData(com.expediagroup.streamplatform.streamregistry.model.Producer in) {
    return (Producer) convert(in, Producer.class);
  }

  public ProducerBindingKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey in) {
    return (ProducerBindingKey) convert(in, ProducerBindingKey.class);
  }

  public ProducerBinding convertToData(com.expediagroup.streamplatform.streamregistry.model.ProducerBinding in) {
    return (ProducerBinding) convert(in, ProducerBinding.class);
  }

  public SchemaKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey in) {
    return (SchemaKey) convert(in, SchemaKey.class);
  }

  public Schema convertToData(com.expediagroup.streamplatform.streamregistry.model.Schema in) {
    return (Schema) convert(in, Schema.class);
  }

  public StreamKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey in) {
    return (StreamKey) convert(in, StreamKey.class);
  }

  public Stream convertToData(com.expediagroup.streamplatform.streamregistry.model.Stream in) {
    return (Stream) convert(in, Stream.class);
  }

  public StreamBindingKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey in) {
    return (StreamBindingKey) convert(in, StreamBindingKey.class);
  }

  public StreamBinding convertToData(com.expediagroup.streamplatform.streamregistry.model.StreamBinding in) {
    return (StreamBinding) convert(in, StreamBinding.class);
  }

  public ZoneKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey in) {
    return (ZoneKey) convert(in, ZoneKey.class);
  }

  public Zone convertToData(com.expediagroup.streamplatform.streamregistry.model.Zone in) {
    return (Zone) convert(in, Zone.class);
  }

  public Specification convertToData(com.expediagroup.streamplatform.streamregistry.model.Specification in) {
    return (Specification) convert(in, Specification.class);
  }

}
