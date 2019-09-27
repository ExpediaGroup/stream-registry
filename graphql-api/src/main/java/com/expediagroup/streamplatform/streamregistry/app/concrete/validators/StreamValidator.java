package com.expediagroup.streamplatform.streamregistry.app.concrete.validators;

/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.app.Stream;
import com.expediagroup.streamplatform.streamregistry.app.ValidationException;
import com.expediagroup.streamplatform.streamregistry.app.keys.DomainKey;
import com.expediagroup.streamplatform.streamregistry.app.services.DomainService;
import com.expediagroup.streamplatform.streamregistry.app.services.SchemaService;

@Component
public class StreamValidator { //todo implements Validator<T>

  private DomainService domainService; //todo implements Validator<T>
  private SchemaService schemaService;

  public StreamValidator(DomainService domainService, SchemaService schemaService) {
    this.domainService = domainService;
    this.schemaService = schemaService;
  }

  //@Override
  public void validateForCreate(Stream stream) throws ValidationException {
    validateForCreateAndUpdate(stream);
  }

  //@Override
  public void validateForUpdate(Stream stream, Stream existing) throws ValidationException {
    validateForCreateAndUpdate(stream);
  }

  public void validateForCreateAndUpdate(Stream stream) throws ValidationException {
    validateSchemaExists(stream);
    validateDomainExists(stream);
  }

  private void validateSchemaExists(Stream stream) {
    if (schemaService.read(stream.getSchemaKey()).isEmpty()) {
      throw new ValidationException("Schema does not exist");
    }
  }

  private void validateDomainExists(Stream stream) {
    DomainKey domainKey = new DomainKey(stream.getKey().getDomain());
    if (domainService.read(domainKey).isEmpty()) {
      throw new ValidationException("Domain does not exist");
    }
  }
}