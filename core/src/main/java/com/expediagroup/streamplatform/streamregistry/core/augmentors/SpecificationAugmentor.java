package com.expediagroup.streamplatform.streamregistry.core.augmentors;

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

import com.expediagroup.streamplatform.streamregistry.core.services.ValidationException;
import com.expediagroup.streamplatform.streamregistry.model.Specification;

@Component
public class SpecificationAugmentor { //todo implements Augmentor<T>

  //@Override
  public Specification augmentForCreate(Specification specification) throws ValidationException {
    return specification;
  }

  //@Override
  public Specification augmentForUpdate(Specification specification, Specification existing) throws ValidationException {
    return specification;
  }
}