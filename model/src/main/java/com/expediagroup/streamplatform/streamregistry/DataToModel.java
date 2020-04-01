/**
 * Copyright (C) 2018-2020 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry;

import java.util.List;
import java.util.stream.Collectors;

import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Tag;
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

  public static ConsumerKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerKey in) {
    return new ConsumerKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getZone(), in.getName());
  }

  public static Consumer convert(com.expediagroup.streamplatform.streamregistry.data.Consumer in) {
    return new Consumer(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static ConsumerBindingKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.ConsumerBindingKey in) {
    return new ConsumerBindingKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getInfrastructureZone(), in.getInfrastructureName(), in.getConsumerName());
  }

  public static ConsumerBinding convert(com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding in) {
    return new ConsumerBinding(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static DomainKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.DomainKey in) {
    return new DomainKey(in.getName());
  }

  public static Domain convert(com.expediagroup.streamplatform.streamregistry.data.Domain in) {
    return new Domain(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static InfrastructureKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.InfrastructureKey in) {
    return new InfrastructureKey(in.getZone(), in.getName());
  }

  public static Infrastructure convert(com.expediagroup.streamplatform.streamregistry.data.Infrastructure in) {
    return new Infrastructure(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static ProducerKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.ProducerKey in) {
    return new ProducerKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getZone(), in.getName());
  }

  public static Producer convert(com.expediagroup.streamplatform.streamregistry.data.Producer in) {
    return new Producer(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static ProducerBindingKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.ProducerBindingKey in) {
    return new ProducerBindingKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getInfrastructureZone(), in.getInfrastructureName(), in.getProducerName());
  }

  public static ProducerBinding convert(com.expediagroup.streamplatform.streamregistry.data.ProducerBinding in) {
    return new ProducerBinding(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static SchemaKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.SchemaKey in) {
    return new SchemaKey(in.getDomain(), in.getName());
  }

  public static Schema convert(com.expediagroup.streamplatform.streamregistry.data.Schema in) {
    return new Schema(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static StreamKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.StreamKey in) {
    return new StreamKey(in.getDomain(), in.getName(), in.getVersion());
  }

  public static Stream convert(com.expediagroup.streamplatform.streamregistry.data.Stream in) {
    return new Stream(convert(in.getKey()), convert(in.getSchemaKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static StreamBindingKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.StreamBindingKey in) {
    return new StreamBindingKey(in.getStreamDomain(), in.getStreamName(), in.getStreamVersion(), in.getInfrastructureZone(), in.getInfrastructureName());
  }

  public static StreamBinding convert(com.expediagroup.streamplatform.streamregistry.data.StreamBinding in) {
    return new StreamBinding(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  public static ZoneKey convert(com.expediagroup.streamplatform.streamregistry.data.keys.ZoneKey in) {
    return new ZoneKey(in.getName());
  }

  public static Zone convert(com.expediagroup.streamplatform.streamregistry.data.Zone in) {
    return new Zone(convert(in.getKey()), convert(in.getSpecification()), convert(in.getStatus()));
  }

  private static Specification convert(com.expediagroup.streamplatform.streamregistry.data.Specification in) {
    return new Specification(in.getDescription(), convert(in.getTags()), in.getType(), in.getConfiguration());
  }

  private static List<Tag> convert(List<com.expediagroup.streamplatform.streamregistry.data.Tag> in) {
    return in.stream().map(t -> new Tag(t.getId(), t.getName(), t.getValue())).collect(Collectors.toList());
  }

  private static Status convert(com.expediagroup.streamplatform.streamregistry.data.Status in) {
    return new Status();
  }
}
