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

import com.expediagroup.streamplatform.streamregistry.model.ConfiguredEntity;

@Component
@RequiredArgsConstructor
public class ConfiguredEntityValidator {
  public void validate(ConfiguredEntity<?> entity, Optional<? extends ConfiguredEntity<?>> existing) {
    checkNotNull(entity.getConfiguration(), "Configuration must not be null.");

    existing.ifPresent(e -> checkArgument(Objects.equals(
        entity.getConfiguration().getType(),
        e.getConfiguration().getType()),
        "Configuration must be of the same type as the existing."));
  }
}
