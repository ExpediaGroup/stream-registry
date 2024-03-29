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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.KeyValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

@Component
@RequiredArgsConstructor
public class DomainValidator implements Validator<Domain> {
  private final KeyValidator<Domain> domainKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Domain domain) throws ValidationException {
    domainKeyValidator.validateKey(domain);
    validateForCreateAndUpdate(domain);
    specificationValidator.validateForCreate(domain.getSpecification());
  }

  @Override
  public void validateForUpdate(Domain domain, Domain existing) throws ValidationException {
    validateForCreateAndUpdate(domain);
    specificationValidator.validateForUpdate(domain.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(Domain domain) {
  }
}
