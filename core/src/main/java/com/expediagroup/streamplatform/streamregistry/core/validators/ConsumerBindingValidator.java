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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.views.ConsumerView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamBindingView;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

@Component
@RequiredArgsConstructor
public class ConsumerBindingValidator implements Validator<ConsumerBinding> {
  private final ConsumerView consumerView;
  private final StreamBindingView streamBindingView;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(ConsumerBinding consumerbinding) throws ValidationException {
    validateForCreateAndUpdate(consumerbinding);
    specificationValidator.validateForCreate(consumerbinding.getSpecification());
  }

  @Override
  public void validateForUpdate(ConsumerBinding consumerbinding, ConsumerBinding existing) throws ValidationException {
    validateForCreateAndUpdate(consumerbinding);
    specificationValidator.validateForUpdate(consumerbinding.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(ConsumerBinding consumerbinding) {
    if (!consumerView.exists(consumerbinding.getKey().getConsumerKey())) {
      throw new ValidationException("Consumer does not exist");
    }
    if (!streamBindingView.exists(consumerbinding.getKey().getStreamBindingKey())) {
      throw new ValidationException("StreamBinding does not exist");
    }
  }
}
