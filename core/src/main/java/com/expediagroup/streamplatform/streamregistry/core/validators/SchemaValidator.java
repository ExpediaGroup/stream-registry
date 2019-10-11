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
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.keys.DomainKey;

@Component
public class SchemaValidator implements Validator<Schema> {

  private DomainService domainService;

  public SchemaValidator(DomainService domainService) {
    this.domainService = domainService;
  }

  @Override
  public void validateForCreate(Schema schema) throws ValidationException {
    validateForCreateAndUpdate(schema);
    new SpecificationValidator().validateForCreate(schema.getSpecification());
  }

  @Override
  public void validateForUpdate(Schema schema, Schema existing) throws ValidationException {
    validateForCreateAndUpdate(schema);
    new SpecificationValidator().validateForUpdate(schema.getSpecification(), existing.getSpecification());
  }

  public void validateForCreateAndUpdate(Schema schema) throws ValidationException {
    validateDomainExists(schema);
  }

  private void validateDomainExists(Schema schema) {
    DomainKey domainKey = new DomainKey(schema.getKey().getDomain());
    if (domainService.read(domainKey).isEmpty()) {
      throw new ValidationException("Domain does not exist");
    }
  }
}