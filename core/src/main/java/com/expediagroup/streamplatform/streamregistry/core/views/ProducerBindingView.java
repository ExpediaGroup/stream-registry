/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;

@Component
@RequiredArgsConstructor
public class ProducerBindingView {
  private final ProducerBindingRepository producerBindingRepository;

  public Optional<ProducerBinding> get(ProducerBindingKey key) {
    return producerBindingRepository.findById(key);
  }

  public Stream<ProducerBinding> findAll(Predicate<ProducerBinding> filter) {
    return producerBindingRepository.findAll().stream().filter(filter);
  }

  public boolean exists(ProducerBindingKey key) {
    return get(key).isPresent();
  }
}
