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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.handlers.HandlerService;
import com.expediagroup.streamplatform.streamregistry.core.validators.ProcessValidator;
import com.expediagroup.streamplatform.streamregistry.core.validators.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessBindingView;
import com.expediagroup.streamplatform.streamregistry.core.views.ProcessView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Process;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProcessRepository;

@Component
@RequiredArgsConstructor
public class ProcessService {
  private final HandlerService handlerService;
  private final ProcessValidator processValidator;
  private final ProcessRepository processRepository;
  private final ProcessBindingService processBindingService;
  private final ProcessBindingView processBindingView;
  private final ProcessView processView;

  @PreAuthorize("hasPermission(#process, 'CREATE')")
  public Optional<Process> create(Process process) throws ValidationException {
    if (processView.exists(process.getKey())) {
      throw new ValidationException("Can't create " + process.getKey() + " because it already exists");
    }
    processValidator.validateForCreate(process);
    process.setSpecification(handlerService.handleInsert(process));
    process.getZones().forEach(zoneKey ->
      process.getInputs().forEach(input -> createConsumer(
        new Consumer(
          new ConsumerKey(
            input.getStream().getDomainKey().getName(),
            input.getStream().getName(),
            input.getStream().getVersion(),
            zoneKey.getName(),
            process.getKey().getName()
          ),
          process.getSpecification(),
          process.getStatus()
        )
      )));

    process.getZones().forEach(zoneKey ->
      process.getOutputs().forEach(output -> createProducer(
        new Producer(
          new ProducerKey(
            output.getStream().getDomainKey().getName(),
            output.getStream().getName(),
            output.getStream().getVersion(),
            zoneKey.getName(),
            process.getKey().getName()
          ),
          process.getSpecification(),
          process.getStatus()
        )
      )));
    return save(process);
  }

  @PreAuthorize("hasPermission(#consumer, 'CREATE')")
  public void createConsumer(Consumer consumer) {

  }

  @PreAuthorize("hasPermission(#producer, 'CREATE')")
  public void createProducer(Producer producer) {

  }

  @PreAuthorize("hasPermission(#process, 'UPDATE')")
  public Optional<Process> update(Process process) throws ValidationException {
    val existing = processView.get(process.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + process.getKey().getName() + " because it doesn't exist");
    }
    processValidator.validateForUpdate(process, existing.get());
    process.setSpecification(handlerService.handleUpdate(process, existing.get()));

    process.getZones().forEach(zoneKey ->
      process.getInputs().forEach(input -> updateConsumer(
        new Consumer(
          new ConsumerKey(
            input.getStream().getDomainKey().getName(),
            input.getStream().getName(),
            input.getStream().getVersion(),
            zoneKey.getName(),
            process.getKey().getName()
          ),
          process.getSpecification(),
          process.getStatus()
        )
      )));

    process.getZones().forEach(zoneKey ->
      process.getOutputs().forEach(output -> updateProducer(
        new Producer(
          new ProducerKey(
            output.getStream().getDomainKey().getName(),
            output.getStream().getName(),
            output.getStream().getVersion(),
            zoneKey.getName(),
            process.getKey().getName()
          ),
          process.getSpecification(),
          process.getStatus()
        )
      )));

    return save(process);
  }

  @PreAuthorize("hasPermission(#producer, 'UPDATE')")
  private void updateProducer(Producer producer) {

  }

  @PreAuthorize("hasPermission(#consumer, 'UPDATE')")
  private void updateConsumer(Consumer consumer) {

  }

  @PreAuthorize("hasPermission(#process, 'UPDATE_STATUS')")
  public Optional<Process> updateStatus(Process process, Status status) {
    process.setStatus(status);
    return save(process);
  }

  private Optional<Process> save(Process process) {
    return Optional.ofNullable(processRepository.save(process));
  }

  @PostAuthorize("returnObject.isPresent() ? hasPermission(returnObject, 'READ') : true")
  public Optional<Process> get(ProcessKey key) {
    return processView.get(key);
  }

  @PostFilter("hasPermission(filterObject, 'READ')")
  public List<Process> findAll(Predicate<Process> filter) {
    return processView.findAll(filter).collect(toList());
  }

  @PreAuthorize("hasPermission(#process, 'DELETE')")
  public void delete(Process process) {
    handlerService.handleDelete(process);

    // This will cascade to Consumers, ConsumerBindings and Producers, ProducerBindings also
    processBindingView
      .findAll(b -> b.getKey().getProcessKey().equals(process.getKey()))
      .forEach(processBindingService::delete);

    processRepository.delete(process);
  }

}
