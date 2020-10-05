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
package com.expediagroup.streamplatform.streamregistry.cli.action;

import lombok.Getter;

import com.apollographql.apollo.ApolloClient;

import picocli.CommandLine.Option;

import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.graphql.Credentials;
import com.expediagroup.streamplatform.streamregistry.state.graphql.DefaultApolloClientFactory;
import com.expediagroup.streamplatform.streamregistry.state.graphql.GraphQLEventSender;

public abstract class GraphQLEventSenderAction implements EventSenderAction {
  @Option(names = "--streamRegistryUrl", required = true)
  @Getter String streamRegistryUrl;

  @Option(names = "--username", required = true)
  @Getter String username;

  @Option(names = "--password", required = true)
  @Getter String password;

  @Override
  public EventSender sender() {
    ApolloClient client = new DefaultApolloClientFactory(streamRegistryUrl, new Credentials(username, password)).create();
    return new GraphQLEventSender(client);
  }
}
