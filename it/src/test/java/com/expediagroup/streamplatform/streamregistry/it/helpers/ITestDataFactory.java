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

import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.InsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateDomainStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateInfrastructureStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateProducerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateSchemaStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.UpdateStreamMutation;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class ITestDataFactory {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String EGSP_KAFKA = "egsp.kafka";
  public String zoneName;
  public String domainName;
  public String consumerName;
  public String streamName;
  public String key;
  public String value;
  public String description;
  private String infrastructureName;
  private String producerName;
  public ITestDataFactory(String suffix) {
    zoneName = "zoneName" + suffix;
    domainName = "domainName" + suffix;
    consumerName = "consumerName" + suffix;
    streamName = "streamName" + suffix;

    infrastructureName = "infrastructureName" + suffix;
    producerName = "producerName" + suffix;

    key = "key" + suffix;
    value = "value" + suffix;
    description = "description" + suffix;
  }

  public final DomainKeyInput.Builder domainKeyInputBuilder() {
    return DomainKeyInput.builder().name(domainName);
  }

  public final ConsumerKeyInput.Builder consumerKeyInputBuilder() {
    return ConsumerKeyInput.builder()
        .name(consumerName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1)
        .zone(zoneName);
  }

  public SpecificationInput.Builder specificationInputBuilder(String type) {
    return SpecificationInput.builder()
        .configuration(mapper.createObjectNode().put(key, value))
        .description(description)
        .tags(Collections.emptyList())
        .type(type);
  }

  public UpsertDomainMutation.Builder upsertDomainMutationBuilder() {
    return UpsertDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder("default").build());
  }

  public UpsertConsumerMutation.Builder upsertConsumerMutationBuilder() {
    return UpsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InsertConsumerMutation.Builder insertConsumerMutationBuilder() {
    return InsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InsertZoneMutation.Builder insertZoneMutationBuilder() {
    return InsertZoneMutation.builder()
        .key(zoneKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public UpsertZoneMutation.Builder upsertZoneMutationBuilder() {
    return UpsertZoneMutation.builder()
        .key(zoneKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public ZoneKeyInput.Builder zoneKeyInputBuilder() {
    return ZoneKeyInput.builder().name(zoneName);
  }

  public UpdateConsumerMutation.Builder updateConsumerMutationBuilder() {
    return UpdateConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
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
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public ConsumerBindingKeyInput.Builder consumerBindingKeyInputBuilder() {
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
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InfrastructureKeyInput infrastructureKey() {
    return InfrastructureKeyInput.builder()
        .name(infrastructureName)
        .zone(zoneName)
        .build();
  }

  public InsertProducerBindingMutation.Builder insertProducerBindingMutationBuilder() {
    return InsertProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public UpdateProducerBindingMutation.Builder updateProducerBindingMutationBuilder() {
    return UpdateProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public UpsertProducerBindingMutation.Builder upsertProducerBindingMutationBuilder() {
    return UpsertProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InfrastructureKeyInput.Builder infrastructureKeyInputBuilder() {
    return InfrastructureKeyInput.builder()
        .name(infrastructureName)
        .zone(zoneName);
  }

  public ProducerBindingKeyInput.Builder producerBindingKeyInputBuilder() {
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
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public ProducerKeyInput.Builder producerKeyInputBuilder() {
    return ProducerKeyInput.builder()
        .name(producerName)
        .streamDomain(domainName)
        .streamName(streamName)
        .streamVersion(1)
        .zone(zoneName);
  }

  public InsertStreamMutation.Builder insertStreamMutationBuilder() {
    return InsertStreamMutation.builder()
        .specification(specificationInputBuilder(EGSP_KAFKA).build())
        .schema(schemaKeyInputBuilder().build())
        .key(streamKeyInputBuilder().build());
  }

  public UpdateStreamMutation.Builder updateStreamMutationBuilder() {
    return UpdateStreamMutation.builder()
        .specification(specificationInputBuilder(EGSP_KAFKA).build())
        // .schema(schemaKeyInputBuilder().build())
        .key(streamKeyInputBuilder().build());
  }

  public UpsertStreamMutation.Builder upsertStreamMutationBuilder() {
    return UpsertStreamMutation.builder()
        .specification(specificationInputBuilder(EGSP_KAFKA).build())
        .schema(schemaKeyInputBuilder().build())
        .key(streamKeyInputBuilder().build());
  }

  public StreamKeyInput.Builder streamKeyInputBuilder() {
    return StreamKeyInput.builder()
        .domain(domainName)
        .name(streamName)
        .version(1);
  }

  public InsertSchemaMutation.Builder insertSchemaMutationBuilder() {
    return InsertSchemaMutation.builder()
        .specification(specificationInputBuilder("egsp.confluent").build())
        .key(schemaKeyInputBuilder().build());
  }

  public UpdateSchemaMutation.Builder updateSchemaMutationBuilder() {
    return UpdateSchemaMutation.builder()
        .specification(specificationInputBuilder("egsp.confluent").build())
        .key(schemaKeyInputBuilder().build());
  }

  public UpsertSchemaMutation.Builder upsertSchemaMutationBuilder() {
    return UpsertSchemaMutation.builder()
        .specification(specificationInputBuilder("egsp.confluent").build())
        .key(schemaKeyInputBuilder().build());
  }

  public SchemaKeyInput.Builder schemaKeyInputBuilder() {
    return SchemaKeyInput.builder()
        .domain(domainName)
        .name(streamName);
  }

  public InsertStreamBindingMutation.Builder insertStreamBindingMutationBuilder() {
    return InsertStreamBindingMutation.builder()
        .specification(specificationInputBuilder(EGSP_KAFKA).build())
        .key(streamBindingKeyInputBuilder().build());
  }

  public UpdateStreamBindingMutation.Builder updateStreamBindingMutationBuilder() {
    return UpdateStreamBindingMutation.builder()
        .specification(specificationInputBuilder(EGSP_KAFKA).build())
        .key(streamBindingKeyInputBuilder().build());
  }

  public UpsertStreamBindingMutation.Builder upsertStreamBindingMutationBuilder() {
    return UpsertStreamBindingMutation.builder()
        .specification(specificationInputBuilder(EGSP_KAFKA).build())
        .key(streamBindingKeyInputBuilder().build());
  }

  public StreamBindingKeyInput.Builder streamBindingKeyInputBuilder() {
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
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InsertProducerMutation.Builder insertProducerMutationBuilder() {
    return InsertProducerMutation.builder()
        .key(producerKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public UpdateDomainStatusMutation.Builder updateDomainStatusMutation() {
    return UpdateDomainStatusMutation.builder()
        .key(domainKeyInputBuilder().build())
        .status(statusInput());
  }

  public InsertInfrastructureMutation.Builder insertInfrastructureMutationBuilder() {
    return InsertInfrastructureMutation.builder()
        .key(infrastructureKey())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public UpdateInfrastructureMutation.Builder updateInfrastructureMutationBuilder() {
    return UpdateInfrastructureMutation.builder()
        .key(infrastructureKey())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
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

  public UpdateConsumerBindingMutation.Builder updateConsumerBindingMutationBuilder() {
    return UpdateConsumerBindingMutation.builder()
        .key(consumerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InsertConsumerBindingMutation.Builder insertConsumerBindingMutationBuilder() {
    return InsertConsumerBindingMutation.builder()
        .key(consumerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(EGSP_KAFKA).build());
  }

  public InsertDomainMutation.Builder insertDomainMutationBuilder() {
    return InsertDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder("default").build());
  }

  public UpdateDomainMutation.Builder updateDomainMutationBuilder() {
    return UpdateDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder("default").build());
  }
}
