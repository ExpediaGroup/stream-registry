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
import com.expediagroup.streamplatform.streamregistry.model.ProcessInputStream;
import com.expediagroup.streamplatform.streamregistry.model.ProcessOutputStream;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.Status;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProcessKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ZoneKey;
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
  private final ConsumerService consumerService;
  private final ProducerService producerService;

  @PreAuthorize("hasPermission(#process, 'CREATE')")
  public Optional<Process> create(Process process) throws ValidationException {
    if (processView.exists(process.getKey())) {
      throw new ValidationException("Can't create " + process.getKey() + " because it already exists");
    }
    processValidator.validateForCreate(process);
    process.setSpecification(handlerService.handleInsert(process));
    process.getZones().forEach(zoneKey ->
      process.getInputs().forEach(input -> consumerService.canCreateConsumer(
        buildConsumer(process, zoneKey, input)
      )));

    process.getZones().forEach(zoneKey ->
      process.getOutputs().forEach(output -> producerService.canCreateProducer(
        buildProducer(process, zoneKey, output)
      )));
    return save(process);
  }

  private Producer buildProducer(Process process, ZoneKey zoneKey, ProcessOutputStream output) {
    return new Producer(
      new ProducerKey(
        output.getStream().getDomainKey().getName(),
        output.getStream().getName(),
        output.getStream().getVersion(),
        zoneKey.getName(),
        process.getKey().getName()
      ),
      process.getSpecification(),
      process.getStatus()
    );
  }

  @PreAuthorize("hasPermission(#process, 'UPDATE')")
  public Optional<Process> update(Process process) throws ValidationException {
    val existing = processView.get(process.getKey());
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update " + process.getKey().getName() + " because it doesn't exist");
    }
    processValidator.validateForUpdate(process, existing.get());
    process.setSpecification(handlerService.handleUpdate(process, existing.get()));

    process.getZones().forEach(zoneKey -> {
      process.getInputs().forEach(input -> {
          // FOR EACH PROCESS INPUT CHECK IF IT EXISTS IN THE EXISTING PROCESS INPUTS AND IF IT DOES UPDATE EXISTING
          if (existing.get().getInputs().stream().anyMatch(i -> i.getStream().equals(input.getStream()))) {
            consumerService.canUpdateConsumer(buildConsumer(process, zoneKey, input));
            // FOR EACH PROCESS INPUT CHECK IF IT EXISTS IN THE EXISTING PROCESS INPUTS AND IF IT DOES NOT ADD NEW ONE
          } else {
            consumerService.canCreateConsumer(buildConsumer(process, zoneKey, input));
          }
        }
      );
      // FOR EACH PROCESS INPUT IN THE EXISTING INPUTS, CHECK IF IT STILL EXISTS IN THE NEW ONE, IF NOT DELETE
      existing.get().getInputs().forEach(input -> {
        if (process.getInputs().stream().noneMatch(i -> i.getStream().equals(input.getStream()))) {
          consumerService.canDeleteConsumer(buildConsumer(process, zoneKey, input));
        }
      });
    });
    process.getZones().forEach(zoneKey -> {
      process.getOutputs().forEach(output -> {
          // FOR EACH PROCESS OUTPUT CHECK IF IT EXISTS IN THE EXISTING PROCESS OUTPUTS AND IF IT DOES UPDATE EXISTING
          if (existing.get().getOutputs().stream().anyMatch(o -> o.getStream().equals(output.getStream()))) {
            producerService.canUpdateProducer(buildProducer(process, zoneKey, output));
            // FOR EACH PROCESS OUTPUT CHECK IF IT EXISTS IN THE EXISTING PROCESS OUTPUTS AND IF IT DOES NOT ADD NEW ONE
          } else {
            producerService.canCreateProducer(buildProducer(process, zoneKey, output));
          }
        }
      );
      // FOR EACH PROCESS OUTPUT IN THE EXISTING OUTPUTS, CHECK IF IT STILL EXISTS IN THE NEW ONE, IF NOT DELETE
      existing.get().getOutputs().forEach(output -> {
        if (process.getOutputs().stream().noneMatch(o -> o.getStream().equals(output.getStream()))) {
          producerService.canDeleteProducer(buildProducer(process, zoneKey, output));
        }
      });
    });

    return save(process);
  }

  private Consumer buildConsumer(Process process, ZoneKey zoneKey, ProcessInputStream input) {
    return new Consumer(
      new ConsumerKey(
        input.getStream().getDomainKey().getName(),
        input.getStream().getName(),
        input.getStream().getVersion(),
        zoneKey.getName(),
        process.getKey().getName()
      ),
      process.getSpecification(),
      process.getStatus()
    );
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
