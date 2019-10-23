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
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;

@Component
public class InfrastructureValidator implements Validator<Infrastructure> {

  private ZoneService zoneService;

  public InfrastructureValidator( ZoneService zoneService) {
    this.zoneService=zoneService;
  }


  @Override
  public void validateForCreate(Infrastructure infrastructure) throws ValidationException {
    validateForCreateAndUpdate(infrastructure);
    new SpecificationValidator().validateForCreate(infrastructure.getSpecification());
  }

  @Override
  public void validateForUpdate(Infrastructure infrastructure, Infrastructure existing) throws ValidationException {
    validateForCreateAndUpdate(infrastructure);
    new SpecificationValidator().validateForUpdate(infrastructure.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Infrastructure infrastructure) throws ValidationException {
    zoneService.validateZoneExists(infrastructure.getKey().getZoneKey());
  }

}