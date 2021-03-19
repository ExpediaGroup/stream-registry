/**
 * Copyright (C) 2018-2021 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.services.unsecured;

import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.keys.InfrastructureKey;
import com.expediagroup.streamplatform.streamregistry.repository.InfrastructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UnsecuredInfrastructureService {
  private final InfrastructureRepository infrastructureRepository;

  public Optional<Infrastructure> get(InfrastructureKey key) {
    return infrastructureRepository.findById(key);
  }

  public boolean exists(InfrastructureKey key) {
    return get(key).isPresent();
  }
}
