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


public class ITestClient extends StreamRegistryClient {

  public ITestClient(final String url, final String streamRegistryUsername, final String streamRegistryPassword) {
    super(url, streamRegistryUsername, streamRegistryPassword);
  }

  public void createDomain(ITestDataFactory factory) {
    invoke(factory.upsertDomainMutationBuilder().build());
  }

  public void createSchema(ITestDataFactory factory) {
    invoke(factory.upsertSchemaMutationBuilder().build());
  }

  public void createStream(ITestDataFactory factory) {
    invoke(factory.upsertStreamMutationBuilder().build());
  }

  public void createProducer(ITestDataFactory factory) {
    invoke(factory.upsertProducerMutationBuilder().build());
  }

  public void createConsumer(ITestDataFactory factory) {
    invoke(factory.upsertConsumerMutationBuilder().build());
  }

  public void createZone(ITestDataFactory factory) {
    invoke(factory.upsertZoneMutationBuilder().build());
  }

  public void createInfrastructure(ITestDataFactory factory) {
    invoke(factory.upsertInfrastructureMutationBuilder().build());
  }

  public void createStreamBinding(ITestDataFactory factory) {
    invoke(factory.upsertStreamBindingMutationBuilder().build());
  }

  public void createProcess(ITestDataFactory factory) {
    invoke(factory.upsertProcessMutationBuilder().build());
  }
}
