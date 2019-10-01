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

import com.expediagroup.streamplatform.streamregistry.core.services.ProducerService;
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;

@Component
public class ProducerBindingValidator implements Validator<ProducerBinding>{

  private ProducerService producerService;

  public ProducerBindingValidator(ProducerService producerService) {
    this.producerService = producerService;
  }

  @Override
  public void validateForCreate(ProducerBinding producerbinding) throws ValidationException {
    validateForCreateAndUpdate(producerbinding);
    new SpecificationValidator().validateForCreate(producerbinding.getSpecification());
  }

  @Override
  public void validateForUpdate(ProducerBinding producerbinding, ProducerBinding existing) throws ValidationException {
    validateForCreateAndUpdate(producerbinding);
    new SpecificationValidator().validateForUpdate(producerbinding.getSpecification(),existing.getSpecification());
  }

  public void validateForCreateAndUpdate(ProducerBinding producerbinding) throws ValidationException {
    if(producerService.read(producerbinding.getKey().getProducerKey()).isEmpty()){
      throw new ValidationException("Producer does not exist");
    }
  }


  }