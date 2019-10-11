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

import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

@Component
public class ConsumerValidator implements Validator<Consumer> {

  @Override
  public void validateForCreate(Consumer consumer) throws ValidationException {
    validateForCreateAndUpdate(consumer);
    new SpecificationValidator().validateForCreate(consumer.getSpecification());
  }

  @Override
  public void validateForUpdate(Consumer consumer, Consumer existing) throws ValidationException {
    validateForCreateAndUpdate(consumer);
    new SpecificationValidator().validateForUpdate(consumer.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(Consumer consumer) {}
}