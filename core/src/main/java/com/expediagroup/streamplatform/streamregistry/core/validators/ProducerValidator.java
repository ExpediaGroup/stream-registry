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
import com.expediagroup.streamplatform.streamregistry.core.views.DomainView;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.core.views.ZoneView;
import com.expediagroup.streamplatform.streamregistry.model.Producer;

@Component
@RequiredArgsConstructor
public class ProducerValidator implements Validator<Producer> {
  private final StreamView streamView;
  private final ZoneView zoneView;

  private final DomainView domainView;
  private final KeyValidator<Producer> producerKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Producer producer) throws ValidationException {
    producerKeyValidator.validateKey(producer);
    validateForCreateAndUpdate(producer);
    specificationValidator.validateForCreate(producer.getSpecification());
  }

  @Override
  public void validateForUpdate(Producer producer, Producer existing) throws ValidationException {
    validateForCreateAndUpdate(producer);
    specificationValidator.validateForUpdate(producer.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Producer producer) throws ValidationException {
    if (!domainView.exists(producer.getKey().getStreamKey().getDomainKey())) {
      throw new ValidationException("Domain does not exist");
    }

    if (!streamView.exists(producer.getKey().getStreamKey())) {
      throw new ValidationException("Stream does not exist");
    }

    if (!zoneView.exists(producer.getKey().getZoneKey())) {
      throw new ValidationException("Zone does not exist");
    }
  }

}
