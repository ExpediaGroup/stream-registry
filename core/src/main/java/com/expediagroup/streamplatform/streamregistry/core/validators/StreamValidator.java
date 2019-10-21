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

import com.expediagroup.streamplatform.streamregistry.core.services.DomainService;
import com.expediagroup.streamplatform.streamregistry.core.services.SchemaService;
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
public class StreamValidator implements Validator<Stream> {

  private DomainService domainService;
  private SchemaService schemaService;

  public StreamValidator(DomainService domainService, SchemaService schemaService) {
    this.domainService = domainService;
    this.schemaService = schemaService;
  }

  @Override
  public void validateForCreate(Stream stream) throws ValidationException {
    validateForCreateAndUpdate(stream);
    new SpecificationValidator().validateForCreate(stream.getSpecification());
  }

  @Override
  public void validateForUpdate(Stream stream, Stream existing) throws ValidationException {
    validateForCreateAndUpdate(stream);
    new SpecificationValidator().validateForUpdate(stream.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Stream stream) throws ValidationException {
    schemaService.validateSchemaBindingExists(stream.getSchemaKey());
    domainService.validateDomainExists(stream.getKey().getDomainKey());
  }

}