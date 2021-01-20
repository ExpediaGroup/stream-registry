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
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import lombok.val;

import com.expediagroup.streamplatform.streamregistry.cli.graphql.ConsumerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ConsumerQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.InfrastructureQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ProducerBindingQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ProducerQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.SchemaQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.StreamBindingQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.StreamQuery;
import com.expediagroup.streamplatform.streamregistry.cli.graphql.ZoneQuery;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.ZoneKey;

class KeyConverter {
  DomainKey domainKey(DomainQuery.Key key) {
    return new DomainKey(key.getName());
  }

  SchemaKey schemaKey(SchemaQuery.Key key) {
    val domainKey = new DomainKey(key.getDomain());
    return new SchemaKey(domainKey, key.getName());
  }

  SchemaKey schemaKey(StreamQuery.Key1 key) {
    val domainKey = new DomainKey(key.getDomain());
    return new SchemaKey(domainKey, key.getName());
  }

  ZoneKey zoneKey(ZoneQuery.Key key) {
    return new ZoneKey(key.getName());
  }

  InfrastructureKey infrastructureKey(InfrastructureQuery.Key key) {
    val zoneKey = new ZoneKey(key.getZone());
    return new InfrastructureKey(zoneKey, key.getName());
  }

  StreamKey streamKey(StreamQuery.Key key) {
    val domainKey = new DomainKey(key.getDomain());
    return new StreamKey(domainKey, key.getName(), key.getVersion());
  }

  ProducerKey producerKey(ProducerQuery.Key key) {
    val domainKey = new DomainKey(key.getStreamDomain());
    val streamKey = new StreamKey(domainKey, key.getStreamName(), key.getStreamVersion());
    val zoneKey = new ZoneKey(key.getZone());
    return new ProducerKey(streamKey, zoneKey, key.getName());
  }

  ConsumerKey consumerKey(ConsumerQuery.Key key) {
    val domainKey = new DomainKey(key.getStreamDomain());
    val streamKey = new StreamKey(domainKey, key.getStreamName(), key.getStreamVersion());
    val zoneKey = new ZoneKey(key.getZone());
    return new ConsumerKey(streamKey, zoneKey, key.getName());
  }

  ProducerBindingKey producerBindingKey(ProducerBindingQuery.Key key) {
    val domainKey = new DomainKey(key.getStreamDomain());
    val streamKey = new StreamKey(domainKey, key.getStreamName(), key.getStreamVersion());
    val zoneKey = new ZoneKey(key.getInfrastructureZone());
    val producerKey = new ProducerKey(streamKey, zoneKey, key.getProducerName());
    val infrastructureKey = new InfrastructureKey(zoneKey, key.getInfrastructureName());
    val streamBindingKey = new StreamBindingKey(streamKey, infrastructureKey);
    return new ProducerBindingKey(producerKey, streamBindingKey);
  }

  ConsumerBindingKey consumerBindingKey(ConsumerBindingQuery.Key key) {
    val domainKey = new DomainKey(key.getStreamDomain());
    val streamKey = new StreamKey(domainKey, key.getStreamName(), key.getStreamVersion());
    val zoneKey = new ZoneKey(key.getInfrastructureZone());
    val consumerKey = new ConsumerKey(streamKey, zoneKey, key.getConsumerName());
    val infrastructureKey = new InfrastructureKey(zoneKey, key.getInfrastructureName());
    val streamBindingKey = new StreamBindingKey(streamKey, infrastructureKey);
    return new ConsumerBindingKey(consumerKey, streamBindingKey);
  }

  StreamBindingKey streamBindingKey(StreamBindingQuery.Key key) {
    val domainKey = new DomainKey(key.getStreamDomain());
    val streamKey = new StreamKey(domainKey, key.getStreamName(), key.getStreamVersion());
    val zoneKey = new ZoneKey(key.getInfrastructureZone());
    val infrastructureKey = new InfrastructureKey(zoneKey, key.getInfrastructureName());
    return new StreamBindingKey(streamKey, infrastructureKey);
  }
}
