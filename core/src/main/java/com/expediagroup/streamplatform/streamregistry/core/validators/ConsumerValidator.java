/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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

import com.expediagroup.streamplatform.streamregistry.core.validators.key.KeyValidator;
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Consumer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConsumerValidator implements Validator<Consumer> {
  private final StreamView streamView;
  private final ZoneView zoneView;
  private final DomainView domainView;
  private final KeyValidator<Consumer> consumerKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Consumer consumer) throws ValidationException {
    consumerKeyValidator.validateKey(consumer);
    validateForCreateAndUpdate(consumer);
    specificationValidator.validateForCreate(consumer.getSpecification());
  }

  @Override
  public void validateForUpdate(Consumer consumer, Consumer existing) throws ValidationException {
    validateForCreateAndUpdate(consumer);
    specificationValidator.validateForUpdate(consumer.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Consumer consumer) throws ValidationException {
    if (!domainView.exists(consumer.getKey().getStreamKey().getDomainKey())) {
      throw new ValidationException("Domain does not exist");
    }

    if (!streamView.exists(consumer.getKey().getStreamKey())) {
      throw new ValidationException("Stream does not exist");
    }
    if (!zoneView.exists(consumer.getKey().getZoneKey())) {
      throw new ValidationException("Zone does not exist");
    }
  }

}
