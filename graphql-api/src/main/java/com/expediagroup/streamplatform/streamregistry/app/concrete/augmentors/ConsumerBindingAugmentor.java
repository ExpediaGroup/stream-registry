package com.expediagroup.streamplatform.streamregistry.app.concrete.augmentors;

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

import com.expediagroup.streamplatform.streamregistry.app.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.app.ValidationException;

@Component
public class ConsumerBindingAugmentor { //todo implements Augmentor<T>

  //@Override
  public ConsumerBinding augmentForCreate(ConsumerBinding consumerbinding) throws ValidationException {
    return consumerbinding;
  }

  //@Override
  public ConsumerBinding augmentForUpdate(ConsumerBinding consumerbinding, ConsumerBinding existing) throws ValidationException {
    return consumerbinding;
  }
}