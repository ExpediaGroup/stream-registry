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

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Specification;


@Component
public class SpecificationValidator implements Validator<Specification> {

  @Override
  public void validateForCreate(Specification specification) throws ValidationException {
    validateForCreateAndUpdate(specification);
  }

  @Override
  public void validateForUpdate(Specification specification, Specification existing) throws ValidationException {
    validateForCreateAndUpdate(specification);
    if (!Objects.equals(specification.getType(), existing.getType())) {
      throw new ValidationException("Configuration must be of the same type as the existing.");
    }
  }

  private void validateForCreateAndUpdate(Specification specification) {
    if (specification.getConfiguration() == null) {
      throw new ValidationException("Configuration must not be null.");
    }
    if (specification.getType() == null) {
      throw new ValidationException("Type must not be null.");
    }
  }
}
