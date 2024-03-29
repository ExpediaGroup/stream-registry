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
import com.expediagroup.streamplatform.streamregistry.model.Zone;

@Component
@RequiredArgsConstructor
public class ZoneValidator implements Validator<Zone> {
  private final KeyValidator<Zone> zoneKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Zone zone) throws ValidationException {
    zoneKeyValidator.validateKey(zone);
    validateForCreateAndUpdate(zone);
    specificationValidator.validateForCreate(zone.getSpecification());
  }

  @Override
  public void validateForUpdate(Zone zone, Zone existing) throws ValidationException {
    validateForCreateAndUpdate(zone);
    specificationValidator.validateForUpdate(zone.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Zone zone) throws ValidationException {
  }
}
