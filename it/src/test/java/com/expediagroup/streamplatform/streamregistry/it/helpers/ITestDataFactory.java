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
package com.expediagroup.streamplatform.streamregistry.it.helpers;

import static com.expediagroup.streamplatform.streamregistry.core.handlers.IdentityHandler.DEFAULT;

import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.DeleteStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.InsertZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateConsumerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateDomainStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateInfrastructureStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateProducerStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateSchemaStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamBindingStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateStreamStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateZoneMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpdateZoneStatusMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertConsumerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertConsumerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertDomainMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertInfrastructureMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertProducerBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertProducerMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertSchemaMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertStreamBindingMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertStreamMutation;
import com.expediagroup.streamplatform.streamregistry.graphql.client.test.UpsertZoneMutation;
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

  public ITestDataFactory(String suffix) {
    zoneName = "zone_name_" + suffix;
    domainName = "domain_name_" + suffix;
    consumerName = "consumer_name_" + suffix;
    schemaName = "schema_name_" + suffix;
    streamName = "stream_name_" + suffix;
    infrastructureName = "infrastructure_name_" + suffix;
    producerName = "producer_name_" + suffix;

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
        );
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
}
