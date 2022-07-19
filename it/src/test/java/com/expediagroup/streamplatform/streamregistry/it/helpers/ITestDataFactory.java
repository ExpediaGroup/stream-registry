/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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

import static com.expediagroup.streamplatform.streamregistry.core.handlers.IdentityHandler.DEFAULT;

import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.*;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.type.*;


public class ITestDataFactory {

  private static final ObjectMapper mapper = new ObjectMapper();
  public String zoneName;
  public String domainName;
  public String consumerName;
  public String streamName;
  public String schemaName;
  public String key;
  public String value;
  public String description;
  public String infrastructureName;
  public String producerName;
  public String processName;


  public ITestDataFactory(String suffix) {
    zoneName = "zone_name_" + suffix;
    domainName = "domain_name_" + suffix;
    consumerName = "consumer_name_" + suffix;
    schemaName = "schema_name_" + suffix;
    streamName = "stream_name_" + suffix;
    infrastructureName = "infrastructure_name_" + suffix;
    producerName = "producer_name_" + suffix;
    processName = "process_name_" + suffix;

    key = "key_" + suffix;
    value = "value_" + suffix;
    description = "description_" + suffix;
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
        .type(type)
        .security(Collections.singletonList(
          SecurityInput.builder().role("admin").principals(
            Collections.singletonList(PrincipalInput.builder().name("user1").build())
          ).build())
        )
      .function("function");
  }

