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

import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.StreamBindingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StreamBindingView {
  private final StreamBindingRepository streamBindingRepository;

  public Optional<StreamBinding> get(StreamBindingKey key) {
    return streamBindingRepository.findById(key);
  }

  public Stream<StreamBinding> findAll(Predicate<StreamBinding> filter) {
    return streamBindingRepository.findAll().stream().filter(filter);
  }

  public boolean exists(StreamBindingKey key) {
    return get(key).isPresent();
  }
}
