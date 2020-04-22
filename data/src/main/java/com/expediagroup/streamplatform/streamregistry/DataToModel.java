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

import com.expediagroup.streamplatform.streamregistry.data.ConsumerBindingData;
import com.expediagroup.streamplatform.streamregistry.data.ConsumerData;
import com.expediagroup.streamplatform.streamregistry.data.DomainData;
import com.expediagroup.streamplatform.streamregistry.data.InfrastructureData;
import com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper;
import com.expediagroup.streamplatform.streamregistry.data.ProducerBindingData;
import com.expediagroup.streamplatform.streamregistry.data.ProducerData;
import com.expediagroup.streamplatform.streamregistry.data.SchemaData;
import com.expediagroup.streamplatform.streamregistry.data.StreamBindingData;
import com.expediagroup.streamplatform.streamregistry.data.StreamData;
import com.expediagroup.streamplatform.streamregistry.data.ZoneData;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;

@Component
public class DataToModel {

  private static Transformer transformer = new BeanUtils()
      .getTransformer()
      .setFlatFieldNameTransformation(true)
      .withFieldMapping(new FieldMapping("configJson", "configuration"))
      .withFieldTransformer(new FieldTransformer<>("configuration", ObjectNodeMapper::deserialise))
      .withFieldMapping(new FieldMapping("statusJson", "objectNode"))
      .withFieldTransformer(new FieldTransformer<>("objectNode", ObjectNodeMapper::deserialise))
      ;

  public Object convert(Object in,Class clazz) {
    if (in == null) {
      return null;
    }
    return transformer.transform(in, clazz);
  }

  public ConsumerKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerKey in) {
    return (ConsumerKey) convert(in,ConsumerKey.class);
  }

  public Consumer convertToModel(ConsumerData in) {
    return (Consumer) convert(in,Consumer.class);
  }

  public ConsumerBindingKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerBindingKey in) {
    return (ConsumerBindingKey) convert(in,ConsumerBindingKey.class);
  }

  public ConsumerBinding convertToModel(ConsumerBindingData in) {
    return (ConsumerBinding) convert(in,ConsumerBinding.class);
  }

  public DomainKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.DomainKey in) {
    return (DomainKey) convert(in,DomainKey.class);
  }

  public Domain convertToModel(DomainData in) {
    return (Domain) convert(in,Domain.class);
  }

  public InfrastructureKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.InfrastructureKey in) {
    return (InfrastructureKey) convert(in,InfrastructureKey.class);
  }

  public Infrastructure convertToModel(InfrastructureData in) {
    return (Infrastructure) convert(in,Infrastructure.class);
  }

  public ProducerKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ProducerKey in) {
    return (ProducerKey) convert(in,ProducerKey.class);
  }

  public Producer convertToModel(ProducerData in) {
    return (Producer) convert(in,Producer.class);
  }

  public ProducerBindingKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ProducerBindingKey in) {
    return (ProducerBindingKey) convert(in,ProducerBindingKey.class);

  }

  public ProducerBinding convertToModel(ProducerBindingData in) {
    return (ProducerBinding) convert(in,ProducerBinding.class);
  }

  public SchemaKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.SchemaKey in) {
    return (SchemaKey) convert(in,SchemaKey.class);
  }

  public Schema convertToModel(SchemaData in) {
    return (Schema) convert(in,Schema.class);
  }

  public StreamKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.StreamKey in) {
    return (StreamKey) convert(in,StreamKey.class);
  }

  public Stream convertToModel(StreamData in) {
    return (Stream) convert(in,Stream.class);
  }

  public StreamBindingKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.StreamBindingKey in) {
    return (StreamBindingKey) convert(in,StreamBindingKey.class);
  }

  public StreamBinding convertToModel(StreamBindingData in) {
    return (StreamBinding) convert(in,StreamBinding.class);
  }

  public ZoneKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ZoneKey in) {
    return (ZoneKey) convert(in,ZoneKey.class);
  }

  public Zone convertToModel(ZoneData in) {
    return (Zone) convert(in,Zone.class);
  }

}
