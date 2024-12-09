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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.validators.key.KeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.ProducerView;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProducerBindingValidator implements Validator<ProducerBinding> {
  private final ProducerView producerView;
  private final KeyValidator<ProducerBinding> producerBindingKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(ProducerBinding producerbinding) throws ValidationException {
    producerBindingKeyValidator.validateKey(producerbinding);
    validateForCreateAndUpdate(producerbinding);
    specificationValidator.validateForCreate(producerbinding.getSpecification());
  }

  @Override
  public void validateForUpdate(ProducerBinding producerbinding, ProducerBinding existing) throws ValidationException {
    validateForCreateAndUpdate(producerbinding);
    specificationValidator.validateForUpdate(producerbinding.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(ProducerBinding producerbinding) throws ValidationException {
    if (!producerView.exists(producerbinding.getKey().getProducerKey())) {
      throw new ValidationException("Producer does not exist");
    }
  }
}
