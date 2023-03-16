/**
 * Copyright (C) 2018-2023 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.KeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Process;

@Component
@RequiredArgsConstructor
public class ProcessValidator implements Validator<Process> {
  private final StreamView streamView;
  private final ZoneView zoneView;

  private final DomainView domainView;
  private final KeyValidator<Process> processKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Process process) throws ValidationException {
    processKeyValidator.validateKey(process);
    validateForCreateAndUpdate(process);
    specificationValidator.validateForCreate(process.getSpecification());
  }

  @Override
  public void validateForUpdate(Process process, Process existing) throws ValidationException {
    validateForCreateAndUpdate(process);
    specificationValidator.validateForUpdate(process.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(Process process) throws ValidationException {
    requireExistingDomain(process);
    process.getZones().forEach(zone -> {
      if (!zoneView.exists(zone)) {
        throw new ValidationException("Zone [" + zone + "] does not exist");
      }
    });

    process.getInputs().forEach(input -> {
      if (!streamView.exists(input.getStream())) {
        throw new ValidationException("Input stream [" + input.getStream() + "] does not exist");
      }
    });

    process.getOutputs().forEach(output -> {
      if (!streamView.exists(output.getStream())) {
        throw new ValidationException("Output stream [" + output.getStream() + "] does not exist");
      }
    });
  }

  private void requireExistingDomain(Process process) {
    if (!domainView.exists(process.getKey().getDomainKey())) {
      throw new ValidationException("Domain [" + process.getKey().getDomain() + "] does not exist");
    }
  }
}
