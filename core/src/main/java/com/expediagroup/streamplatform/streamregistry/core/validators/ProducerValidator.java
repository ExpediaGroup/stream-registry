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
import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;

@Component
public class ProducerValidator {

  private final DomainService domainService;
  private StreamService streamService;

  public ProducerValidator(DomainService domainService, StreamService streamService) {
    this.domainService = domainService;
    this.streamService = streamService;
  }

  //@Override
  public void validateForCreate(Producer producer) throws ValidationException {
    validateForCreateAndUpdate(producer);
  }

  //@Override
  public void validateForUpdate(Producer producer, Producer existing) throws ValidationException {
    validateForCreateAndUpdate(producer);
  }

  public void validateForCreateAndUpdate(Producer producer) throws ValidationException {
    validateStreamExists(producer);
  }

  private void validateStreamExists(Producer producer) {
    //todo: get StreamKey as an object from producer, not as fields
    StreamKey streamKey = new StreamKey(
        producer.getKey().getStreamDomain(),
        producer.getKey().getStreamName(),
        producer.getKey().getStreamVersion()
    );
    if (streamService.read(streamKey).isEmpty()) {
      throw new ValidationException("Stream does not exist");
    }
  }
}