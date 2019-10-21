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

import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.core.services.ZoneService;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@Component
public class ConsumerValidator implements Validator<Consumer> {

  private StreamService streamService;
  private ZoneService zoneService;

  public ConsumerValidator(StreamService streamService, ZoneService zoneService) {
    this.zoneService=zoneService;
    this.streamService = streamService;
  }

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

  public void validateForCreateAndUpdate(Consumer consumer) throws ValidationException {
    validateStreamExists(consumer);
    validateZoneExists(consumer);
  }

  private void validateStreamExists(Consumer consumer) {
    if (streamService.read(consumer.getKey().getStreamKey()).isEmpty()) {
      throw new ValidationException("Stream does not exist");
    }
  }

  private void validateZoneExists(Consumer consumer) {
    if (zoneService.read(consumer.getKey().getZoneKey()).isEmpty()) {
      throw new ValidationException("Zone does not exist");
    }
  }}