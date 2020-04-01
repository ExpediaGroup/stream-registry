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

import static com.expediagroup.streamplatform.streamregistry.data.ObjectNodeMapper.serialise;

import java.util.List;
import java.util.stream.Collectors;

import com.expediagroup.streamplatform.streamregistry.data.Consumer;
import com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.data.Domain;
import com.expediagroup.streamplatform.streamregistry.data.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.data.Producer;
import com.expediagroup.streamplatform.streamregistry.data.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.data.Schema;
import com.expediagroup.streamplatform.streamregistry.data.Specification;
import com.expediagroup.streamplatform.streamregistry.data.Status;
import com.expediagroup.streamplatform.streamregistry.data.Stream;
import com.expediagroup.streamplatform.streamregistry.data.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.data.Tag;
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

public class ModelToData {

  public static ConsumerKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey in) {
    return new ConsumerKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getZone(), in.getName());
  }

  public static Consumer convertToData(com.expediagroup.streamplatform.streamregistry.model.Consumer in) {
    return new Consumer(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static ConsumerBindingKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey in) {
    return new ConsumerBindingKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getInfrastructureZone(), in.getInfrastructureName(),
        in.getConsumerName());
  }

  public static ConsumerBinding convertToData(com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding in) {
    return new ConsumerBinding(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static DomainKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey in) {
    return new DomainKey(in.getName());
  }

  public static Domain convertToData(com.expediagroup.streamplatform.streamregistry.model.Domain in) {
    return new Domain(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static InfrastructureKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey in) {
    return new InfrastructureKey(in.getZone(), in.getName());
  }

  public static Infrastructure convertToData(com.expediagroup.streamplatform.streamregistry.model.Infrastructure in) {
    return new Infrastructure(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static ProducerKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey in) {
    return new ProducerKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getZone(), in.getName());
  }

  public static Producer convertToData(com.expediagroup.streamplatform.streamregistry.model.Producer in) {
    return new Producer(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static ProducerBindingKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey in) {
    return new ProducerBindingKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getInfrastructureZone(), in.getInfrastructureName(),
        in.getProducerName());
  }

  public static ProducerBinding convertToData(com.expediagroup.streamplatform.streamregistry.model.ProducerBinding in) {
    return new ProducerBinding(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static SchemaKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.SchemaKey in) {
    return new SchemaKey(in.getDomain(), in.getName());
  }

  public static Schema convertToData(com.expediagroup.streamplatform.streamregistry.model.Schema in) {
    return new Schema(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static StreamKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey in) {
    return new StreamKey(in.getDomain(), in.getName(), in.getVersion());
  }

  public static Stream convertToData(com.expediagroup.streamplatform.streamregistry.model.Stream in) {
    return new Stream(convertToData(in.getKey()), convertToData(in.getSchemaKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static StreamBindingKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey in) {
    return new StreamBindingKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getInfrastructureZone(), in.getInfrastructureName());
  }

  public static StreamBinding convertToData(com.expediagroup.streamplatform.streamregistry.model.StreamBinding in) {
    return new StreamBinding(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  public static ZoneKey convertToData(com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey in) {
    return new ZoneKey(in.getName());
  }

  public static Zone convertToData(com.expediagroup.streamplatform.streamregistry.model.Zone in) {
    return new Zone(convertToData(in.getKey()), convertToData(in.getSpecification()), convertToData(in.getStatus()));
  }

  private static Specification convertToData(com.expediagroup.streamplatform.streamregistry.model.Specification in) {
    return new Specification(in.getDescription(), convertToData(in.getTags()), in.getType(), serialise(in.getConfiguration()));
  }

  private static List<com.expediagroup.streamplatform.streamregistry.data.Tag> convertToData(
      List<com.expediagroup.streamplatform.streamregistry.model.Tag> in) {
    return in.stream().map(t -> new Tag(t.getId(), t.getName(), t.getValue())).collect(Collectors.toList());
  }

  private static Status convertToData(com.expediagroup.streamplatform.streamregistry.model.Status in) {
    return new Status(serialise(in.getObjectNode()));
  }
}
