/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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

import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredStreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.unsecured.UnsecuredZoneService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@Component
@RequiredArgsConstructor
public class ProducerValidator implements Validator<Producer> {
  private final UnsecuredStreamService streamService;
  private final UnsecuredZoneService zoneService;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Producer producer) throws ValidationException {
    validateForCreateAndUpdate(producer);
    specificationValidator.validateForCreate(producer.getSpecification());
  }

  @Override
  public void validateForUpdate(Producer producer, Producer existing) throws ValidationException {
    validateForCreateAndUpdate(producer);
    specificationValidator.validateForUpdate(producer.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Producer producer) throws ValidationException {
    if (!streamService.exists(producer.getKey().getStreamKey())) {
      throw new ValidationException("Stream does not exist");
    }
    if (!zoneService.exists(producer.getKey().getZoneKey())) {
      throw new ValidationException("Zone does not exist");
    }
  }

}
