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

import com.expediagroup.streamplatform.streamregistry.core.view.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.view.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsumerValidator implements Validator<Consumer> {
  private final StreamView streamView;
  private final ZoneView zoneView;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Consumer consumer) throws ValidationException {
    validateForCreateAndUpdate(consumer);
    specificationValidator.validateForCreate(consumer.getSpecification());
  }

  @Override
  public void validateForUpdate(Consumer consumer, Consumer existing) throws ValidationException {
    validateForCreateAndUpdate(consumer);
    specificationValidator.validateForUpdate(consumer.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Consumer consumer) throws ValidationException {
    if (!streamView.exists(consumer.getKey().getStreamKey())) {
      throw new ValidationException("Stream does not exist");
    }
    if (!zoneView.exists(consumer.getKey().getZoneKey())) {
      throw new ValidationException("Zone does not exist");
    }
  }

}
