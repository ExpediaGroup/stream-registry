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

import com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper;
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

public class DataToModel {

  private static Transformer transformer = new BeanUtils()
      .getTransformer()
      .setFlatFieldNameTransformation(true)
      .withFieldMapping(new FieldMapping("configJson", "configuration"))
      .withFieldTransformer(new FieldTransformer<>("configuration", ObjectNodeMapper::deserialise))
      .withFieldMapping(new FieldMapping("statusJson", "objectNode"))
      .withFieldTransformer(new FieldTransformer<>("objectNode", ObjectNodeMapper::deserialise))
      ;

  public static Object convert(Object in,Class clazz) {
    if (in == null) {
      return null;
    }
    return transformer.transform(in, clazz);
  }

  public static ConsumerKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerKey in) {
    return (ConsumerKey) convert(in,ConsumerKey.class);
  }

  public static Consumer convertToModel(com.expediagroup.streamplatform.streamregistry.data.Consumer in) {
    return (Consumer) convert(in,Consumer.class);
  }

  public static ConsumerBindingKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerBindingKey in) {
    return (ConsumerBindingKey) convert(in,ConsumerBindingKey.class);
  }

  public static ConsumerBinding convertToModel(com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding in) {
    return (ConsumerBinding) convert(in,ConsumerBinding.class);
  }

  public static DomainKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.DomainKey in) {
    return (DomainKey) convert(in,DomainKey.class);
  }

  public static Domain convertToModel(com.expediagroup.streamplatform.streamregistry.data.Domain in) {
    return (Domain) convert(in,Domain.class);
  }

  public static InfrastructureKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.InfrastructureKey in) {
    return (InfrastructureKey) convert(in,InfrastructureKey.class);
  }

  public static Infrastructure convertToModel(com.expediagroup.streamplatform.streamregistry.data.Infrastructure in) {
    return (Infrastructure) convert(in,Infrastructure.class);
  }

  public static ProducerKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ProducerKey in) {
    return (ProducerKey) convert(in,ProducerKey.class);
  }

  public static Producer convertToModel(com.expediagroup.streamplatform.streamregistry.data.Producer in) {
    return (Producer) convert(in,Producer.class);
  }

  public static ProducerBindingKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ProducerBindingKey in) {
    return (ProducerBindingKey) convert(in,ProducerBindingKey.class);

  }

  public static ProducerBinding convertToModel(com.expediagroup.streamplatform.streamregistry.data.ProducerBinding in) {
    return (ProducerBinding) convert(in,ProducerBinding.class);
  }

  public static SchemaKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.SchemaKey in) {
    return (SchemaKey) convert(in,SchemaKey.class);
  }

  public static Schema convertToModel(com.expediagroup.streamplatform.streamregistry.data.Schema in) {
    return (Schema) convert(in,Schema.class);
  }

  public static StreamKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.StreamKey in) {
    return (StreamKey) convert(in,StreamKey.class);
  }

  public static Stream convertToModel(com.expediagroup.streamplatform.streamregistry.data.Stream in) {
    return (Stream) convert(in,Stream.class);
  }

  public static StreamBindingKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.StreamBindingKey in) {
    return (StreamBindingKey) convert(in,StreamBindingKey.class);
  }

  public static StreamBinding convertToModel(com.expediagroup.streamplatform.streamregistry.data.StreamBinding in) {
    return (StreamBinding) convert(in,StreamBinding.class);
  }

  public static ZoneKey convertToModel(com.expediagroup.streamplatform.streamregistry.data.keys.ZoneKey in) {
    return (ZoneKey) convert(in,ZoneKey.class);
  }

  public static Zone convertToModel(com.expediagroup.streamplatform.streamregistry.data.Zone in) {
    return (Zone) convert(in,Zone.class);
  }

}
