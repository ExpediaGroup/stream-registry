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
package com.expediagroup.streamplatform.streamregistry.repository.postgres.data;

import com.hotels.beans.BeanUtils;
import com.hotels.beans.model.FieldMapping;
import com.hotels.beans.model.FieldTransformer;
import com.hotels.beans.transformer.Transformer;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.ConsumerBindingDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.ConsumerDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.DomainDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.InfrastructureDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.ProducerBindingDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.ProducerDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.SchemaDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.StreamBindingDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.StreamDataKey;
import com.expediagroup.streamplatform.streamregistry.repository.postgres.data.keys.ZoneDataKey;

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

  public ConsumerDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey in) {
    return (ConsumerDataKey) convert(in, ConsumerDataKey.class);
  }

  public ConsumerData convertToData(com.expediagroup.streamplatform.streamregistry.model.Consumer in) {
    return (ConsumerData) convert(in, ConsumerData.class);
  }

  public ConsumerBindingDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey in) {
    return (ConsumerBindingDataKey) convert(in, ConsumerBindingDataKey.class);
  }

  public ConsumerBindingData convertToData(com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding in) {
    return (ConsumerBindingData) convert(in, ConsumerBindingData.class);
  }

  public DomainDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey in) {
    return (DomainDataKey) convert(in, DomainDataKey.class);
  }

  public DomainData convertToData(com.expediagroup.streamplatform.streamregistry.model.Domain in) {
    return (DomainData) convert(in, DomainData.class);
  }

  public InfrastructureDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey in) {
    return (InfrastructureDataKey) convert(in, InfrastructureDataKey.class);
  }

  public InfrastructureData convertToData(com.expediagroup.streamplatform.streamregistry.model.Infrastructure in) {
    return (InfrastructureData) convert(in, InfrastructureData.class);
  }

  public ProducerDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey in) {
    return (ProducerDataKey) convert(in, ProducerDataKey.class);
  }

  public ProducerData convertToData(com.expediagroup.streamplatform.streamregistry.model.Producer in) {
    return (ProducerData) convert(in, ProducerData.class);
  }

  public ProducerBindingDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey in) {
    return (ProducerBindingDataKey) convert(in, ProducerBindingDataKey.class);
  }

  public ProducerBindingData convertToData(com.expediagroup.streamplatform.streamregistry.model.ProducerBinding in) {
    return (ProducerBindingData) convert(in, ProducerBindingData.class);
  }

  public SchemaDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey in) {
    return (SchemaDataKey) convert(in, SchemaDataKey.class);
  }

  public SchemaData convertToData(com.expediagroup.streamplatform.streamregistry.model.Schema in) {
    return (SchemaData) convert(in, SchemaData.class);
  }

  public StreamDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey in) {
    return (StreamDataKey) convert(in, StreamDataKey.class);
  }

  public StreamData convertToData(com.expediagroup.streamplatform.streamregistry.model.Stream in) {
    return (StreamData) convert(in, StreamData.class);
  }

  public StreamBindingDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey in) {
    return (StreamBindingDataKey) convert(in, StreamBindingDataKey.class);
  }

  public StreamBindingData convertToData(com.expediagroup.streamplatform.streamregistry.model.StreamBinding in) {
    return (StreamBindingData) convert(in, StreamBindingData.class);
  }

  public ZoneDataKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey in) {
    return (ZoneDataKey) convert(in, ZoneDataKey.class);
  }

  public ZoneData convertToData(com.expediagroup.streamplatform.streamregistry.model.Zone in) {
    return (ZoneData) convert(in, ZoneData.class);
  }

  public SpecificationData convertToData(com.expediagroup.streamplatform.streamregistry.model.Specification in) {
    return (SpecificationData) convert(in, SpecificationData.class);
  }

}
