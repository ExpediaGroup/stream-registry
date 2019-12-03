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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.ProducerBindingService;
import com.expediagroup.streamplatform.streamregistry.core.services.SessionService;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.SessionMutation;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Session;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;

@Component
@RequiredArgsConstructor
public class SessionMutationImpl implements SessionMutation {
  private final SessionService sessionService;
  private final ProducerBindingService producerBindingService;
  private final ConsumerBindingService consumerBindingService;

  @Override
  public Session create(List<ProducerKeyInput> producers, List<ConsumerKeyInput> consumers) {
    return sessionService.create(asSession(producers, consumers));
  }

  @Override
  public Session renew(String id, final String secret) {
    return sessionService.renew(id);
  }

  private Session asSession(List<ProducerKeyInput> producerKeyInputs, List<ConsumerKeyInput> consumerKeyInputs) {
    Session session = new Session();
    session.setProducerBindings(findProducerBindings(producerKeyInputs));
    session.setConsumerBindings(findConsumerBindings(consumerKeyInputs));
    return session;
  }

  private Set<ProducerBinding> findProducerBindings(List<ProducerKeyInput> producerKeyInputs) {
    return producerKeyInputs.stream()
        .map(ProducerKeyInput::asProducerKey)
        .map(this::producerBindingPredicate)
        .flatMap(predicate -> StreamSupport.stream(producerBindingService.findAll(predicate).spliterator(), false))
        .collect(Collectors.toSet());
  }

  private Predicate<ProducerBinding> producerBindingPredicate(ProducerKey producerKey) {
    return (ProducerBinding binding) -> binding.getKey().getProducerKey().equals(producerKey);
  }

  private Set<ConsumerBinding> findConsumerBindings(List<ConsumerKeyInput> consumerKeyInputs) {
    return consumerKeyInputs.stream()
        .map(ConsumerKeyInput::asConsumerKey)
        .map(this::consumerBindingPredicate)
        .flatMap(predicate -> StreamSupport.stream(consumerBindingService.findAll(predicate).spliterator(), false))
        .collect(Collectors.toSet());
  }

  private Predicate<ConsumerBinding> consumerBindingPredicate(ConsumerKey consumerKey) {
    return (ConsumerBinding binding) -> binding.getKey().getConsumerKey().equals(consumerKey);
  }
}
