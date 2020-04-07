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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.DomainService;
import com.expediagroup.streamplatform.streamregistry.core.services.SchemaService;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StreamValidator implements Validator<Stream> {
  private final DomainService domainService;
  private final SchemaService schemaService;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Stream stream) throws ValidationException {
    validateForCreateAndUpdate(stream);
    specificationValidator.validateForCreate(stream.getSpecification());
  }

  @Override
  public void validateForUpdate(Stream stream, Stream existing) throws ValidationException {
    validateForCreateAndUpdate(stream);
    specificationValidator.validateForUpdate(stream.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Stream stream) throws ValidationException {
    requireExistingDomain(stream);
    requireExistingSchema(stream);
  }

  private void requireExistingDomain(Stream stream) {
    if (!domainService.exists(stream.getKey().getDomainKey())) {
      throw new ValidationException("Domain does not exist");
    }
  }

  private void requireExistingSchema(Stream stream) {
    if (stream.getSchemaKey() == null) {
      throw new ValidationException("Schema must be specified");
    }
    if (!schemaService.exists(stream.getSchemaKey())) {
      throw new ValidationException("Schema does not exist");
    }
  }
}