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

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpsertSchemaMutation;
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
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.client.type.ZoneKeyInput;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ITestDataFactory {

  private static final ObjectMapper mapper = new ObjectMapper();

  public StringValue zoneName = new StringValue();
  public StringValue domainName = new StringValue();
  public StringValue consumerName = new StringValue();
  public StringValue streamName = new StringValue();
  private StringValue infrastructureName = new StringValue();
  private StringValue infrastructureZone = new StringValue();
  private StringValue streamDomain = new StringValue();
  private StringValue producerName = new StringValue();

  public StringValue key = new StringValue();
  public StringValue value = new StringValue();
  public StringValue description = new StringValue();

  private DomainKeyInput.Builder domainKeyInputBuilder;

  public final DomainKeyInput.Builder domainKeyInputBuilder() {
    if (domainKeyInputBuilder == null) {
      domainKeyInputBuilder = DomainKeyInput.builder().name(domainName.getValue());
    }
    return domainKeyInputBuilder;
  }

  public final ConsumerKeyInput.Builder consumerKeyInputBuilder() {
    return ConsumerKeyInput.builder()
        .name(consumerName.getValue())
        .streamDomain(domainName.getValue())
        .streamName(streamName.getValue())
        .streamVersion(1)
        .zone(zoneName.getValue());
  }

  private SpecificationInput.Builder specificationInputBuilder;

  public SpecificationInput.Builder specificationInputBuilder() {
    if (specificationInputBuilder == null) {
      specificationInputBuilder = SpecificationInput.builder()
          .configuration(mapper.createObjectNode().put(key.getValue(), value.getValue()))
          .description(description.getValue())
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
      ZoneKeyInputBuilder = ZoneKeyInput.builder().name(zoneName.getValue());
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
        .consumerName(consumerName.getValue())
        .infrastructureName(infrastructureName.getValue())
        .infrastructureZone(infrastructureZone.getValue())
        .streamDomain(streamDomain.getValue())
        .streamName(streamName.getValue())
        .streamVersion(1);
  }

  public UpdateConsumerBindingStatusMutation.Builder updateConsumerBindingStatusBuilder() {
    return UpdateConsumerBindingStatusMutation.builder()
        .key(consumerBindingKeyInputBuilder().build())
        .status(StatusInput.builder().agentStatus(
            mapper.createObjectNode().put("skey", "svalue")
        ).build());
  }

  public UpsertInfrastructureMutation.Builder upsertInfrastructureMutationBuilder() {
    return UpsertInfrastructureMutation.builder()
        .key(infrastructureKey())
        .specification(specificationInputBuilder().build());
  }

  private InfrastructureKeyInput infrastructureKey() {
    return InfrastructureKeyInput.builder()
        .name(infrastructureName.getValue())
        .zone(zoneName.getValue())
        .build();
  }

  public UpsertProducerBindingMutation.Builder upsertProducerBindingMutationBuilder() {
    return UpsertProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  private ProducerBindingKeyInput.Builder producerBindingKeyInputBuilder() {
    return ProducerBindingKeyInput.builder()
        .infrastructureName(infrastructureName.getValue())
        .infrastructureZone(infrastructureZone.getValue())
        .producerName(producerName.getValue())
        .streamDomain(streamDomain.getValue())
        .streamName(streamName.getValue())
        .streamVersion(1);
  }

  public UpsertProducerMutation.Builder upsertProducerMutationBuilder() {
    return UpsertProducerMutation.builder()
        .key(producerKeyInputBuilder().build())
        .specification(specificationInputBuilder().build());
  }

  private ProducerKeyInput.Builder producerKeyInputBuilder() {
    return ProducerKeyInput.builder()
        .name(producerName.getValue())
        .streamDomain(streamDomain.getValue())
        .streamName(streamName.getValue())
        .streamVersion(1)
        .zone(zoneName.getValue());
  }

  public UpsertStreamMutation.Builder upsertStreamMutationBuilder() {
    return UpsertStreamMutation.builder()
        .specification(specificationInputBuilder().build())
        .key(streamKeyInputBuilder().build()
        );
  }

  private StreamKeyInput.Builder streamKeyInputBuilder() {
    return StreamKeyInput.builder()
        .domain(domainName.getValue())
        .name(streamName.getValue())
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
        .domain(domainName.getValue())
        .name(streamName.getValue());
  }
}
