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

import com.expediagroup.streamplatform.streamregistry.core.view.DomainView;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Schema;

@Component
@RequiredArgsConstructor
public class SchemaValidator implements Validator<Schema> {
  private final DomainView domainView;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Schema schema) throws ValidationException {
    validateForCreateAndUpdate(schema);
    specificationValidator.validateForCreate(schema.getSpecification());
  }

  @Override
  public void validateForUpdate(Schema schema, Schema existing) throws ValidationException {
    validateForCreateAndUpdate(schema);
    specificationValidator.validateForUpdate(schema.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Schema schema) throws ValidationException {
    if (!domainView.exists(schema.getKey().getDomainKey())) {
      throw new ValidationException("Domain does not exist");
    }
  }
}
