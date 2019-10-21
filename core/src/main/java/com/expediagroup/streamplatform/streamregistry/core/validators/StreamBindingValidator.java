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

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

@Component
public class StreamBindingValidator implements Validator<StreamBinding> {

  private StreamService streamService;
  private SpecificationValidator specificationValidator = new SpecificationValidator();

  public StreamBindingValidator(StreamService streamService) {
    this.streamService = streamService;
  }

  @Override
  public void validateForCreate(StreamBinding streambinding) throws ValidationException {
    validateForCreateAndUpdate(streambinding);
    specificationValidator.validateForCreate(streambinding.getSpecification());
  }

  @Override
  public void validateForUpdate(StreamBinding streambinding, StreamBinding existing) throws ValidationException {
    validateForCreateAndUpdate(streambinding);
    specificationValidator.validateForUpdate(streambinding.getSpecification(), existing.getSpecification());
  }

  private void validateForCreateAndUpdate(StreamBinding streambinding) throws ValidationException {
    validateStreamExists(streambinding);
  }

  private void validateStreamExists(StreamBinding streamBinding) {
    if (streamService.read(streamBinding.getKey().getStreamKey()).isEmpty()) {
      throw new ValidationException("Stream does not exist");
    }
  }
}