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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ConsumerService;
import com.expediagroup.streamplatform.streamregistry.core.services.ProducerService;
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;

@Component
public class ConsumerBindingValidator implements Validator<ConsumerBinding> {

  private ConsumerService consumerService;

  public ConsumerBindingValidator(ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  @Override
  public void validateForCreate(ConsumerBinding consumerbinding) throws ValidationException {
    validateForCreateAndUpdate(consumerbinding);
    new SpecificationValidator().validateForCreate(consumerbinding.getSpecification());
  }

  @Override
  public void validateForUpdate(ConsumerBinding consumerbinding, ConsumerBinding existing) throws ValidationException {
    validateForCreateAndUpdate(consumerbinding);
    new SpecificationValidator().validateForUpdate(consumerbinding.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(ConsumerBinding consumerbinding) {
    consumerService.validateConsumerExists(consumerbinding.getKey().getConsumerKey());
  }
}