  public UpsertDomainMutation.Builder upsertDomainMutationBuilder() {
    return UpsertDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpsertConsumerMutation.Builder upsertConsumerMutationBuilder() {
    return UpsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public InsertConsumerMutation.Builder insertConsumerMutationBuilder() {
    return InsertConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public InsertZoneMutation.Builder insertZoneMutationBuilder() {
    return InsertZoneMutation.builder()
        .key(zoneKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpsertZoneMutation.Builder upsertZoneMutationBuilder() {
    return UpsertZoneMutation.builder()
        .key(zoneKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpdateZoneMutation.Builder updateZoneMutationBuilder() {
    return UpdateZoneMutation.builder()
        .key(zoneKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpdateZoneStatusMutation.Builder updateZoneStatusBuilder() {
    return UpdateZoneStatusMutation.builder()
        .key(zoneKeyInputBuilder().build())
        .status(StatusInput.builder().agentStatus(
            mapper.createObjectNode().put("skey", "svalue")
        ).build());
  }

  public ZoneKeyInput.Builder zoneKeyInputBuilder() {
    return ZoneKeyInput.builder().name(zoneName);
  }

  public UpdateConsumerMutation.Builder updateConsumerMutationBuilder() {
    return UpdateConsumerMutation.builder()
        .key(consumerKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
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
        .specification(specificationInputBuilder(DEFAULT).build());
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
        .specification(specificationInputBuilder(DEFAULT).build());
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
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpdateProducerBindingMutation.Builder updateProducerBindingMutationBuilder() {
    return UpdateProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpsertProducerBindingMutation.Builder upsertProducerBindingMutationBuilder() {
    return UpsertProducerBindingMutation.builder()
        .key(producerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
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
        .specification(specificationInputBuilder(DEFAULT).build());
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
        .specification(specificationInputBuilder(DEFAULT).build())
        .schema(schemaKeyInputBuilder().build())
        .key(streamKeyInputBuilder().build());
  }

  public UpdateStreamMutation.Builder updateStreamMutationBuilder() {
    return UpdateStreamMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
        .key(streamKeyInputBuilder().build());
  }

  public UpsertStreamMutation.Builder upsertStreamMutationBuilder() {
    return UpsertStreamMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
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
        .specification(specificationInputBuilder(DEFAULT).build())
        .key(schemaKeyInputBuilder().build());
  }

  public UpdateSchemaMutation.Builder updateSchemaMutationBuilder() {
    return UpdateSchemaMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
        .key(schemaKeyInputBuilder().build());
  }

  public UpsertSchemaMutation.Builder upsertSchemaMutationBuilder() {
    return UpsertSchemaMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
        .key(schemaKeyInputBuilder().build());
  }

  public SchemaKeyInput.Builder schemaKeyInputBuilder() {
    return SchemaKeyInput.builder()
        .domain(domainName)
        .name(schemaName);
  }

  public InsertStreamBindingMutation.Builder insertStreamBindingMutationBuilder() {
    return InsertStreamBindingMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
        .key(streamBindingKeyInputBuilder().build());
  }

  public UpdateStreamBindingMutation.Builder updateStreamBindingMutationBuilder() {
    return UpdateStreamBindingMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
        .key(streamBindingKeyInputBuilder().build());
  }

  public UpsertStreamBindingMutation.Builder upsertStreamBindingMutationBuilder() {
    return UpsertStreamBindingMutation.builder()
        .specification(specificationInputBuilder(DEFAULT).build())
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
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public InsertProducerMutation.Builder insertProducerMutationBuilder() {
    return InsertProducerMutation.builder()
        .key(producerKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpdateDomainStatusMutation.Builder updateDomainStatusMutation() {
    return UpdateDomainStatusMutation.builder()
        .key(domainKeyInputBuilder().build())
        .status(statusInput());
  }

  public InsertInfrastructureMutation.Builder insertInfrastructureMutationBuilder() {
    return InsertInfrastructureMutation.builder()
        .key(infrastructureKey())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpdateInfrastructureMutation.Builder updateInfrastructureMutationBuilder() {
    return UpdateInfrastructureMutation.builder()
        .key(infrastructureKey())
        .specification(specificationInputBuilder(DEFAULT).build());
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
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public InsertConsumerBindingMutation.Builder insertConsumerBindingMutationBuilder() {
    return InsertConsumerBindingMutation.builder()
        .key(consumerBindingKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public InsertDomainMutation.Builder insertDomainMutationBuilder() {
    return InsertDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public UpdateDomainMutation.Builder updateDomainMutationBuilder() {
    return UpdateDomainMutation.builder()
        .key(domainKeyInputBuilder().build())
        .specification(specificationInputBuilder(DEFAULT).build());
  }

  public DeleteConsumerBindingMutation.Builder deleteConsumerBindingMutationBuilder() {
    return DeleteConsumerBindingMutation.builder().key(consumerBindingKeyInputBuilder().build());
  }

  public DeleteConsumerMutation.Builder deleteConsumerMutationBuilder() {
    return DeleteConsumerMutation.builder().key(consumerKeyInputBuilder().build());
  }

  public DeleteProducerBindingMutation.Builder deleteProducerBindingMutationBuilder() {
    return DeleteProducerBindingMutation.builder().key(producerBindingKeyInputBuilder().build());
  }

  public DeleteProducerMutation.Builder deleteProducerMutationBuilder() {
    return DeleteProducerMutation.builder().key(producerKeyInputBuilder().build());
  }

  public DeleteSchemaMutation.Builder deleteSchemaMutationBuilder() {
    return DeleteSchemaMutation.builder().key(schemaKeyInputBuilder().build());
  }

  public DeleteStreamMutation.Builder deleteStreamMutationBuilder() {
    return DeleteStreamMutation.builder().key(streamKeyInputBuilder().build());
  }

  public DeleteStreamBindingMutation.Builder deleteStreamBindingMutationBuilder() {
    return DeleteStreamBindingMutation.builder().key(streamBindingKeyInputBuilder().build());
  }

  // Process
  public final ProcessKeyInput.Builder processKeyInputBuilder() {
    return ProcessKeyInput.builder()
      .domain(domainName)
      .name(processName);
  }

  public UpsertProcessMutation.Builder upsertProcessMutationBuilder() {
    return UpsertProcessMutation.builder()
      .key(processKeyInputBuilder().build())
      .specification(specificationInputBuilder(DEFAULT).build())
      .zones(Collections.singletonList(zoneKeyInputBuilder().build()))
      .inputs(Collections.singletonList(ProcessInputStreamInput.builder()
        .stream(streamKeyInputBuilder().name(streamName).domain(domainName).version(1).build())
        .configuration(mapper.createObjectNode())
        .build()))
      .outputs(Collections.singletonList(ProcessOutputStreamInput.builder()
        .stream(streamKeyInputBuilder().name(streamName).domain(domainName).version(1).build())
        .configuration(mapper.createObjectNode())
        .build()));
  }

  public InsertProcessMutation.Builder insertProcessMutationBuilder() {
    return InsertProcessMutation.builder()
      .key(processKeyInputBuilder().build())
      .specification(specificationInputBuilder(DEFAULT).build())
      .zones(Collections.singletonList(zoneKeyInputBuilder().build()))
      .inputs(Collections.singletonList(ProcessInputStreamInput.builder()
        .stream(streamKeyInputBuilder().name(streamName).domain(domainName).version(1).build())
        .configuration(mapper.createObjectNode())
        .build()))
      .outputs(Collections.singletonList(ProcessOutputStreamInput.builder()
        .stream(streamKeyInputBuilder().name(streamName).domain(domainName).version(1).build())
        .configuration(mapper.createObjectNode())
        .build()));
  }

  public UpdateProcessMutation.Builder updateProcessMutationBuilder() {
    return UpdateProcessMutation.builder()
      .key(processKeyInputBuilder().build())
      .specification(specificationInputBuilder(DEFAULT).build())
      .zones(Collections.singletonList(zoneKeyInputBuilder().build()))
      .inputs(Collections.singletonList(ProcessInputStreamInput.builder()
        .stream(streamKeyInputBuilder().name(streamName).domain(domainName).version(1).build())
        .configuration(mapper.createObjectNode())
        .build()))
      .outputs(Collections.singletonList(ProcessOutputStreamInput.builder()
        .stream(streamKeyInputBuilder().name(streamName).domain(domainName).version(1).build())
        .configuration(mapper.createObjectNode())
        .build()));
  }

  public UpdateProcessStatusMutation.Builder updateProcessStatusBuilder() {
    return UpdateProcessStatusMutation.builder()
      .key(processKeyInputBuilder().build())
      .status(StatusInput.builder().agentStatus(
        mapper.createObjectNode().put("skey", "svalue")
      ).build());
  }

  public DeleteProcessMutation.Builder deleteProcessMutationBuilder() {
    return DeleteProcessMutation.builder().key(processKeyInputBuilder().build());
  }

  public DeleteDomainMutation.Builder deleteDomainMutationBuilder() {
    return DeleteDomainMutation.builder().key(domainKeyInputBuilder().build());
  }

  // ProcessBinding
  public final ProcessBindingKeyInput.Builder processBindingKeyInputBuilder() {
    return ProcessBindingKeyInput.builder()
      .domainName(domainName)
      .infrastructureZone(zoneName)
      .processName(processName);
  }

  public final ProcessInputStreamBindingInput.Builder processInputStreamBindingInputBuilder() {
    return ProcessInputStreamBindingInput.builder()
      .streamBindingKey(streamBindingKeyInputBuilder().build())
      .configuration(mapper.createObjectNode());
  }

  public final ProcessOutputStreamBindingInput.Builder processOutputStreamBindingInputBuilder() {
    return ProcessOutputStreamBindingInput.builder()
      .streamBindingKey(streamBindingKeyInputBuilder().build())
      .configuration(mapper.createObjectNode());
  }

  public UpsertProcessBindingMutation.Builder upsertProcessBindingMutationBuilder() {
    return UpsertProcessBindingMutation.builder()
      .key(processBindingKeyInputBuilder().build())
      .specification(specificationInputBuilder(DEFAULT).build())
      .zone(zoneKeyInputBuilder().build())
      .inputs(Collections.singletonList(processInputStreamBindingInputBuilder().build()))
      .outputs(Collections.singletonList(processOutputStreamBindingInputBuilder().build()));
  }

  public InsertProcessBindingMutation.Builder insertProcessBindingMutationBuilder() {
    return InsertProcessBindingMutation.builder()
      .key(processBindingKeyInputBuilder().build())
      .specification(specificationInputBuilder(DEFAULT).build())
      .zone(zoneKeyInputBuilder().build())
      .inputs(Collections.singletonList(processInputStreamBindingInputBuilder().build()))
      .outputs(Collections.singletonList(processOutputStreamBindingInputBuilder().build()));
  }

  public UpdateProcessBindingMutation.Builder updateProcessBindingMutationBuilder() {
    return UpdateProcessBindingMutation.builder()
      .key(processBindingKeyInputBuilder().build())
      .specification(specificationInputBuilder(DEFAULT).build())
      .zone(zoneKeyInputBuilder().build())
      .inputs(Collections.singletonList(processInputStreamBindingInputBuilder().build()))
      .outputs(Collections.singletonList(processOutputStreamBindingInputBuilder().build()));
  }

  public UpdateProcessBindingStatusMutation.Builder updateProcessBindingStatusBuilder() {
    return UpdateProcessBindingStatusMutation.builder()
      .key(processBindingKeyInputBuilder().build())
      .status(StatusInput.builder().agentStatus(
        mapper.createObjectNode().put("skey", "svalue")
      ).build());
  }

  public DeleteProcessBindingMutation.Builder deleteProcessBindingMutationBuilder() {
    return DeleteProcessBindingMutation.builder().key(processBindingKeyInputBuilder().build());
  }
}
