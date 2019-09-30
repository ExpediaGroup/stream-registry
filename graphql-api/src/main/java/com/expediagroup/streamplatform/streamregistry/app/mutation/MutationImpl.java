package com.expediagroup.streamplatform.streamregistry.app.mutation;
/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ZoneKeyInput;
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

public interface MutationImpl extends GraphQLMutationResolver {

  Domain insertDomain(
      DomainKeyInput key,
      SpecificationInput specification
  );

  Domain updateDomain(
      DomainKeyInput key,
      SpecificationInput specification
  );

  Domain upsertDomain(
      DomainKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteDomain(
      DomainKeyInput key
  );

  Domain updateDomainStatus(
      DomainKeyInput key,
      StatusInput status
  );

  Schema insertSchema(
      SchemaKeyInput key,
      SpecificationInput specification
  );

  Schema updateSchema(
      SchemaKeyInput key,
      SpecificationInput specification
  );

  Schema upsertSchema(
      SchemaKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteSchema(
      SchemaKeyInput key
  );

  Schema updateSchemaStatus(
      SchemaKeyInput key,
      StatusInput status
  );

  Stream insertStream(
      StreamKeyInput key,
      SpecificationInput specification,
      SchemaKeyInput schema
  );

  Stream updateStream(
      StreamKeyInput key,
      SpecificationInput specification
  );

  Stream upsertStream(
      StreamKeyInput key,
      SpecificationInput specification,
      SchemaKeyInput schema
  );

  Boolean deleteStream(
      StreamKeyInput key
  );

  Stream updateStreamStatus(
      StreamKeyInput key,
      StatusInput status
  );

  Zone insertZone(
      ZoneKeyInput key,
      SpecificationInput specification
  );

  Zone updateZone(
      ZoneKeyInput key,
      SpecificationInput specification
  );

  Zone upsertZone(
      ZoneKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteZone(
      ZoneKeyInput key
  );

  Zone updateZoneStatus(
      ZoneKeyInput key,
      StatusInput status
  );

  Infrastructure insertInfrastructure(
      InfrastructureKeyInput key,
      SpecificationInput specification
  );

  Infrastructure updateInfrastructure(
      InfrastructureKeyInput key,
      SpecificationInput specification
  );

  Infrastructure upsertInfrastructure(
      InfrastructureKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteInfrastructure(
      InfrastructureKeyInput key
  );

  Infrastructure updateInfrastructureStatus(
      InfrastructureKeyInput key,
      StatusInput status
  );

  Producer insertProducer(
      ProducerKeyInput key,
      SpecificationInput specification
  );

  Producer updateProducer(
      ProducerKeyInput key,
      SpecificationInput specification
  );

  Producer upsertProducer(
      ProducerKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteProducer(
      ProducerKeyInput key
  );

  Producer updateProducerStatus(
      ProducerKeyInput key,
      StatusInput status
  );

  Consumer insertConsumer(
      ConsumerKeyInput key,
      SpecificationInput specification
  );

  Consumer updateConsumer(
      ConsumerKeyInput key,
      SpecificationInput specification
  );

  Consumer upsertConsumer(
      ConsumerKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteConsumer(
      ConsumerKeyInput key
  );

  Consumer updateConsumerStatus(
      ConsumerKeyInput key,
      StatusInput status
  );

  StreamBinding insertStreamBinding(
      StreamBindingKeyInput key,
      SpecificationInput specification
  );

  StreamBinding updateStreamBinding(
      StreamBindingKeyInput key,
      SpecificationInput specification
  );

  StreamBinding upsertStreamBinding(
      StreamBindingKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteStreamBinding(
      StreamBindingKeyInput key
  );

  StreamBinding updateStreamBindingStatus(
      StreamBindingKeyInput key,
      StatusInput status
  );

  ProducerBinding insertProducerBinding(
      ProducerBindingKeyInput key,
      SpecificationInput specification
  );

  ProducerBinding updateProducerBinding(
      ProducerBindingKeyInput key,
      SpecificationInput specification
  );

  ProducerBinding upsertProducerBinding(
      ProducerBindingKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteProducerBinding(
      ProducerBindingKeyInput key
  );

  ProducerBinding updateProducerBindingStatus(
      ProducerBindingKeyInput key,
      StatusInput status
  );

  ConsumerBinding insertConsumerBinding(
      ConsumerBindingKeyInput key,
      SpecificationInput specification
  );

  ConsumerBinding updateConsumerBinding(
      ConsumerBindingKeyInput key,
      SpecificationInput specification
  );

  ConsumerBinding upsertConsumerBinding(
      ConsumerBindingKeyInput key,
      SpecificationInput specification
  );

  Boolean deleteConsumerBinding(
      ConsumerBindingKeyInput key
  );

  ConsumerBinding updateConsumerBindingStatus(
      ConsumerBindingKeyInput key,
      StatusInput status
  );
}