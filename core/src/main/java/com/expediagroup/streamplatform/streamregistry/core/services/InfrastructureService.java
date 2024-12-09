/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.InfrastructureValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.InfrastructureView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.ProcessBinding;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.repository.InfrastructureRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor
public class InfrastructureService {
  private final InfrastructureView infrastructureView;
  private final HandlerService handlerService;
  private final InfrastructureValidator infrastructureValidator;
  private final InfrastructureRepository infrastructureRepository;
  private final StreamBindingView streamBindingView;
  private final ConsumerBindingView consumerBindingView;
  private final ProducerBindingView producerBindingView;
  private final ProcessBindingView processBindingView;

  @PreAuthorize("hasPermission(#infrastructure, 'CREATE')")
  public Optional<Infrastructure> create(Infrastructure infrastructure) throws ValidationException {
    if (infrastructureView.get(infrastructure.getKey()).isPresent()) {
      throw new ValidationException("Can't create " + infrastructure.getKey() + " because it already exists");
    }
    infrastructureValidator.validateForCreate(infrastructure);
    infrastructure.setSpecification(handlerService.handleInsert(infrastructure));
    return saveSpecification(infrastructure);
  }

  @PreAuthorize("hasPermission(#infrastructure, 'UPDATE')")
  public Optional<Infrastructure> update(Infrastructure infrastructure) throws ValidationException {
    val existing = infrastructureView.get(infrastructure.getKey());
    if (existing.isEmpty()) {
      throw new ValidationException("Can't update " + infrastructure.getKey().getName() + " because it doesn't exist");
    }
    infrastructureValidator.validateForUpdate(infrastructure, existing.get());
    infrastructure.setSpecification(handlerService.handleUpdate(infrastructure, existing.get()));
    return saveSpecification(infrastructure);
  }

  @PreAuthorize("hasPermission(#infrastructure, 'UPDATE_STATUS')")
  public Optional<Infrastructure> updateStatus(Infrastructure infrastructure, Status status) {
    infrastructure.setStatus(status);
    return saveStatus(infrastructure);
  }

  private Optional<Infrastructure> saveSpecification(Infrastructure infrastructure) {
    return Optional.ofNullable(infrastructureRepository.saveSpecification(infrastructure));
  }

  private Optional<Infrastructure> saveStatus(Infrastructure infrastructure) {
    return Optional.ofNullable(infrastructureRepository.saveStatus(infrastructure));
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Infrastructure> get(InfrastructureKey key) {
    return infrastructureView.get(key);
  }


  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Infrastructure> findAll(Predicate<Infrastructure> filter) {
    return infrastructureRepository.findAll().stream().filter(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#infrastructure, 'DELETE')")
  public void delete(Infrastructure infrastructure) {
    handlerService.handleDelete(infrastructure);
    streamBindingView
      .findAll(sb -> sb.getKey().getInfrastructureKey().equals(infrastructure.getKey()))
      .findAny()
      .ifPresent(sb -> { throw new IllegalStateException("Infrastructure is used in stream: " + sb.getKey().getStreamKey()); });

    consumerBindingView
      .findAll(cb -> cb.getKey().getStreamBindingKey().getInfrastructureKey().equals(infrastructure.getKey()))
      .findAny()
      .ifPresent(cb -> { throw new IllegalStateException("Infrastructure is used in consumer binding: " + cb.getKey()); });

    producerBindingView
      .findAll(pb -> pb.getKey().getStreamBindingKey().getInfrastructureKey().equals(infrastructure.getKey()))
      .findAny()
      .ifPresent(pb -> { throw new IllegalStateException("Infrastructure is used in producer binding: " + pb.getKey()); });

    processBindingView
      .findAll(pb -> isInfrastructureUsedInProcessBinding(infrastructure, pb))
      .findAny()
      .ifPresent(pb -> { throw new IllegalStateException("Infrastructure is used in process binding: " + pb.getKey()); });

    infrastructureRepository.delete(infrastructure);
  }

  private boolean isInfrastructureUsedInProcessBinding(Infrastructure infrastructure, ProcessBinding processBinding) {
    return isInfrastructureUsedInProcessBindingOutput(infrastructure, processBinding) ||
      isInfrastructureUsedInProcessBindingInput(infrastructure, processBinding);
  }

  private boolean isInfrastructureUsedInProcessBindingOutput(Infrastructure infrastructure, ProcessBinding processBinding) {
    return processBinding.getOutputs().stream().map(o -> o.getStreamBindingKey().getInfrastructureKey())
      .anyMatch(infra -> infra.equals(infrastructure.getKey()));
  }

  private boolean isInfrastructureUsedInProcessBindingInput(Infrastructure infrastructure, ProcessBinding processBinding) {
    return processBinding.getInputs().stream().map(i -> i.getStreamBindingKey().getInfrastructureKey())
      .anyMatch(infra -> infra.equals(infrastructure.getKey()));
  }
}
