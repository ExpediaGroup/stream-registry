/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
import com.expediagroup.streamplatform.streamregistry.core.views.SchemaView;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
@RequiredArgsConstructor
public class StreamValidator implements Validator<Stream> {
  private final DomainView domainView;
  private final SchemaView schemaView;
  private final KeyValidator<Stream> streamKeyValidator;
  private final SpecificationValidator specificationValidator;

  @Override
  public void validateForCreate(Stream stream) throws ValidationException {
    streamKeyValidator.validateKey(stream);
    requireExistingDomain(stream);
    requireExistingSchema(stream);
    specificationValidator.validateForCreate(stream.getSpecification());
  }

  @Override
  public void validateForUpdate(Stream stream, Stream existing) throws ValidationException {
    requireExistingDomain(stream);
    specificationValidator.validateForUpdate(stream.getSpecification(), existing.getSpecification());
  }

  private void requireExistingDomain(Stream stream) {
    if (!domainView.exists(stream.getKey().getDomainKey())) {
      throw new ValidationException("Domain does not exist");
    }
  }

  private void requireExistingSchema(Stream stream) {
    if (stream.getSchemaKey() == null) {
      throw new ValidationException("Schema must be specified");
    }

    if (!schemaView.exists(stream.getSchemaKey())) {
      throw new ValidationException("Schema does not exist");
    }
  }
}
