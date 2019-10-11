/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateDomainStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateSchemaStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;

public class ITestDataFactory {

  private static AtomicInteger count = new AtomicInteger(0);

  private static final ObjectMapper mapper = new ObjectMapper();

  public ITestDataFactory() {
    count.incrementAndGet();
    zoneName = "zoneName" + count;
    domainName = "domainName" + count;
    consumerName = "consumerName" + count;
    streamName = "streamName" + count;

    infrastructureName = "infrastructureName" + count;
    producerName = "producerName" + count;

    key = "key" + count;
    value = "value" + count;
    description = "description" + count;
  }

  public String zoneName;
  public String domainName;
  public String consumerName;
  public String streamName;
  private String infrastructureName;
  private String producerName;

  public String key;
  public String value;
  public String description;

  private DomainKeyInput.Builder domainKeyInputBuilder;

  public final DomainKeyInput.Builder domainKeyInputBuilder() {
    if (domainKeyInputBuilder == null) {
      domainKeyInputBuilder = DomainKeyInput.builder().name(domainName);
    }
    return domainKeyInputBuilder;
  }

  public final ConsumerKeyInput.Builder consumerKeyInputBuilder() {
    return ConsumerKeyInput.builder()
        .name(consumerName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1)
        .zone(zoneName);
  }

  private SpecificationInput.Builder specificationInputBuilder;

  public SpecificationInput.Builder specificationInputBuilder() {
    if (specificationInputBuilder == null) {
      specificationInputBuilder = SpecificationInput.builder()
          .configuration(mapper.createObjectNode().put(key, value))
          .description(description)
          .tags(Collections.emptyList())
          .type("default");
    }
    return specificationInputBuilder;
  }

