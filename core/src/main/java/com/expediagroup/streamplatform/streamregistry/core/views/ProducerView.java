/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.views;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProducerView {
  private final ProducerRepository producerRepository;

  public Optional<Producer> get(ProducerKey key) {
    return producerRepository.findById(key);
  }

  public Stream<Producer> findAll(Predicate<Producer> filter) {
    return producerRepository.findAll().stream().filter(filter);
  }

  public boolean exists(ProducerKey key) {
    return get(key).isPresent();
  }
}
