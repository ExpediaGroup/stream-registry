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
package com.expediagroup.streamplatform.streamregistry.core.validator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Entity;

@Component
@RequiredArgsConstructor
public class EntityValidator {
  private final NameValidator nameValidator;

  public void validate(Entity<?> entity, Optional<? extends Entity<?>> existing) {
    nameValidator.validate(entity.getName());

    checkNotNull(entity.getConfiguration(), "Configuration must not be null.");
    checkNotNull(entity.getType(), "Type must not be null.");

    existing.ifPresent(e -> checkArgument(Objects.equals(
        entity.getType(),
        e.getType()),
        "Configuration must be of the same type as the existing."));

    //TODO owner is mandatory and exists
  }
}