  public UpsertDomainMutation.Builder upsertDomainMutationBuilder() {
    return UpsertDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public UpsertConsumerMutation.Builder upsertConsumerMutationBuilder() {
    return UpsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public InsertConsumerMutation.Builder insertConsumerMutationBuilder() {
    return InsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public UpsertZoneMutation.Builder upsertZoneMutationBuilder() {
    return UpsertZoneMutation.builder()
        .key(ZoneKeyInputBuilder().build())
        .specification(specificationInputBuilder().build())
        ;
  }

  private ZoneKeyInput.Builder ZoneKeyInputBuilder;

  private ZoneKeyInput.Builder ZoneKeyInputBuilder() {
    if (ZoneKeyInputBuilder == null) {
      ZoneKeyInputBuilder = ZoneKeyInput.builder().name(zoneName);
    }
    return ZoneKeyInputBuilder;
  }

  public UpdateConsumerMutation.Builder updateConsumerMutationBuilder() {
    return UpdateConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public UpdateConsumerStatusMutation.Builder updateConsumerStatusBuilder() {
    return UpdateConsumerStatusMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .status(StatusInput.builder().agentStatus(
            mapper.createObjectNode().put("skey", "svalue")
        ).build());
  }

  public UpsertConsumerBindingMutation.Builder upsertConsumerBindingMutationBuilder() {
    return UpsertConsumerBindingMutation.builder()
        .key(consumerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  private ConsumerBindingKeyInput.Builder consumerBindingKeyInputBuilder() {
    return ConsumerBindingKeyInput.builder()
        .consumerName(consumerName)
        .infrastructureName(infrastructureName)
        .infrastructureZone(zoneName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1);
  }

  public StatusInput statusInput() {
    return StatusInput.builder().agentStatus(mapper.createObjectNode().put("skey", "svalue")).build();
  }

  public UpdateConsumerBindingStatusMutation.Builder updateConsumerBindingStatusBuilder() {
    return UpdateConsumerBindingStatusMutation.builder()
        .key(consumerBindingKeyInputBuilder().build())
        .status(statusInput());
  }

  public UpsertInfrastructureMutation.Builder upsertInfrastructureMutationBuilder() {
    return UpsertInfrastructureMutation.builder()
        .key(infrastructureKey())
        .specification(specificationInputBuilder().build());
  }

  private InfrastructureKeyInput infrastructureKey() {
    return InfrastructureKeyInput.builder()
        .name(infrastructureName)
        .zone(zoneName)
        .build();
  }

  public UpsertProducerBindingMutation.Builder upsertProducerBindingMutationBuilder() {
    return UpsertProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  private ProducerBindingKeyInput.Builder producerBindingKeyInputBuilder() {
    return ProducerBindingKeyInput.builder()
        .infrastructureName(infrastructureName)
        .infrastructureZone(zoneName)
        .producerName(producerName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1);
  }

  public UpsertProducerMutation.Builder upsertProducerMutationBuilder() {
    return UpsertProducerMutation.builder()
        .key(producerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  private ProducerKeyInput.Builder producerKeyInputBuilder() {
    return ProducerKeyInput.builder()
        .name(producerName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1)
        .zone(zoneName);
  }

  public UpsertStreamMutation.Builder upsertStreamMutationBuilder() {
    return UpsertStreamMutation.builder()
        .specification(specificationInputBuilder().build())
        .schema(schemaKeyInputBuilder().build())
        .key(streamKeyInputBuilder().build());
  }

  private StreamKeyInput.Builder streamKeyInputBuilder() {
    return StreamKeyInput.builder()
        .domain(domainName)
        .name(streamName)
        .version(1);
  }

  public UpsertSchemaMutation.Builder upsertSchemaMutationBuilder() {
    return UpsertSchemaMutation.builder()
        .specification(specificationInputBuilder().build())
        .key(schemaKeyInputBuilder().build()
        );
  }

  private SchemaKeyInput.Builder schemaKeyInputBuilder() {
    return SchemaKeyInput.builder()
        .domain(domainName)
        .name(streamName);
  }

  public UpsertStreamBindingMutation.Builder upsertStreamBindingMutationBuilder() {
    return UpsertStreamBindingMutation.builder()
        .specification(specificationInputBuilder().build())
        .key(streamBindingKeyInputBuilder().build()
        );
  }

  private StreamBindingKeyInput.Builder streamBindingKeyInputBuilder() {
    return StreamBindingKeyInput.builder()
        .infrastructureName(infrastructureName)
        .infrastructureZone(zoneName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1);
  }

  public UpdateProducerMutation.Builder updateProducerMutationBuilder() {
    return UpdateProducerMutation.builder()
        .key(producerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public InsertProducerMutation.Builder insertProducerMutationBuilder() {
    return InsertProducerMutation.builder()
        .key(producerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  public UpdateDomainStatusMutation.Builder updateDomainStatusMutation() {
    return UpdateDomainStatusMutation.builder()
        .key(domainKeyInputBuilder().build())
        .status(statusInput());
  }

  public UpdateInfrastructureStatusMutation.Builder updateInfrastructureStatusBuilder() {
    return UpdateInfrastructureStatusMutation.builder()
        .key(infrastructureKey())
        .status(statusInput());
  }

  public UpdateProducerBindingStatusMutation.Builder updateProducerBindingStatusBuilder() {
    return UpdateProducerBindingStatusMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .status(statusInput());
  }

  public UpdateProducerStatusMutation.Builder updateProducerStatusBuilder() {
    return UpdateProducerStatusMutation.builder()
        .key(producerKeyInputBuilder().build())
        .status(statusInput());
  }

  public UpdateSchemaStatusMutation.Builder updateSchemaStatusBuilder() {
    return UpdateSchemaStatusMutation.builder()
        .key(schemaKeyInputBuilder().build())
        .status(statusInput());
  }

  public UpdateStreamBindingStatusMutation.Builder updateStreamBindingStatusBuilder() {
    return UpdateStreamBindingStatusMutation.builder()
        .key(streamBindingKeyInputBuilder().build())
        .status(statusInput());
  }

  public UpdateStreamStatusMutation.Builder updateStreamStatusBuilder() {
    return UpdateStreamStatusMutation.builder()
        .key(streamKeyInputBuilder().build())
        .status(statusInput());
  }
}
