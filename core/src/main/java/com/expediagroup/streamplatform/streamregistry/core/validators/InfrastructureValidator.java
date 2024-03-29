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
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;


@Component
@RequiredArgsConstructor
public class InfrastructureValidator implements Validator<Infrastructure> {
  private final ZoneView zoneView;
  private final KeyValidator<Infrastructure> infrastructureKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Infrastructure infrastructure) throws ValidationException {
    infrastructureKeyValidator.validateKey(infrastructure);
    validateForCreateAndUpdate(infrastructure);
    specificationValidator.validateForCreate(infrastructure.getSpecification());
  }

  @Override
  public void validateForUpdate(Infrastructure infrastructure, Infrastructure existing) throws ValidationException {
    validateForCreateAndUpdate(infrastructure);
    specificationValidator.validateForUpdate(infrastructure.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Infrastructure infrastructure) throws ValidationException {
    if (!zoneView.exists(infrastructure.getKey().getZoneKey())) {
      throw new ValidationException("Zone does not exist");
    }
  }

}
